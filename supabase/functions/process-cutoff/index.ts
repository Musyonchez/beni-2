// Supabase Edge Function — process-cutoff
// Replaces Firebase Cloud Functions lunchCutoff / dinnerCutoff (Blaze plan required).
// Called by cron-job.org twice daily:
//   Lunch:  POST at 07:00 UTC  { "mealSlot": "lunch"  }
//   Dinner: POST at 14:00 UTC  { "mealSlot": "dinner" }

import { initializeApp, cert, getApps, getApp } from "npm:firebase-admin@12/app";
import { getFirestore, FieldValue }               from "npm:firebase-admin@12/firestore";
import { getMessaging }                           from "npm:firebase-admin@12/messaging";

function getFirebaseApp() {
  if (getApps().length > 0) return getApp();
  const raw = Deno.env.get("FIREBASE_SERVICE_ACCOUNT");
  if (!raw) throw new Error("FIREBASE_SERVICE_ACCOUNT secret not set");
  const sa = JSON.parse(raw);
  return initializeApp({ credential: cert(sa) });
}

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

  try {
    const app = getFirebaseApp();
    const db  = getFirestore(app);
    db.settings({ preferRest: true });

    const { mealSlot } = await req.json() as { mealSlot: string };
    if (mealSlot !== "lunch" && mealSlot !== "dinner") {
      return new Response("Bad Request: mealSlot must be lunch or dinner", { status: 400 });
    }

    await processCutoff(db, mealSlot);
    return new Response(JSON.stringify({ ok: true, mealSlot }), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : String(e);
    console.error("process-cutoff error:", msg);
    return new Response(JSON.stringify({ error: msg }), {
      status: 500, headers: { "Content-Type": "application/json" },
    });
  }
});

// ── Core logic ───────────────────────────────────────────────────────────────

async function processCutoff(db: ReturnType<typeof getFirestore>, mealSlot: string) {
  const today = new Date().toISOString().split("T")[0];

  const snapshot = await db.collection("preOrders")
    .where("mealSlot",      "==", mealSlot)
    .where("scheduledDate", "==", today)
    .where("status",        "==", "scheduled")
    .get();

  await Promise.all(snapshot.docs.map(doc => processPreOrder(db, doc)));
}

// deno-lint-ignore no-explicit-any
async function processPreOrder(db: ReturnType<typeof getFirestore>, preOrderDoc: any) {
  const preOrder    = preOrderDoc.data();
  const userRef     = db.collection("users").doc(preOrder.userId);
  const preOrderRef = preOrderDoc.ref;

  try {
    await db.runTransaction(async (tx: any) => {
      const userSnap = await tx.get(userRef);
      const balance  = userSnap.data().walletBalance as number;

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

    const refreshed = (await preOrderRef.get()).data();
    if (refreshed.status === "confirmed") {
      await sendFCM(db, preOrder.userId,
        "Pre-order Confirmed",
        `Your ${preOrder.mealSlot} pre-order is confirmed. See you at the cafeteria!`);
    } else {
      await sendFCM(db, preOrder.userId,
        "Pre-order Cancelled",
        "Insufficient wallet balance. Please top up at the cafeteria counter.");
    }

    if (preOrder.recurring && Array.isArray(preOrder.recurringDays) && preOrder.recurringDays.length > 0) {
      await scheduleNextOccurrence(db, preOrder);
    }
  } catch (e) {
    console.error("Error processing pre-order", preOrderDoc.id, e);
  }
}

async function scheduleNextOccurrence(db: ReturnType<typeof getFirestore>, preOrder: any) {
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

async function sendFCM(db: ReturnType<typeof getFirestore>, userId: string, title: string, body: string) {
  try {
    const userSnap = await db.collection("users").doc(userId).get();
    const token    = userSnap.data()?.fcmToken as string | undefined;
    if (!token) return;
    await getMessaging().send({ token, notification: { title, body } });
  } catch (e) {
    console.error("FCM send failed for user", userId, e);
  }
}
