// Supabase Edge Function — notify-order-ready
// Replaces the Firebase Cloud Function onOrderReady Firestore trigger.
// Called directly by the Android staff app after updating order status to "ready".
//
// POST body: { "userId": "<uid>" }
// Header:    Authorization: Bearer <FUNCTIONS_SECRET>
//
// Required Supabase secrets:
//   FIREBASE_SERVICE_ACCOUNT  — full service account JSON (one line)
//   FUNCTIONS_SECRET          — shared bearer token for auth

import { initializeApp, cert, getApps } from "npm:firebase-admin/app";
import { getFirestore }                  from "npm:firebase-admin/firestore";
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

  const { userId } = await req.json() as { userId: string };
  if (!userId) {
    return new Response("Bad Request: userId required", { status: 400 });
  }

  try {
    const userSnap = await db.collection("users").doc(userId).get();
    const token    = userSnap.data()?.fcmToken as string | undefined;

    if (token) {
      await getMessaging().send({
        token,
        notification: {
          title: "Order Ready!",
          body:  "Your order is ready for collection at the cafeteria.",
        },
      });
    }

    return new Response(JSON.stringify({ ok: true, notified: !!token }), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (e) {
    console.error("notify-order-ready error", e);
    return new Response("Internal Server Error", { status: 500 });
  }
});
