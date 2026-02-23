# Report Edit Guide — Chapters 1–5

Use this doc alongside the delta docs (4a, 4b, 4c) to correct report-own/.
Read each section: it tells you what is currently there and exactly what to change.

---

## Chapter 1 — Introduction

**No substantive changes needed.**

The problem statement, history of food ordering systems, and conclusion are all still accurate.
The solution described (Android app + Firebase + wallet + pre-order) is still correct.

Optional minor addition in 1.3 (Conclusion): if the existing text mentions only an "Android app",
you may add "...and a web-based admin panel" to reflect the full system scope. Not strictly
required.

---

## Chapter 2 — Literature Review

**No changes needed.**

The six systems reviewed, the comparison table, and the identified gap are all unchanged.

---

## Chapter 3 — Aims and Objectives

### General Aim

**Currently says (approximately):**
> To design and develop a mobile cafeteria ordering system for USIU-Africa that eliminates
> physical queuing, enables advance meal scheduling, and provides cashless payment through an
> in-app wallet — improving the daily experience for students and giving cafeteria staff
> real-time demand visibility.

**Change to:**
> To design and develop a mobile cafeteria ordering system for USIU-Africa comprising a native
> Android application for students and cafeteria staff, and a web-based admin panel for
> system administrators — eliminating physical queuing, enabling advance meal scheduling, and
> providing cashless payment through an in-app wallet, while improving the daily experience
> for students and giving cafeteria staff real-time demand visibility.

### Objective 2

**Currently says (approximately):**
> Design and develop a native Android application for USIU-A with real-time order placement,
> scheduled pre-ordering with auto-deduction, an in-app wallet, and role-based access for
> students and staff.

**Change to:**
> Design and develop a native Android application for students and cafeteria staff, and a
> web-based admin panel for administrators — together supporting real-time order placement,
> scheduled pre-ordering with auto-deduction, an in-app wallet, role-based access for three
> user types, and admin-managed menu and account control.

---

## Chapter 4 — Proposed Project

### Requirements table — Server row

**Currently says:**
> Firebase (Firestore, Auth, FCM, Cloud Functions) — Blaze plan required for Cloud Functions

**Change to:**
> Firebase (Firestore, Auth, FCM) — Spark (free) plan; Supabase free plan for Edge Functions;
> cron-job.org for scheduled pre-order cut-off jobs; Vercel for admin web panel hosting

### Requirements table — Software row

**Currently lists:** Android Studio, Firebase Console, Node.js (Cloud Functions), Git, GitHub

**Change to:** Android Studio, Firebase Console, Node.js, Next.js, Supabase CLI, Git, GitHub

### Requirements table — Budget row

**Currently says:** "Firebase Blaze pay-as-you-go (free tier covers prototype scale)"

**Change to:** "Minimal — all services on free tiers: Firebase Spark, Supabase free,
cron-job.org free, Vercel free. No billing account or credit card required."

### Phase 2 description (Cloud Functions item)

**Currently says:** "Cloud Functions: pre-order cut-off cron jobs (10 AM lunch, 5 PM dinner)"

**Change to:** "Supabase Edge Functions: pre-order cut-off HTTP endpoints called by cron-job.org
at 10 AM (lunch) and 5 PM (dinner) EAT; notify-order-ready endpoint called by Android on status
change"

### Phase 2 — add item for admin panel

After the existing Firebase/fragments items, add:
> Admin web panel (Next.js + TypeScript): menu CRUD, staff account creation, user management;
> deployed to Vercel

### Gantt chart (if editable)

Week 10 label "Cloud Functions & End-to-End Integration" →
"Supabase Edge Functions & End-to-End Integration"

---

## Chapter 5 — System Analysis and Design

### Architecture

**Currently describes:** 3-tier — Android client / Firebase SDK + Cloud Functions / Firestore

**Change to:** 3-tier — **two clients** (Android app + Next.js web admin panel) / Firebase SDK +
Supabase Edge Functions / Firebase Firestore

Update any architecture diagram description to mention:
- Android app serves students and cafeteria staff
- Next.js admin panel serves administrators
- Supabase Edge Functions replace Cloud Functions for scheduled jobs and notifications

