"use client";
import { useEffect, useState } from "react";
import { collection, onSnapshot, doc, setDoc, updateDoc, deleteDoc, Timestamp } from "firebase/firestore";
import { db } from "@/lib/firebase";

interface MenuItem { itemId: string; name: string; description: string; price: number; category: string; imageUrl: string; available: boolean; }
const CATEGORIES = ["Breakfast", "Lunch", "Dinner"];
const EMPTY: Omit<MenuItem, "itemId"> = { name: "", description: "", price: 0, category: "Lunch", imageUrl: "", available: true };

export default function MenuPage() {
  const [items, setItems]     = useState<MenuItem[]>([]);
  const [form, setForm]       = useState(EMPTY);
  const [editing, setEditing] = useState<string | null>(null);
  const [filter, setFilter]   = useState("all");

  useEffect(() => onSnapshot(collection(db, "menuItems"), snap =>
    setItems(snap.docs.map(d => ({ itemId: d.id, ...d.data() } as MenuItem)))), []);

  const filtered = filter === "all" ? items : items.filter(i => i.category.toLowerCase() === filter);

  async function save() {
    if (!form.name.trim()) return;
    if (editing) {
      await updateDoc(doc(db, "menuItems", editing), { ...form });
    } else {
      const ref = doc(collection(db, "menuItems"));
      await setDoc(ref, { ...form, itemId: ref.id, createdAt: Timestamp.now() });
    }
    setForm(EMPTY); setEditing(null);
  }

  function startEdit(item: MenuItem) {
    setEditing(item.itemId);
    setForm({ name: item.name, description: item.description, price: item.price, category: item.category, imageUrl: item.imageUrl ?? "", available: item.available });
  }

  async function remove(id: string) {
    if (!confirm("Delete this item?")) return;
    await deleteDoc(doc(db, "menuItems", id));
  }

  return (
    <div>
      <h1 className="text-xl font-bold mb-4" style={{ color: "#002147" }}>Menu Management</h1>

      {/* Form */}
      <div className="bg-white rounded-xl shadow p-4 mb-6 grid grid-cols-2 gap-3">
        <input className="border rounded-lg px-3 py-2 text-sm col-span-2" placeholder="Item name *"
          value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
        <input className="border rounded-lg px-3 py-2 text-sm col-span-2" placeholder="Description"
          value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
        <input className="border rounded-lg px-3 py-2 text-sm col-span-2" placeholder="Image URL (Unsplash)"
          value={form.imageUrl} onChange={e => setForm(f => ({ ...f, imageUrl: e.target.value }))} />
        <input type="number" className="border rounded-lg px-3 py-2 text-sm" placeholder="Price (KES)"
          value={form.price} onChange={e => setForm(f => ({ ...f, price: parseFloat(e.target.value) || 0 }))} />
        <select className="border rounded-lg px-3 py-2 text-sm"
          value={form.category} onChange={e => setForm(f => ({ ...f, category: e.target.value }))}>
          {CATEGORIES.map(c => <option key={c}>{c}</option>)}
        </select>
        <label className="flex items-center gap-2 text-sm col-span-2">
          <input type="checkbox" checked={form.available} onChange={e => setForm(f => ({ ...f, available: e.target.checked }))} />
          Available
        </label>
        <div className="col-span-2 flex gap-2">
          <button onClick={save} className="px-4 py-2 rounded-lg text-white text-sm font-medium" style={{ backgroundColor: "#002147" }}>
            {editing ? "Update" : "Add Item"}
          </button>
          {editing && <button onClick={() => { setForm(EMPTY); setEditing(null); }} className="px-4 py-2 rounded-lg border text-sm">Cancel</button>}
        </div>
      </div>

      {/* Filter */}
      <div className="flex gap-2 mb-3">
        {["all", "breakfast", "lunch", "dinner"].map(c => (
          <button key={c} onClick={() => setFilter(c)}
            className={`px-3 py-1 rounded-full text-xs font-medium border ${filter === c ? "text-white border-transparent" : "border-gray-300"}`}
            style={filter === c ? { backgroundColor: "#002147" } : {}}>
            {c.charAt(0).toUpperCase() + c.slice(1)}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-left">
            <tr>{["Name","Category","Price","Available",""].map(h => <th key={h} className="px-4 py-3 font-medium text-gray-600">{h}</th>)}</tr>
          </thead>
          <tbody className="divide-y">
            {filtered.map(item => (
              <tr key={item.itemId}>
                <td className="px-4 py-3 font-medium">{item.name}</td>
                <td className="px-4 py-3 text-gray-500">{item.category}</td>
                <td className="px-4 py-3">KES {item.price.toFixed(2)}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs ${item.available ? "bg-green-100 text-green-700" : "bg-red-100 text-red-600"}`}>
                    {item.available ? "Yes" : "No"}
                  </span>
                </td>
                <td className="px-4 py-3 flex gap-2">
                  <button onClick={() => startEdit(item)} className="text-blue-600 hover:underline text-xs">Edit</button>
                  <button onClick={() => remove(item.itemId)} className="text-red-500 hover:underline text-xs">Delete</button>
                </td>
              </tr>
            ))}
            {filtered.length === 0 && <tr><td colSpan={5} className="px-4 py-6 text-center text-gray-400">No items</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
