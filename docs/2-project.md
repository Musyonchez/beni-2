# Project Brief — USIU Cafeteria Ordering System
**APT 3065 | Applied Computer Technology | USIU-Africa | Supervisor: Prof. Paul Okanda**

---

## What is it?

A native Android app that lets USIU-Africa students browse the cafeteria menu, place orders from their phone, and pick up at the counter when notified. Cafeteria staff use the same app (different role) to manage the menu and update order status in real time.

---

## The Problem

The USIU-A cafeteria serves hundreds of students daily but has no digital ordering system. Students must physically queue — often during short breaks between lectures — with no idea what is available or how long the wait will be. Cafeteria staff have no advance visibility into demand, leading to stock shortages and preparation delays. The result: long queues, wasted break time, missed meals, and frustrated students and staff.

---

## Tech Stack

| Layer | Choice |
|-------|--------|
| Platform | Android (Java + XML) |
| UI | Material Design 3 — USIU branding (navy #002147, gold #CFB991) |
| Database | Firebase Firestore (real-time, cloud) |
| Auth | Firebase Authentication (email + password) |
| Notifications | Firebase Cloud Messaging (FCM) — order ready alerts |
| Navigation | Fragment-based (hide/show to preserve state) |

---

## Key Features

**Student side:**
- Browse daily menu by category (Mains, Snacks, Drinks, Specials)
- See real-time item availability (available / sold out)
- Add items to cart, set quantity
- Place order and choose pickup time slot
- Real-time order status: Pending → Preparing → Ready
- Push notification when order is ready
- Order history with one-tap reorder

**Staff side (same app, role-based):**
- View incoming orders in real time
- Mark orders: Preparing → Ready
- Toggle menu item availability on/off
- View daily order summary

---

## Design Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Payment | Pay at counter on pickup | Avoids M-Pesa/payment gateway complexity for prototype |
| Storage | Firebase Firestore (cloud) | Orders must reach kitchen — local-first not viable |
| Auth | Firebase email/password | Simple, no OAuth complexity; USIU email convention |
| Staff access | Role flag in Firestore user document | One app, two roles — no separate staff build |
| Offline | Not supported | App is useless without live order sync |
| State | Shared ViewModel for cart | Survives fragment switches without reloading |

---

## Screens (4 main)

1. **Menu** — category tabs, item cards with price + availability badge
2. **Cart** — item list, quantity controls, pickup time picker, place order button
3. **Orders** — active order with live status tracker + past order history
4. **Profile** — student name, ID, logout; staff toggle (if role = staff)

---

## Data Model

| Collection | Key Fields |
|------------|------------|
| `users` | uid, name, studentId, role (student/staff) |
| `menuItems` | id, name, category, price, available (bool), imageUrl |
| `orders` | id, userId, items[], status, pickupTime, createdAt |
| `orderItems` | menuItemId, name, price, quantity |

---

## Chapters at a Glance

| # | Chapter | Core content |
|---|---------|-------------|
| 1 | Introduction | History of food ordering systems; USIU cafeteria problem |
| 2 | Literature Review | 6 systems: MIT Dining, NUS uNivUS, UCT campus dining, UNILAG, UoN, Strathmore |
| 3 | Aims & Objectives | Survey → Design & Build → Test |
| 4 | Proposed Project | 3 phases, 13-week plan, hardware/software requirements |
| 5 | System Analysis & Design | Architecture, use case, ERD, DFDs, flowchart, class diagram, wireframes, FR/NFR |
| 6 | Implementation | MenuFragment, CartFragment, OrdersFragment, FirestoreRepository, FCM |
| 7 | Testing & Evaluation | 16 test cases — order flow, real-time sync, role access, performance |
| 8 | Conclusion | Achievements, challenges, future work |
| 9 | References | ~16 APA sources |

Full chapter-by-chapter decisions → [3-decisions.md](3-decisions.md)
