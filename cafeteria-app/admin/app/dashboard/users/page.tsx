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

const EMPTY_FORM = { name: "", email: "", password: "" };

export default function UsersPage() {
  const [users, setUsers]     = useState<User[]>([]);
  const [search, setSearch]   = useState("");
  const [form, setForm]       = useState(EMPTY_FORM);
  const [creating, setCreating] = useState(false);
  const [formError, setFormError] = useState("");
  const [formSuccess, setFormSuccess] = useState("");

  useEffect(() => onSnapshot(collection(db, "users"),
    snap => setUsers(snap.docs.map(d => ({ uid: d.id, ...d.data() } as User)))), []);

  async function createStaff(e: React.FormEvent) {
    e.preventDefault();
    setCreating(true); setFormError(""); setFormSuccess("");
    try {
      const res = await fetch("/api/create-staff", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form),
      });
      const data = await res.json();
      if (!res.ok) { setFormError(data.error ?? "Failed to create account"); return; }
      setFormSuccess(`Staff account created for ${form.name}`);
      setForm(EMPTY_FORM);
    } catch {
      setFormError("Network error");
    } finally {
      setCreating(false);
    }
  }

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

      {/* Add Staff Form */}
      <section>
        <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-2">Add Staff Account</h2>
        <form onSubmit={createStaff} className="bg-white rounded-xl shadow p-4 grid grid-cols-3 gap-3">
          <input required placeholder="Full name" className="border rounded-lg px-3 py-2 text-sm"
            value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
          <input required type="email" placeholder="Email" className="border rounded-lg px-3 py-2 text-sm"
            value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))} />
          <input required type="password" placeholder="Temporary password" className="border rounded-lg px-3 py-2 text-sm"
            value={form.password} onChange={e => setForm(f => ({ ...f, password: e.target.value }))} />
          {formError && <p className="col-span-3 text-red-500 text-sm">{formError}</p>}
          {formSuccess && <p className="col-span-3 text-green-600 text-sm">{formSuccess}</p>}
          <button type="submit" disabled={creating}
            className="col-span-3 py-2 rounded-lg text-white text-sm font-medium disabled:opacity-50"
            style={{ backgroundColor: "#002147" }}>
            {creating ? "Creating…" : "Create Staff Account"}
          </button>
        </form>
      </section>

      {/* Staff & Admin */}
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

      {/* Students */}
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
