"use client";
import { useState } from "react";
import { updatePassword } from "firebase/auth";
import { doc, updateDoc } from "firebase/firestore";
import { auth, db } from "@/lib/firebase";
import { useRouter } from "next/navigation";

export default function ChangePasswordPage() {
  const [newPassword, setNewPassword]     = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError]   = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (newPassword.length < 6) { setError("Password must be at least 6 characters."); return; }
    if (newPassword !== confirmPassword) { setError("Passwords do not match."); return; }

    setLoading(true);
    setError("");
    try {
      const user = auth.currentUser;
      if (!user) { router.push("/login"); return; }
      await updatePassword(user, newPassword);
      await updateDoc(doc(db, "users", user.uid), { firstLogin: false });
      router.push("/dashboard/menu");
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Failed to update password.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="bg-white rounded-xl shadow p-8 w-full max-w-sm">
        <div className="mb-6 text-center">
          <h1 className="text-xl font-bold" style={{ color: "#002147" }}>Set New Password</h1>
          <p className="text-gray-500 text-sm mt-1">
            This is your first login. Please set a new password to continue.
          </p>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="password" required placeholder="New password"
            className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2"
            value={newPassword} onChange={e => setNewPassword(e.target.value)}
          />
          <input
            type="password" required placeholder="Confirm new password"
            className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2"
            value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)}
          />
          {error && <p className="text-red-500 text-sm">{error}</p>}
          <button
            type="submit" disabled={loading}
            className="w-full py-2 rounded-lg text-white font-medium disabled:opacity-50"
            style={{ backgroundColor: "#002147" }}
          >
            {loading ? "Saving…" : "Set Password & Continue"}
          </button>
        </form>
      </div>
    </div>
  );
}
