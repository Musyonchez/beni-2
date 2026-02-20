# 5 — Firebase Setup & Cloud Functions

## Services used
| Service             | Purpose                                              |
|---------------------|------------------------------------------------------|
| Firebase Auth       | Email + password login for students and staff        |
| Cloud Firestore     | All data storage (NoSQL, real-time)                  |
| Cloud Functions     | Scheduled cron jobs for pre-order cut-off processing |
| Cloud Messaging     | Push notifications to students                       |

**Plan:** Firebase Blaze (pay-as-you-go) required for Cloud Functions. Usage at USIU scale stays within free tier limits.

---

## Firebase project setup checklist
1. Create Firebase project at console.firebase.google.com
2. Add Android app with package com.usiu.cafeteria
3. Download google-services.json -> place in app/
4. Enable Authentication -> Email/Password provider
5. Enable Firestore -> start in production mode
6. Enable Cloud Messaging (automatic with Firebase project)
7. Upgrade to Blaze plan (for Cloud Functions)
8. Deploy Cloud Functions (Node.js 18)

---

## Firestore Security Rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function isAuth() { return request.auth != null; }
    function isStaff() {
      return isAuth() &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'staff';
    }
    function isOwner(uid) { return isAuth() && request.auth.uid == uid; }

    match /users/{userId} {
      allow read: if isOwner(userId) || isStaff();
      allow create: if isAuth() && isOwner(userId);
      // walletBalance only changed by Cloud Functions (admin SDK) or staff
      allow update: if isStaff();
    }

    match /menuItems/{itemId} {
      allow read: if isAuth();
      allow write: if isStaff();
    }

    match /orders/{orderId} {
      allow read: if isStaff() || (isAuth() && resource.data.userId == request.auth.uid);
      allow create: if isAuth() && request.resource.data.userId == request.auth.uid;
      allow update: if isStaff();
    }

    match /preOrders/{preOrderId} {
      allow read: if isStaff() || (isAuth() && resource.data.userId == request.auth.uid);
      allow create: if isAuth() && request.resource.data.userId == request.auth.uid;
      allow update: if isStaff() ||
        (isAuth() && resource.data.userId == request.auth.uid
          && request.resource.data.status == 'cancelled');
    }

    match /walletTransactions/{txId} {
      allow read: if isOwner(resource.data.userId) || isStaff();
      allow write: if isStaff();
    }
  }
}
```

---

## FCM — Push Notifications

### Setup on Android
1. google-services.json already includes FCM config — no extra setup needed
2. Create MyFirebaseMessagingService extends FirebaseMessagingService
3. Override onNewToken(String token) -> save token to users/{uid}.fcmToken in Firestore
4. Override onMessageReceived(RemoteMessage msg) -> build and show a NotificationCompat notification

### Notification types
| Trigger                        | Title               | Body                                                         |
|-------------------------------|---------------------|--------------------------------------------------------------|
| Pre-order confirmed            | Pre-order Confirmed | Your [lunch/dinner] pre-order is confirmed. See you there!   |
| Pre-order cancelled (no funds) | Pre-order Cancelled | Insufficient wallet balance. Please top up at the counter.   |
| Order status -> Ready          | Order Ready!        | Your order is ready for collection at the cafeteria.         |

---

## Cloud Functions (Node.js 18, Firebase Functions v2)

### functions/index.js

```javascript
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

// 10:00 AM EAT = 07:00 UTC
exports.lunchCutoff = onSchedule(
  { schedule: "0 7 * * *", timeZone: "Africa/Nairobi" },
  async (event) => { await processCutoff("lunch"); }
);

// 5:00 PM EAT = 14:00 UTC
exports.dinnerCutoff = onSchedule(
  { schedule: "0 14 * * *", timeZone: "Africa/Nairobi" },
  async (event) => { await processCutoff("dinner"); }
);

async function processCutoff(mealSlot) {
  const db = getFirestore();
  const today = new Date().toISOString().split("T")[0]; // YYYY-MM-DD

  const snapshot = await db.collection("preOrders")
    .where("mealSlot", "==", mealSlot)
    .where("scheduledDate", "==", today)
    .where("status", "==", "scheduled")
    .get();

  await Promise.all(snapshot.docs.map(doc => processPreOrder(db, doc)));
}

async function processPreOrder(db, preOrderDoc) {
  const preOrder = preOrderDoc.data();
  const userRef = db.collection("users").doc(preOrder.userId);
  const preOrderRef = preOrderDoc.ref;

  try {
    await db.runTransaction(async (tx) => {
      const userSnap = await tx.get(userRef);
      const balance = userSnap.data().walletBalance;

      if (balance >= preOrder.totalAmount) {
        tx.update(userRef, { walletBalance: balance - preOrder.totalAmount });
        tx.update(preOrderRef, { status: "confirmed" });
        const txRef = db.collection("walletTransactions").doc();
        tx.set(txRef, {
          txId: txRef.id,
          userId: preOrder.userId,
          type: "deduction",
          amount: preOrder.totalAmount,
          description: "Pre-order #" + preOrderDoc.id + " - " + preOrder.mealSlot,
          relatedOrderId: preOrderDoc.id,
          createdAt: FieldValue.serverTimestamp()
        });
        if (preOrder.recurring && preOrder.recurringDays?.length > 0) {
          await scheduleNextOccurrence(db, preOrder);
        }
        await sendFCM(db, preOrder.userId, "Pre-order Confirmed",
          "Your " + preOrder.mealSlot + " pre-order is confirmed. See you at the cafeteria!");
      } else {
        tx.update(preOrderRef, { status: "cancelled" });
        if (preOrder.recurring && preOrder.recurringDays?.length > 0) {
          await scheduleNextOccurrence(db, preOrder);
        }
        await sendFCM(db, preOrder.userId, "Pre-order Cancelled",
          "Insufficient wallet balance. Please top up at the cafeteria counter.");
      }
    });
  } catch (e) {
    console.error("Error processing pre-order", preOrderDoc.id, e);
  }
}

async function scheduleNextOccurrence(db, preOrder) {
  const dayNames = ["sunday","monday","tuesday","wednesday","thursday","friday","saturday"];
  let nextDate = new Date();
  nextDate.setDate(nextDate.getDate() + 1);
  for (let i = 0; i < 7; i++) {
    if (preOrder.recurringDays.includes(dayNames[nextDate.getDay()])) break;
    nextDate.setDate(nextDate.getDate() + 1);
  }
  const nextRef = db.collection("preOrders").doc();
  await nextRef.set({
    ...preOrder,
    preOrderId: nextRef.id,
    scheduledDate: nextDate.toISOString().split("T")[0],
    status: "scheduled",
    createdAt: FieldValue.serverTimestamp()
  });
}

async function sendFCM(db, userId, title, body) {
  const userSnap = await db.collection("users").doc(userId).get();
  const token = userSnap.data()?.fcmToken;
  if (!token) return;
  await getMessaging().send({ token, notification: { title, body } });
}

// Firestore trigger: notify student when staff marks order as "ready"
exports.onOrderReady = onDocumentUpdated("orders/{orderId}", async (event) => {
  const before = event.data.before.data();
  const after = event.data.after.data();
  if (before.status !== "ready" && after.status === "ready") {
    await sendFCM(getFirestore(), after.userId, "Order Ready!",
      "Your order is ready for collection at the cafeteria.");
  }
});
```

---

## Deploying Cloud Functions
```bash
npm install -g firebase-tools
firebase login
firebase init functions   # select existing project, Node.js 18, JavaScript
# edit functions/index.js with the code above
firebase deploy --only functions
```
