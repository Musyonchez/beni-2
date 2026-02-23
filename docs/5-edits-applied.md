# 5 — Delta Edits Applied to Report (Chapters 3–5)

All edits from `4d-report-edits.md` have been applied. This file records what each
chapter now says accurately, so future sessions do not need to re-read the delta docs.

---

## Chapter 3 — Aims and Objectives (UPDATED)

### 3.2 General Aim
Now reads: system comprises **two deliverables** — native Android app for students and
cafeteria staff, AND a web-based admin panel for system administrators.
Objective 2 paragraph: mentions Next.js admin panel, Supabase Edge Functions (not Cloud
Functions), three user types, admin-controlled menu and staff account creation.

### 3.3 Specific Objective 2
Now reads: "...native Android application for students and cafeteria staff, and a
web-based admin panel for administrators — together providing real-time order placement
and tracking, advance meal scheduling with recurring options, automated wallet deduction
via Supabase Edge Functions, an in-app wallet with staff-managed top-up and deduction,
role-based access for three user types, and admin-controlled menu management and staff
account creation..."

---

## Chapter 4 — Proposed Project (UPDATED)

### 4.2.1 Phase 1 requirements gathering
Now mentions: staff-controlled wallet top-up AND deduction, admin-managed menu and
staff account creation, edge function execution timing (not Cloud Function timing),
architecture has two client apps + Supabase Edge Functions + cron-job.org.

### 4.2.2 Phase 2 description
- **Frontend:** Two clients — Android app (students + staff) + Next.js admin panel
  (TypeScript, Tailwind CSS, Vercel). Staff cannot self-register.
- **Backend:** Firebase Spark plan (no Cloud Functions). Firestore, Auth, FCM only.
- **Scheduled functions:** Supabase Edge Functions (process-cutoff via cron-job.org,
  notify-order-ready called by Android app).
- **Core features:** Three-role access, admin panel for menu + account management.

### 4.3.2 Phase 2 program of work
- Week 5: Firebase + Supabase + Vercel setup (no Blaze plan)
- Week 9: Added admin panel development (login, menu CRUD, staff creation, users directory)
- Week 10: Edge Functions + end-to-end integration (not Cloud Functions)

### 4.5.2 Software requirements
Java (Android), TypeScript (Next.js + Supabase), Android Studio + VS Code, Material
Design 3 + Tailwind CSS, Next.js 14, Firebase Spark, Supabase CLI, cron-job.org, Vercel.
No Cloud Functions. No Node.js Firebase Functions.

### 4.5.3 Server requirements
Firebase Spark (free) — Firestore, Auth, FCM. No Cloud Functions / Blaze plan.
Supabase free — process-cutoff and notify-order-ready edge functions.
cron-job.org free — two scheduled jobs (10 AM and 5 PM EAT).
Vercel free — Next.js admin panel hosting.

### 4.5.4 Budget
KES 0. All services on free tiers: Firebase Spark, Supabase free, cron-job.org free,
Vercel free. No credit card required.

---

## Chapter 5 — System Analysis and Design (UPDATED)

### 5.1.1 Architecture
3-tier with **two clients**:
- Presentation: Android app (students + staff) + Next.js admin panel (admin only)
- Application: Firebase Android SDK + Firebase Admin SDK (Next.js API routes) +
  Supabase Edge Functions (process-cutoff via cron-job.org, notify-order-ready via Android)
- Data: Firestore (5 collections), Firebase Auth (3 roles), FCM (fcmToken field)

### 5.1.2 Use Case Diagram
**Three human actors** (was two):
- Student: Register/Login, Browse, Cart, Order, Track, Pre-order, Wallet history
- Staff (Cashier): Login (no self-register), Change Password on First Login, Update order
  status, Toggle menu availability, Top Up Wallet, **Deduct Wallet Balance**
- Admin (NEW): Login, Change Password on First Login, Create Staff Account, Menu CRUD, View Users
- System: Supabase Edge Functions + cron-job.org (not Cloud Functions + Firestore triggers)

### 5.1.3 ERD
**users** entity updated:
- `deviceToken` → `fcmToken`
- `walletBalance` — students only (absent for staff/admin)
- `firstLogin` (boolean) — staff/admin only, new field
- `role` now has 3 values: student | staff | admin

**walletTransactions** entity updated:
- Added `staffId` field — UID of staff who performed the transaction

Firestore rules: `isStudent()`, `isStaff()`, `isAdmin()`, `isPrivileged()` helpers.

### 5.1.4 DFD
- Level 0: Three external entities (Student, Staff, Admin) + Supabase Edge Function actor
- Level 1, Process 7 (Send Notification): Android app calls notify-order-ready via HTTP
  POST (not Firestore trigger Cloud Function)
- Level 1, Process 9 (Execute Cut-off): Supabase process-cutoff called by cron-job.org
  (not Firebase Cloud Function cron); uses Firebase Admin SDK with preferRest:true
- Level 1, Process 12: Staff wallet operation (credit OR debit), records staffId
- Level 1, Process 13 (NEW): Admin manage users (Next.js API → Firebase Admin SDK)
- Level 2 cut-off: uses cron-job.org trigger + FUNCTIONS_SECRET auth +
  Deno/REST Firebase Admin SDK + recurring next-occurrence creation

### 5.1.8 Class Diagram
- `User` model: `fcmToken` (was `deviceToken`), added `firstLogin`
- `WalletTransaction` model: added `staffId`
- `StaffWalletFragment`: calls `creditWallet()` OR `deductWallet()`
- `FirestoreRepository`: added `notifyOrderReady()` (HTTP POST to Supabase),
  `deductWalletByStaff()` (with staffId)
- Note at bottom: admin panel is TypeScript/React — not represented in Java class diagram

### 5.1.9 Wireframes
Now covers **six interfaces** (was five):
- Android: Menu, Cart, Orders, Pre-orders, Profile/Wallet (unchanged)
- Web Admin Panel (NEW 6th): Admin Login, Menu Management dashboard,
  Staff Account Creation form, Users Directory (two tables)

### 5.2 Functional Requirements
- **FR12** (updated): Staff credit OR debit wallet; each transaction records staffId
- **FR13–15** (NEW): Admin FRs — create staff accounts, menu CRUD, view all users
- **FR16** (was FR13): Supabase process-cutoff (not Firebase Cloud Function)
- **FR17** (was FR14): FCM via Supabase notify-order-ready (not Firestore trigger)
- **FR18** (was FR15): 3-role security rules with isPrivileged() helper
- **FR19** (was FR16): Wallet atomicity via runTransaction()
- **FR20** (was FR17): Audit trail includes staffId on each transaction

### 5.3 Non-Functional Requirements
- **NFR2**: Three-role security rules named; Supabase FUNCTIONS_SECRET; admin SDK in
  Next.js API routes only (not exposed to browser)
- **NFR4**: Supabase Edge Function (not Cloud Function) for cut-off reliability
- **NFR6**: Supabase Edge Function independently deployable/testable via HTTP POST;
  Next.js API routes keep admin SDK credentials off the client
