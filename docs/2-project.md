# Project Brief — USIU Cafeteria Ordering System
**APT 3065 | Applied Computer Technology | USIU-Africa | Supervisor: Prof. Paul Okanda**

---

## What is it?

A native Android app that lets USIU-Africa students browse the cafeteria menu, place orders from their phone, and pick up at the counter when notified. Students hold a wallet balance in the app and can schedule recurring advance orders for fixed meal times. Cafeteria staff use the same app (different role) to manage the menu, update order status, and top up student wallets in real time.

---

## The Problem

The USIU-A cafeteria serves hundreds of students daily but has no digital ordering system. Students must physically queue — often during short breaks between lectures — with no idea what is available or how long the wait will be. Cafeteria staff have no advance visibility into demand, leading to stock shortages and preparation delays. Students also have no way to pre-arrange meals around their timetable. The result: long queues, wasted break time, missed meals, and frustrated students and staff.

---

## Tech Stack

| Layer | Choice |
|-------|--------|
| Platform | Android (Java + XML) |
| UI | Material Design 3 — USIU branding (navy #002147, gold #CFB991) |
| Database | Firebase Firestore (real-time, cloud) |
| Auth | Firebase Authentication (email + password) |
| Notifications | Firebase Cloud Messaging (FCM) — order ready + low funds alerts |
| Scheduled jobs | Firebase Cloud Functions (cron triggers for pre-order cut-offs) |
| Navigation | Fragment-based (hide/show to preserve state) |

---

## Key Features

**Student side:**
- Browse daily menu by category (Mains, Snacks, Drinks, Specials)
- See real-time item availability (available / sold out)
- Add items to cart, set quantity
- Place order — pay from wallet or pay cash at counter
- Real-time order status: Pending → Preparing → Ready
- Estimated wait time shown on order screen (~X min, based on queue depth)
- Push notification when order is ready
- Order history with one-tap reorder
- In-app wallet — hold balance, no need to pay per order
- Smart Pre-order — schedule a meal for a future pickup time
  - Recurring option (e.g. every Thursday 12:30)
  - Cut-off: 10:00 AM for lunch, 5:00 PM for dinner (breakfast: no cut-off)
  - At cut-off, wallet is charged automatically; if insufficient → FCM alert + reservation cancelled
  - Pre-orders are wallet only — non-refundable if student does not pick up

**Staff side (same app, role-based):**
- View incoming orders in real time
- Mark orders: Preparing → Ready
- Toggle menu item availability on/off
- Top up a student's wallet (cash received at counter → staff credits account)
- View daily order summary

---

## Design Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Payment — regular order | Wallet or cash at counter | Flexibility; no payment API needed |
| Payment — pre-order | Wallet only | Commitment mechanism; cafeteria prepares food, no-show = no refund |
| Wallet top-up | Cash at counter → staff credits manually | Avoids M-Pesa/payment gateway complexity |
| Storage | Firebase Firestore (cloud) | Orders must reach kitchen — local-first not viable |
| Auth | Firebase email/password | Simple, no OAuth complexity; USIU email convention |
| Staff access | Role flag in Firestore user document | One app, two roles — no separate staff build |
| Offline | Not supported | App is useless without live order sync |
| State | Shared ViewModel for cart | Survives fragment switches without reloading |
| Pre-order cut-off jobs | Firebase Cloud Functions (scheduled) | Server-side — cannot rely on device being open |

---

## Screens (5 main)

1. **Menu** — category tabs, item cards with price + availability badge
2. **Cart** — item list, quantity controls, payment method selector (wallet/cash), place order button
3. **Orders** — active order with live status tracker + estimated wait time + past order history
4. **Pre-orders** — schedule new pre-order, set recurring, view/cancel upcoming pre-orders
5. **Profile / Wallet** — wallet balance, top-up history, student info, logout; staff panel if role = staff

---

## Data Model

| Collection | Key Fields |
|------------|------------|
| `users` | uid, name, studentId, role, walletBalance, deviceToken |
| `menuItems` | id, name, category, price, available (bool), imageUrl |
| `orders` | id, userId, items[], status, paymentMethod, total, pickupTime, createdAt |
| `orderItems` | menuItemId, name, price, quantity (embedded in orders[]) |
| `preOrders` | id, userId, items[], mealType, pickupTime, recurring, dayOfWeek, status, total, createdAt |
| `walletTransactions` | id, userId, type (credit/debit), amount, description, createdAt |

---

## Chapters at a Glance

| # | Chapter | Core content |
|---|---------|-------------|
| 1 | Introduction | History of food ordering systems; USIU cafeteria problem |
| 2 | Literature Review | 6 systems: MIT Dining, NUS uNivUS, UCT campus dining, UNILAG, UoN, Strathmore |
| 3 | Aims & Objectives | Survey → Design & Build → Test |
| 4 | Proposed Project | 3 phases, 13-week plan, hardware/software/server requirements |
| 5 | System Analysis & Design | Architecture, use case, ERD, DFDs, flowchart, class diagram, wireframes, FR/NFR |
| 6 | Implementation | Fragments, ViewModel, FirestoreRepository, Cloud Functions, wallet logic |
| 7 | Testing & Evaluation | 20 test cases — order flow, pre-order cut-off, wallet, real-time sync, performance |
| 8 | Conclusion | Achievements, challenges, future work |
| 9 | References | ~16 APA sources |

Full chapter-by-chapter decisions → [3-decisions.md](3-decisions.md)
