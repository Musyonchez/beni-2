const { onSchedule }        = require("firebase-functions/v2/scheduler");
const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { initializeApp }     = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging }      = require("firebase-admin/messaging");

initializeApp();

// ── Scheduled cut-offs ──────────────────────────────────────────────────────

// 10:00 AM EAT = 07:00 UTC
exports.lunchCutoff = onSchedule(
  { schedule: "0 7 * * *", timeZone: "Africa/Nairobi" },
  async () => { await processCutoff("lunch"); }
);

// 5:00 PM EAT = 14:00 UTC
exports.dinnerCutoff = onSchedule(
  { schedule: "0 14 * * *", timeZone: "Africa/Nairobi" },
  async () => { await processCutoff("dinner"); }
);

async function processCutoff(mealSlot) {
  const db    = getFirestore();
  const today = new Date().toISOString().split("T")[0]; // "YYYY-MM-DD"

  const snapshot = await db.collection("preOrders")
    .where("mealSlot",       "==", mealSlot)
    .where("scheduledDate",  "==", today)
    .where("status",         "==", "scheduled")
    .get();

  await Promise.all(snapshot.docs.map(doc => processPreOrder(db, doc)));
}

async function processPreOrder(db, preOrderDoc) {
  const preOrder    = preOrderDoc.data();
  const userRef     = db.collection("users").doc(preOrder.userId);
  const preOrderRef = preOrderDoc.ref;

  try {
    await db.runTransaction(async (tx) => {
      const userSnap = await tx.get(userRef);
      const balance  = userSnap.data().walletBalance;

      if (balance >= preOrder.totalAmount) {
        // Deduct wallet and confirm
        tx.update(userRef,     { walletBalance: balance - preOrder.totalAmount });
        tx.update(preOrderRef, { status: "confirmed" });

        const txRef = db.collection("walletTransactions").doc();
        tx.set(txRef, {
          txId:           txRef.id,
          userId:         preOrder.userId,
          type:           "deduction",
          amount:         preOrder.totalAmount,
          description:    `Pre-order #${preOrderDoc.id} \u2014 ${preOrder.mealSlot}`,
          relatedOrderId: preOrderDoc.id,
          createdAt:      FieldValue.serverTimestamp()
        });
      } else {
        // Insufficient funds — cancel
        tx.update(preOrderRef, { status: "cancelled" });
      }
    });

    // Post-transaction: FCM + recurring (runs outside transaction to avoid read-after-write)
    const confirmed = (await preOrderRef.get()).data().status === "confirmed";
    if (confirmed) {
      await sendFCM(db, preOrder.userId,
        "Pre-order Confirmed",
        `Your ${preOrder.mealSlot} pre-order is confirmed. See you at the cafeteria!`);
    } else {
      await sendFCM(db, preOrder.userId,
        "Pre-order Cancelled",
        "Insufficient wallet balance. Please top up at the cafeteria counter.");
    }

    // Schedule next occurrence for recurring pre-orders
    if (preOrder.recurring && Array.isArray(preOrder.recurringDays) && preOrder.recurringDays.length > 0) {
      await scheduleNextOccurrence(db, preOrder);
    }
  } catch (e) {
    console.error("Error processing pre-order", preOrderDoc.id, e);
  }
}

async function scheduleNextOccurrence(db, preOrder) {
  const dayNames = ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"];
  const nextDate = new Date();
  nextDate.setDate(nextDate.getDate() + 1); // start checking from tomorrow

  for (let i = 0; i < 7; i++) {
    if (preOrder.recurringDays.includes(dayNames[nextDate.getDay()])) break;
    nextDate.setDate(nextDate.getDate() + 1);
  }

  const nextRef = db.collection("preOrders").doc();
  await nextRef.set({
    ...preOrder,
    preOrderId:    nextRef.id,
    scheduledDate: nextDate.toISOString().split("T")[0],
    status:        "scheduled",
    createdAt:     FieldValue.serverTimestamp()
  });
}

// ── FCM helper ───────────────────────────────────────────────────────────────

async function sendFCM(db, userId, title, body) {
  try {
    const userSnap = await db.collection("users").doc(userId).get();
    const token    = userSnap.data()?.fcmToken;
    if (!token) return;
    await getMessaging().send({ token, notification: { title, body } });
  } catch (e) {
    console.error("FCM send failed for user", userId, e);
  }
}

// ── Firestore trigger: notify student when order is ready ────────────────────

exports.onOrderReady = onDocumentUpdated("orders/{orderId}", async (event) => {
  const before = event.data.before.data();
  const after  = event.data.after.data();
  if (before.status !== "ready" && after.status === "ready") {
    await sendFCM(
      getFirestore(),
      after.userId,
      "Order Ready!",
      "Your order is ready for collection at the cafeteria."
    );
  }
});
