import { initializeApp, getApps, cert, App } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";

function getAdminApp(): App {
  if (getApps().length) return getApps()[0];
  const raw = process.env.FIREBASE_SERVICE_ACCOUNT;
  if (!raw) throw new Error("FIREBASE_SERVICE_ACCOUNT env var not set");
  return initializeApp({ credential: cert(JSON.parse(raw)) });
}

export const adminAuth = getAuth(getAdminApp());
export const adminDb   = getFirestore(getAdminApp());
