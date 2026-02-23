"use client";
import { useEffect, useState } from "react";
import { collection, onSnapshot, doc, updateDoc } from "firebase/firestore";
import { db } from "@/lib/firebase";

interface User { uid: string; name: string; email: string; role: string; walletBalance: number; studentId: string; }

export default function UsersPage() {
  const [users, setUsers]     = useState<User[]>([]);
  const [search, setSearch]   = useState("");

  useEffect(() => onSnapshot(collection(db, "users"),
    snap => setUsers(snap.docs.map(d => ({ uid: d.id, ...d.data() } as User)))), []);

  const filtered = users.filter(u =>
    u.name?.toLowerCase().includes(search.toLowerCase()) ||
    u.email?.toLowerCase().includes(search.toLowerCase()) ||
    u.studentId?.includes(search)
  );

  async function toggleRole(u: User) {
    const next = u.role === "admin" ? "student" : u.role === "staff" ? "admin" : "staff";
    if (!confirm(`Change ${u.name}'s role from "${u.role}" to "${next}"?`)) return;
    await updateDoc(doc(db, "users", u.uid), { role: next });
  }

  return (
    <div>
      <h1 className="text-xl font-bold mb-4" style={{ color: "#002147" }}>Users</h1>
      <input className="border rounded-lg px-3 py-2 text-sm mb-4 w-full max-w-xs" placeholder="Search by name, email or student ID"
        value={search} onChange={e => setSearch(e.target.value)} />
      <div className="bg-white rounded-xl shadow overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-left">
            <tr>{["Name","Email","Student ID","Role","Balance",""].map(h => <th key={h} className="px-4 py-3 font-medium text-gray-600">{h}</th>)}</tr>
          </thead>
          <tbody className="divide-y">
            {filtered.map(u => (
              <tr key={u.uid}>
                <td className="px-4 py-3 font-medium">{u.name}</td>
                <td className="px-4 py-3 text-gray-500">{u.email}</td>
                <td className="px-4 py-3 text-gray-500">{u.studentId}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs ${
                    u.role === "admin" ? "bg-purple-100 text-purple-700" :
                    u.role === "staff" ? "bg-blue-100 text-blue-700" :
                    "bg-gray-100 text-gray-600"}`}>{u.role}</span>
                </td>
                <td className="px-4 py-3">KES {u.walletBalance?.toFixed(2) ?? "0.00"}</td>
                <td className="px-4 py-3">
                  <button onClick={() => toggleRole(u)} className="text-xs text-blue-600 hover:underline">Change role</button>
                </td>
              </tr>
            ))}
            {filtered.length === 0 && <tr><td colSpan={6} className="px-4 py-6 text-center text-gray-400">No users</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