### Use Case Actors

**Currently:** Student, Staff

**Change to:** Student, Staff (Cashier), Admin

### Use Cases — Admin (new section to add)

Add these use cases for Admin actor:
- Log in to web admin panel
- Create staff account (sets temporary password; staff must change on first login)
- Add / edit / delete menu items
- Toggle menu item availability
- View all users (students and staff)

### Use Cases — Staff (remove one, keep rest)

Remove: "Register account" — staff no longer self-register via Android app.
Keep all operational use cases (view orders, update status, top-up wallet, etc.).

Add: "Deduct student wallet balance" (see 4c-schema.md — wallet deduction feature).

### ERD — users collection

Update the users row to reflect the actual schema (see 4c-schema.md):

| Field | Type | Notes |
|---|---|---|
| uid | string | PK |
| name | string | |
| studentId | string | students only |
| email | string | |
| role | string | "student" / "staff" / "admin" |
| walletBalance | number | students only |
| fcmToken | string | was "deviceToken" in plan |
| firstLogin | boolean | staff/admin only |
| createdAt | timestamp | |

### ERD — walletTransactions collection

Add `staffId: string` field — records which staff member performed the transaction.

### DFD Level 1 — processes 7 and 9

Process 7 (FCM notification):
**Old:** "Send FCM notification (Cloud Function triggers on status = Ready)"
**New:** "Send FCM notification (Android calls Supabase Edge Function `notify-order-ready`
after writing status = Ready to Firestore; edge function fetches FCM token and sends message)"

Process 9 (cut-off job):
**Old:** "Cut-off job: Cloud Function scans preOrders at 10 AM / 5 PM"
**New:** "Cut-off job: Supabase Edge Function `process-cutoff` called by cron-job.org at
10 AM and 5 PM EAT → deducts wallet or cancels pre-order + FCM"

### Functional Requirements

Update existing:

| FR | Currently says | Change to |
|---|---|---|
| FR 9 (cut-off deduction) | "The system shall automatically deduct..." | Keep text; change footnote/tech note from "Cloud Function" to "Supabase Edge Function" |
| FR 10 (insufficient funds) | "The system shall send an FCM notification..." | Keep text; same Cloud Function → Edge Function note |
| FR 14 (staff wallet) | "Staff shall credit a student's wallet after receiving cash" | "Staff shall credit or deduct a student's wallet balance; each transaction records the staff member's ID" |

Add new FRs:

- FR 15: Admin shall create staff accounts via the web admin panel; staff must change their
  temporary password on first login
- FR 16: Admin shall add, edit, and delete menu items and toggle item availability via the
  web admin panel

### Non-Functional Requirements

NFR 3:
**Old:** "Pre-order cut-off Cloud Function shall execute within 60 seconds of the scheduled time"
**New:** "Pre-order cut-off edge function (Supabase) shall execute within 60 seconds of the
scheduled time; cron-job.org triggers the HTTP endpoint at 10 AM and 5 PM EAT"

### Class Diagram

No Java classes exist for the admin web panel (it's TypeScript/React).
Add a note: "A separate web admin panel (Next.js, TypeScript) handles administrator functions
including menu management and staff account creation. Its components are not represented in this
Java class diagram."

The Android class diagram itself is unchanged from the original plan.

### Wireframes

Add a note that a **sixth interface** exists — the admin web panel — covering:
- Login page
- Dashboard: Menu management (table + form)
- Dashboard: Users (students table + staff/admin table)
- Dashboard: Create staff account form

These are web browser screens, not Android wireframes.

---

## Summary: minimal vs thorough edits

If you want **minimal** corrections (enough to be accurate):
- Ch 3: Update Aim + Objective 2 (3 sentences)
- Ch 4: Fix requirements table (server, software, budget rows) + Phase 2 Cloud Functions item
- Ch 5: Fix architecture description, actors, ERD (users + walletTransactions), DFD processes
  7+9, FR 14 update, add FR 15+16, NFR 3 fix

If you want **thorough** corrections:
- All of the above plus class diagram note, wireframes note, and admin use cases section
