# Change 2 — Three Roles + Next.js Admin Web Panel

## What the original plan said

- **Two roles only**: student and staff
- Both roles use the **same Android APK** — a flag (`role` field in Firestore) controls which
  navigation they see
- Staff register the same way as students (via Android `RegisterActivity`) and are manually
  promoted in Firestore
- No separate admin concept; no web panel

## What was actually built

Three roles across two applications:

### Role: student
- Registers and logs in via the **Android app** (`RegisterActivity`)
- Self-service: fills name, student ID, email, password
- No approval required — account created immediately in Firebase Auth + Firestore

### Role: staff (cashier)
- Logs in via the **Android app** (`LoginActivity`)
- **Cannot self-register** — account must be created by an admin via the web panel
- On first login, forced to change their password (`ChangePasswordActivity`) before they can
  access the staff order screens
- Capabilities inside Android: view orders, mark Preparing/Ready, top-up student wallet,
  manually deduct student wallet, toggle menu item availability

### Role: admin
- Logs in via the **Next.js web admin panel** (separate app: `cafeteria-app/admin/`)
- On first login, also forced to change password (same `firstLogin` mechanism, web version)
- Capabilities:
  - Create staff accounts (sets `role: "staff"`, `firstLogin: true`)
  - Add / edit / delete menu items (including image URL, category, price, availability)
  - View all users: separate tables for Students and Staff/Admin
  - Seed / manage initial menu data
- Admin accounts are created manually in Firestore + Firebase Auth by the project owner
  (no "create admin" UI — would be a security risk)

## Tech stack for admin web panel

| Layer | Choice |
|---|---|
| Framework | Next.js 14 (App Router) |
| Language | TypeScript |
| Styling | Tailwind CSS v4 |
| Auth | Firebase Client SDK (`signInWithEmailAndPassword`) |
| Data | Firebase Admin SDK (API routes) + Firebase Client SDK (client components) |
| Hosting | Vercel (free) or local dev |

## Key files (admin panel)

```
cafeteria-app/admin/
  app/
    login/page.tsx               — admin login with role + firstLogin check
    change-password/page.tsx     — forced first-login password change
    dashboard/
      menu/page.tsx              — menu CRUD
      users/page.tsx             — staff/admin + students tables
      staff/page.tsx             — create staff accounts
  api/
    create-staff/route.ts        — Firebase Admin SDK: creates Auth user + Firestore doc
  lib/
    firebase.ts                  — client-side Firebase init
    firebase-admin.ts            — server-side Admin SDK init
```

## Commits that captured this change

- `4a70435f feat: admin/cashier split — Next.js admin + Android cashier-only`
- `6becd07  feat: split Users page into Staff/Admin and Students tables`
- `0ffeb5e  feat: staff accounts created by admin only (not via Android app)`
- `69854b4  feat: force password change on first login for staff and admin`
- `c524c2b  fix: allow admin role to read/write all Firestore collections`

## Impact on report chapters

| Chapter | Section | What to change |
|---|---|---|
| Ch 3 | General Aim | Add "...and a web-based admin panel for system administrators" |
| Ch 3 | Objective 2 | Change "native Android application" → "native Android application for students and cafeteria staff, and a web-based admin panel for administrators" |
| Ch 4 | Phase 2 | Add "Admin web panel (Next.js)" as a development item |
| Ch 4 | Requirements — Software | Add: Node.js, Next.js, Vercel |
| Ch 4 | Requirements — Server | Note Blaze plan not needed; admin panel hosted on Vercel free |
| Ch 5 | Architecture | 3-tier now has **two clients**: Android app + Next.js web panel |
| Ch 5 | Use Case Actors | Add **Admin** as a third actor |
| Ch 5 | Use Cases — Admin | Add: Create staff account, Manage full menu (CRUD), View all users |
| Ch 5 | Use Cases — Staff | Remove: "register" (staff cannot self-register); keep all operational use cases |
| Ch 5 | Class diagram | Note existence of admin web panel as a separate system (no Java classes for it) |
| Ch 5 | FR list | Add FR: "Admin shall create staff accounts via the web panel"; "Admin shall manage the menu via the web panel" |
