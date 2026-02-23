"use client";
import { useState } from "react";
import { signInWithEmailAndPassword } from "firebase/auth";
import { doc, getDoc } from "firebase/firestore";
import { auth, db } from "@/lib/firebase";
import { useRouter } from "next/navigation";

export default function LoginPage() {
  const [email, setEmail]       = useState("");
  const [password, setPassword] = useState("");
  const [error, setError]       = useState("");
  const [loading, setLoading]   = useState(false);
  const router = useRouter();

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const cred = await signInWithEmailAndPassword(auth, email, password);
      const snap = await getDoc(doc(db, "users", cred.user.uid));
      if (snap.data()?.role !== "admin") {
        await auth.signOut();
        setError("Access denied. Admin accounts only.");
        return;
      }
      router.push("/dashboard/menu");
    } catch {
      setError("Invalid email or password.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="bg-white rounded-xl shadow p-8 w-full max-w-sm">
        <div className="mb-6 text-center">
          <h1 className="text-2xl font-bold" style={{ color: "#002147" }}>USIU Cafeteria</h1>
          <p className="text-gray-500 text-sm mt-1">Admin Panel</p>
        </div>
        <form onSubmit={handleLogin} className="space-y-4">
          <input
            type="email" required placeholder="Email"
            className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2"
            value={email} onChange={e => setEmail(e.target.value)}
          />
          <input
            type="password" required placeholder="Password"
            className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2"
            value={password} onChange={e => setPassword(e.target.value)}
          />
          {error && <p className="text-red-500 text-sm">{error}</p>}
          <button
            type="submit" disabled={loading}
            className="w-full py-2 rounded-lg text-white font-medium disabled:opacity-50"
            style={{ backgroundColor: "#002147" }}
          >
            {loading ? "Signing in..." : "Sign In"}
          </button>
        </form>
      </div>
    </div>
  );
}
