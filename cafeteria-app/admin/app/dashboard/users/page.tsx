"use client";
import { useEffect, useState } from "react";
import { collection, onSnapshot, doc, updateDoc } from "firebase/firestore";
import { db } from "@/lib/firebase";

interface User { uid: string; name: string; email: string; role: string; walletBalance: number; studentId?: string; }

const ROLE_BADGE: Record<string, string> = {
  admin:   "bg-purple-100 text-purple-700",
  staff:   "bg-blue-100 text-blue-700",
  student: "bg-gray-100 text-gray-600",
};

function RoleBadge({ role }: { role: string }) {
  return <span className={`px-2 py-0.5 rounded-full text-xs ${ROLE_BADGE[role] ?? "bg-gray-100 text-gray-600"}`}>{role}</span>;
}

export default function UsersPage() {
  const [users, setUsers]   = useState<User[]>([]);
  const [search, setSearch] = useState("");

  useEffect(() => onSnapshot(collection(db, "users"),
    snap => setUsers(snap.docs.map(d => ({ uid: d.id, ...d.data() } as User)))), []);

  async function toggleRole(u: User) {
    const next = u.role === "admin" ? "staff" : u.role === "staff" ? "admin" : "staff";
    if (!confirm(`Change ${u.name}'s role from "${u.role}" to "${next}"?`)) return;
    await updateDoc(doc(db, "users", u.uid), { role: next });
  }

  const students = users.filter(u => u.role === "student" &&
    (u.name?.toLowerCase().includes(search.toLowerCase()) ||
     u.email?.toLowerCase().includes(search.toLowerCase()) ||
     u.studentId?.includes(search)));

  const staff = users.filter(u => u.role !== "student" &&
    (u.name?.toLowerCase().includes(search.toLowerCase()) ||
     u.email?.toLowerCase().includes(search.toLowerCase())));

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold" style={{ color: "#002147" }}>Users</h1>
        <input
          className="border rounded-lg px-3 py-2 text-sm w-64"
          placeholder="Search name, email or student ID"
          value={search} onChange={e => setSearch(e.target.value)}
        />
      </div>

      {/* Staff & Admin — no wallet balance, no student ID */}
      <section>
        <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-2">
          Staff &amp; Admin <span className="ml-1 text-gray-400">({staff.length})</span>
        </h2>
        <div className="bg-white rounded-xl shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-left">
              <tr>{["Name","Email","Role",""].map(h => <th key={h} className="px-4 py-3 font-medium text-gray-600">{h}</th>)}</tr>
            </thead>
            <tbody className="divide-y">
              {staff.map(u => (
                <tr key={u.uid}>
                  <td className="px-4 py-3 font-medium">{u.name}</td>
                  <td className="px-4 py-3 text-gray-500">{u.email}</td>
                  <td className="px-4 py-3"><RoleBadge role={u.role} /></td>
                  <td className="px-4 py-3">
                    <button onClick={() => toggleRole(u)} className="text-xs text-blue-600 hover:underline">
                      {u.role === "admin" ? "Demote to staff" : "Promote to admin"}
                    </button>
                  </td>
                </tr>
              ))}
              {staff.length === 0 && <tr><td colSpan={4} className="px-4 py-6 text-center text-gray-400">No staff accounts</td></tr>}
            </tbody>
          </table>
        </div>
      </section>

      {/* Students — student ID + wallet balance */}
      <section>
        <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-2">
          Students <span className="ml-1 text-gray-400">({students.length})</span>
        </h2>
        <div className="bg-white rounded-xl shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-left">
              <tr>{["Name","Email","Student ID","Wallet Balance",""].map(h => <th key={h} className="px-4 py-3 font-medium text-gray-600">{h}</th>)}</tr>
            </thead>
            <tbody className="divide-y">
              {students.map(u => (
                <tr key={u.uid}>
                  <td className="px-4 py-3 font-medium">{u.name}</td>
                  <td className="px-4 py-3 text-gray-500">{u.email}</td>
                  <td className="px-4 py-3 font-mono text-gray-500">{u.studentId ?? "—"}</td>
                  <td className="px-4 py-3">KES {u.walletBalance?.toFixed(2) ?? "0.00"}</td>
                  <td className="px-4 py-3">
                    <button onClick={() => toggleRole(u)} className="text-xs text-blue-600 hover:underline">Make staff</button>
                  </td>
                </tr>
              ))}
              {students.length === 0 && <tr><td colSpan={5} className="px-4 py-6 text-center text-gray-400">No students</td></tr>}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
