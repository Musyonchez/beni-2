"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import { collection, query, orderBy, limit, onSnapshot } from "firebase/firestore";
import { db } from "@/lib/firebase";

interface Order {
  orderId: string;
  status: string;
  paymentMethod: string;
  totalAmount: number;
  createdAt: { seconds: number } | null;
}
interface User { role: string; }
interface MenuItem { itemId: string; }

const STATUS_COLORS: Record<string, string> = {
  pending:   "bg-yellow-100 text-yellow-700",
  preparing: "bg-blue-100 text-blue-700",
  ready:     "bg-green-100 text-green-700",
  collected: "bg-gray-100 text-gray-600",
  cancelled: "bg-red-100 text-red-600",
};

function greeting() {
  const h = new Date().getHours();
  return h < 12 ? "Good morning" : h < 17 ? "Good afternoon" : "Good evening";
}

function todayDate() {
  return new Date().toLocaleDateString("en-KE", { weekday: "long", day: "numeric", month: "long", year: "numeric" });
}

function StatCard({ value, label }: { value: string | number; label: string }) {
  return (
    <div className="bg-white rounded-lg shadow-sm p-6">
      <p className="text-3xl font-bold" style={{ color: "#002147" }}>{value}</p>
      <p className="text-sm text-gray-500 mt-1">{label}</p>
    </div>
  );
}

export default function DashboardPage() {
  const [orders, setOrders]       = useState<Order[]>([]);
  const [users, setUsers]         = useState<User[]>([]);
  const [menuCount, setMenuCount] = useState(0);

  useEffect(() => onSnapshot(
    query(collection(db, "orders"), orderBy("createdAt", "desc"), limit(100)),
    snap => setOrders(snap.docs.map(d => ({ orderId: d.id, ...d.data() } as Order)))
  ), []);

  useEffect(() => onSnapshot(
    collection(db, "users"),
    snap => setUsers(snap.docs.map(d => d.data() as User))
  ), []);

  useEffect(() => onSnapshot(
    collection(db, "menuItems"),
    snap => setMenuCount(snap.size)
  ), []);

  const todayMidnightSeconds = (() => {
    const d = new Date(); d.setHours(0, 0, 0, 0); return d.getTime() / 1000;
  })();

  const todayOrders   = orders.filter(o => (o.createdAt?.seconds ?? 0) >= todayMidnightSeconds);
  const pendingCount  = orders.filter(o => o.status === "pending").length;
  const todayRevenue  = todayOrders.reduce((sum, o) => sum + (o.totalAmount ?? 0), 0);
  const studentCount  = users.filter(u => u.role === "student").length;
  const staffCount    = users.filter(u => u.role === "staff" || u.role === "admin").length;
  const recentOrders  = orders.slice(0, 10);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold" style={{ color: "#002147" }}>
          {greeting()}, Admin
        </h1>
        <p className="text-sm text-gray-500 mt-1">{todayDate()}</p>
      </div>

      {/* Stats row 1 */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <StatCard value={todayOrders.length} label="Today's Orders" />
        <StatCard value={pendingCount}        label="Pending" />
        <StatCard value={`KES ${todayRevenue.toFixed(2)}`} label="Today's Revenue" />
        <StatCard value={menuCount}           label="Menu Items" />
      </div>

      {/* Stats row 2 */}
      <div className="grid grid-cols-2 gap-4">
        <StatCard value={studentCount} label="Students" />
        <StatCard value={staffCount}   label="Staff & Admins" />
      </div>

      {/* Recent orders */}
      <div>
        <h2 className="text-base font-semibold mb-3" style={{ color: "#002147" }}>Recent Orders</h2>
        <div className="bg-white rounded-xl shadow overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-left">
              <tr>
                {["Order ID", "Status", "Payment", "Amount", "Time"].map(h => (
                  <th key={h} className="px-4 py-3 font-medium text-gray-600">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y">
              {recentOrders.map(o => (
                <tr key={o.orderId}>
                  <td className="px-4 py-3 font-mono text-xs text-gray-500">{o.orderId.slice(0, 8)}…</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs ${STATUS_COLORS[o.status] ?? "bg-gray-100 text-gray-600"}`}>
                      {o.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 capitalize">{o.paymentMethod}</td>
                  <td className="px-4 py-3">KES {o.totalAmount?.toFixed(2)}</td>
                  <td className="px-4 py-3 text-gray-500">
                    {o.createdAt ? new Date(o.createdAt.seconds * 1000).toLocaleTimeString() : "—"}
                  </td>
                </tr>
              ))}
              {recentOrders.length === 0 && (
                <tr><td colSpan={5} className="px-4 py-6 text-center text-gray-400">No orders yet</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Quick actions */}
      <div className="flex gap-3 flex-wrap">
        {[
          { href: "/dashboard/orders", label: "Manage Orders" },
          { href: "/dashboard/menu",   label: "Manage Menu" },
          { href: "/dashboard/users",  label: "Manage Users" },
        ].map(a => (
          <Link
            key={a.href} href={a.href}
            className="px-4 py-2 rounded-lg text-sm font-medium border transition-colors hover:opacity-80"
            style={{ borderColor: "#002147", color: "#002147" }}
          >
            {a.label}
          </Link>
        ))}
      </div>
    </div>
  );
}
