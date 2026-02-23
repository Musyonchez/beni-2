import { NextRequest, NextResponse } from "next/server";
import { adminAuth, adminDb } from "@/lib/firebase-admin";

export async function POST(req: NextRequest) {
  try {
    const { name, email, password } = await req.json();

    if (!name || !email || !password) {
      return NextResponse.json({ error: "name, email and password are required" }, { status: 400 });
    }

    // Create Firebase Auth user
    const userRecord = await adminAuth.createUser({ email, password, displayName: name });

    // Write Firestore doc
    await adminDb.collection("users").doc(userRecord.uid).set({
      uid:           userRecord.uid,
      name,
      email,
      role:          "staff",
      walletBalance: 0,
      fcmToken:      null,
      createdAt:     new Date(),
    });

    return NextResponse.json({ ok: true, uid: userRecord.uid });
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : String(e);
    return NextResponse.json({ error: msg }, { status: 500 });
  }
}
