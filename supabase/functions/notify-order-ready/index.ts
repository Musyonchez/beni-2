// Supabase Edge Function — notify-order-ready
// Replaces the Firebase Cloud Function onOrderReady Firestore trigger.
// Called directly by the Android staff app after updating order status to "ready".
//
// POST body: { "userId": "<uid>" }
// Header:    Authorization: Bearer <FUNCTIONS_SECRET>

import { initializeApp, cert, getApps, getApp } from "npm:firebase-admin@12/app";
import { getFirestore }                          from "npm:firebase-admin@12/firestore";
import { getMessaging }                          from "npm:firebase-admin@12/messaging";

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

    const { userId } = await req.json() as { userId: string };
    if (!userId) {
      return new Response("Bad Request: userId required", { status: 400 });
    }

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
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : String(e);
    console.error("notify-order-ready error:", msg);
    return new Response(JSON.stringify({ error: msg }), {
      status: 500, headers: { "Content-Type": "application/json" },
    });
  }
});
