"use client";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { auth } from "@/lib/firebase";

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router   = useRouter();

  function logout() {
    auth.signOut().then(() => router.push("/login"));
  }

  const links = [
    { href: "/dashboard",        label: "Dashboard", exact: true },
    { href: "/dashboard/menu",   label: "Menu" },
    { href: "/dashboard/orders", label: "Orders" },
    { href: "/dashboard/users",  label: "Users" },
  ];

  return (
    <div className="flex h-screen">
      <aside className="w-48 flex flex-col text-white" style={{ backgroundColor: "#002147" }}>
        <div className="px-4 py-5 border-b border-blue-900">
          <p className="font-bold text-sm">USIU Cafeteria</p>
          <p className="text-xs opacity-60">Admin</p>
        </div>
        <nav className="flex-1 px-2 py-4 space-y-1">
          {links.map(l => {
            const active = l.exact ? pathname === l.href : pathname.startsWith(l.href);
            return (
              <Link
                key={l.href} href={l.href}
                className={`block px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  active ? "text-white" : "opacity-70 hover:opacity-100"
                }`}
                style={active ? { backgroundColor: "#CFB991", color: "#002147" } : {}}
              >
                {l.label}
              </Link>
            );
          })}
        </nav>
        <button
          onClick={logout}
          className="mx-2 mb-4 px-3 py-2 text-sm rounded-lg border border-blue-700 opacity-70 hover:opacity-100 text-left"
        >
          Logout
        </button>
      </aside>
      <main className="flex-1 overflow-y-auto p-6">{children}</main>
    </div>
  );
}
