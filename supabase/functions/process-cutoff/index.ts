// Supabase Edge Function — process-cutoff
// Replaces Firebase Cloud Functions lunchCutoff / dinnerCutoff (Blaze plan required).
// Called by cron-job.org twice daily:
//   Lunch:  POST at 07:00 UTC  { "mealSlot": "lunch"  }
//   Dinner: POST at 14:00 UTC  { "mealSlot": "dinner" }
//
// Required Supabase secrets:
//   FIREBASE_SERVICE_ACCOUNT  — full service account JSON (one line)
//   FUNCTIONS_SECRET          — shared bearer token for auth

import { initializeApp, cert, getApps } from "npm:firebase-admin/app";
import { getFirestore, FieldValue }      from "npm:firebase-admin/firestore";
import { getMessaging }                  from "npm:firebase-admin/messaging";

// ── Init Firebase Admin (idempotent across warm starts) ──────────────────────
if (getApps().length === 0) {
  const sa = JSON.parse(Deno.env.get("FIREBASE_SERVICE_ACCOUNT")!);
  initializeApp({ credential: cert(sa) });
}

const db = getFirestore();

// ── Handler ──────────────────────────────────────────────────────────────────
Deno.serve(async (req: Request) => {
  // Auth check
  const secret = Deno.env.get("FUNCTIONS_SECRET");
  if (req.headers.get("Authorization") !== `Bearer ${secret}`) {
    return new Response("Unauthorized", { status: 401 });
  }

  if (req.method !== "POST") {
    return new Response("Method Not Allowed", { status: 405 });
  }

  const { mealSlot } = await req.json() as { mealSlot: string };
  if (mealSlot !== "lunch" && mealSlot !== "dinner") {
    return new Response("Bad Request: mealSlot must be lunch or dinner", { status: 400 });
  }

  await processCutoff(mealSlot);
  return new Response(JSON.stringify({ ok: true, mealSlot }), {
    headers: { "Content-Type": "application/json" },
  });
});

// ── Core logic (mirrors functions/index.js) ──────────────────────────────────

async function processCutoff(mealSlot: string) {
  const today = new Date().toISOString().split("T")[0]; // "YYYY-MM-DD" in UTC (matches EAT date at cutoff)

  const snapshot = await db.collection("preOrders")
    .where("mealSlot",      "==", mealSlot)
    .where("scheduledDate", "==", today)
    .where("status",        "==", "scheduled")
    .get();

  await Promise.all(snapshot.docs.map(doc => processPreOrder(doc)));
}

async function processPreOrder(preOrderDoc: FirebaseFirestore.QueryDocumentSnapshot) {
  const preOrder    = preOrderDoc.data();
  const userRef     = db.collection("users").doc(preOrder.userId);
  const preOrderRef = preOrderDoc.ref;

  try {
    await db.runTransaction(async (tx) => {
      const userSnap = await tx.get(userRef);
      const balance  = userSnap.data()!.walletBalance as number;

      if (balance >= preOrder.totalAmount) {
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
          createdAt:      FieldValue.serverTimestamp(),
        });
      } else {
        tx.update(preOrderRef, { status: "cancelled" });
      }
    });

    // Post-transaction: FCM + recurring (outside transaction to avoid read-after-write)
    const refreshed = (await preOrderRef.get()).data()!;
    if (refreshed.status === "confirmed") {
      await sendFCM(preOrder.userId,
        "Pre-order Confirmed",
        `Your ${preOrder.mealSlot} pre-order is confirmed. See you at the cafeteria!`);
    } else {
      await sendFCM(preOrder.userId,
        "Pre-order Cancelled",
        "Insufficient wallet balance. Please top up at the cafeteria counter.");
    }

    if (preOrder.recurring && Array.isArray(preOrder.recurringDays) && preOrder.recurringDays.length > 0) {
      await scheduleNextOccurrence(preOrder);
    }
  } catch (e) {
    console.error("Error processing pre-order", preOrderDoc.id, e);
  }
}

async function scheduleNextOccurrence(preOrder: FirebaseFirestore.DocumentData) {
  const dayNames = ["sunday","monday","tuesday","wednesday","thursday","friday","saturday"];
  const nextDate = new Date();
  nextDate.setDate(nextDate.getDate() + 1);

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
    createdAt:     FieldValue.serverTimestamp(),
  });
}

async function sendFCM(userId: string, title: string, body: string) {
  try {
    const userSnap = await db.collection("users").doc(userId).get();
    const token    = userSnap.data()?.fcmToken as string | undefined;
    if (!token) return;
    await getMessaging().send({ token, notification: { title, body } });
  } catch (e) {
    console.error("FCM send failed for user", userId, e);
  }
}
