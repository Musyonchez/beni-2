"use client";
import { useEffect, useState } from "react";
import { collection, query, orderBy, limit, onSnapshot } from "firebase/firestore";
import { db } from "@/lib/firebase";

interface Order { orderId: string; userId: string; status: string; paymentMethod: string; totalAmount: number; createdAt: { seconds: number } | null; }

const STATUS_COLORS: Record<string, string> = {
  pending:   "bg-yellow-100 text-yellow-700",
  confirmed: "bg-blue-100 text-blue-700",
  ready:     "bg-green-100 text-green-700",
  completed: "bg-gray-100 text-gray-600",
  cancelled: "bg-red-100 text-red-600",
};

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);

  useEffect(() => onSnapshot(
    query(collection(db, "orders"), orderBy("createdAt", "desc"), limit(100)),
    snap => setOrders(snap.docs.map(d => ({ orderId: d.id, ...d.data() } as Order)))
  ), []);

  return (
    <div>
      <h1 className="text-xl font-bold mb-4" style={{ color: "#002147" }}>Orders</h1>
      <div className="bg-white rounded-xl shadow overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-left">
            <tr>{["Order ID","Status","Payment","Amount","Date"].map(h => <th key={h} className="px-4 py-3 font-medium text-gray-600">{h}</th>)}</tr>
          </thead>
          <tbody className="divide-y">
            {orders.map(o => (
              <tr key={o.orderId}>
                <td className="px-4 py-3 font-mono text-xs text-gray-500">{o.orderId.slice(0,8)}…</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs ${STATUS_COLORS[o.status] ?? "bg-gray-100"}`}>{o.status}</span>
                </td>
                <td className="px-4 py-3 capitalize">{o.paymentMethod}</td>
                <td className="px-4 py-3">KES {o.totalAmount?.toFixed(2)}</td>
                <td className="px-4 py-3 text-gray-500">{o.createdAt ? new Date(o.createdAt.seconds * 1000).toLocaleString() : "—"}</td>
              </tr>
            ))}
            {orders.length === 0 && <tr><td colSpan={5} className="px-4 py-6 text-center text-gray-400">No orders</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
