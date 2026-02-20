# Chapter Decisions — USIU Cafeteria Ordering System

This file maps every chapter of `report-own/` to the concrete decisions already made.
When writing the report, consult this file — no decisions left to make.

---

## Chapter -1 — Front Matter

| Section | Decision |
|---------|----------|
| Cover | Title: "Design and Development of a Cafeteria Ordering System for USIU-Africa" |
| Declaration | Standard — original work, partial fulfilment of APT 3065 |
| Acknowledgement | God, supervisor (Prof. Paul Okanda), faculty, classmates, family |
| Abstract | ~250 words: problem (queues, no digital ordering, no advance planning), solution (Android app + Firebase + wallet + pre-order), method (3-phase), outcome (working prototype, all tests passed) |
| Table of Contents | Match chapter structure below |

---

## Chapter 1 — Introduction

**History (1.1):**
Trace the evolution of food ordering systems:
- 1950s–1980s: Physical menus, manual queuing, paper tickets in institutional canteens
- 1990s: Digital point-of-sale (POS) terminals in fast food chains
- 2000s: Online ordering websites (Pizza Hut, Domino's)
- 2010s: Mobile apps — McDonald's, Starbucks, Uber Eats
- Present: QR-code menus, self-service kiosks, real-time kitchen display systems (KDS), scheduled pre-ordering
- Kenyan context: M-Pesa integration in food delivery (Jumia Food, Glovo Kenya); digital payments normalised
- University context: Most Kenyan universities still rely on manual cafeteria queuing with no digital ordering

**Problem Statement (1.2):**
- USIU-A cafeteria serves hundreds of students daily with no digital system
- Peak hours (10–11 AM, 1–2 PM) produce queues of 20–40 students
- No menu visibility — students walk to cafeteria only to find items unavailable
- No pre-ordering — preparation only starts when student reaches counter
- No way to plan meals around a lecture timetable
- Students carry cash for every transaction — inconvenient and slow
- Staff cannot anticipate demand — leads to stock shortages mid-rush
- Identified gap: no mobile ordering solution with advance scheduling or cashless wallet exists for USIU-A or comparable Kenyan university cafeterias

**Conclusion (1.3):**
- Summarise the problem and introduce the proposed mobile solution
- Transition into literature review

---

## Chapter 2 — Literature Review

Six systems reviewed across three scales (mirrors lec's structure exactly):

### Global Perspective

**2.2.1 MIT Dining App (USA)**
- Features: mobile meal plan management, dining hall menus, nutritional info, real-time seat availability
- Tech: iOS + Android, integrated with MIT student ID system
- Strength: seamless ID integration, full nutritional data, pre-paid meal plans
- Weakness: requires deep institutional IT integration — not replicable cheaply; no scheduled pre-ordering; no estimated wait time

**2.2.2 NUS uNivUS (National University of Singapore)**
- Features: canteen stall browsing, queue status, digital payment, campus-wide service hub
- Tech: native Android + iOS, NUS SSO login
- Strength: combines ordering + campus services; digital payment (NUS card)
- Weakness: heavy dependency on NUS IT infrastructure; no recurring meal scheduling; no estimated wait time

### Regional Perspective

**2.3.1 UCT Campus Dining / Hungry Rhino (South Africa)**
- Features: web-based pre-ordering for UCT dining halls, menu display, collection slots
- Tech: responsive web app, integrated with UCT student portal
- Strength: addresses the same queue problem; collection slots reduce peak congestion
- Weakness: web-only (no native mobile); no wallet; no recurring orders; no estimated wait time

**2.3.2 UNILAG Cafeteria System (Nigeria)**
- Features: basic digital menu board, cashless payments via student ID card
- Tech: POS terminal + student card NFC
- Strength: cashless reduces transaction time at counter
- Weakness: no mobile ordering, no pre-ordering, no scheduling, queue problem unaddressed

### Local Perspective

**2.4.1 UoN Cafeteria (Kenya)**
- Fully manual — physical queuing, cash only, chalkboard menu
- No digital component whatsoever
- Represents the current baseline for Kenyan university cafeterias

**2.4.2 Strathmore University Cafeteria (Kenya)**
- QR-code menu boards (static PDF) introduced post-COVID
- No ordering functionality — display only
- Closest local comparison; still no mobile ordering, no wallet, no scheduling

### Strengths and Weaknesses Summary Table

| System | Pre-order | Scheduled | Real-time | Mobile | Wallet | Est. Wait | Local |
|--------|-----------|-----------|-----------|--------|--------|-----------|-------|
| MIT Dining | ✓ | ✗ | ✓ | ✓ | ✗ | ✗ | ✗ |
| NUS uNivUS | ✓ | ✗ | ✓ | ✓ | Partial | ✗ | ✗ |
| UCT Hungry Rhino | ✓ | ✗ | Partial | ✗ | ✗ | ✗ | Partial |
| UNILAG | ✗ | ✗ | ✗ | ✗ | Partial | ✗ | Partial |
| UoN | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | ✓ |
| Strathmore | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | ✓ |
| **Our system** | **✓** | **✓** | **✓** | **✓** | **✓** | **✓** | **✓** |

**Gap identified:** No existing local/regional system offers mobile pre-ordering + recurring meal scheduling + in-app wallet + real-time order tracking + estimated wait time in a single university-specific application.

### Conclusion (2.6)
Summarise the gap and position the proposed system as the first to address all six dimensions in the Kenyan university context.

---

## Chapter 3 — Aims and Objectives

**General Aim:**
To design and develop a mobile cafeteria ordering system for USIU-Africa that eliminates physical queuing, enables advance meal scheduling, and provides cashless payment through an in-app wallet — improving the daily experience for students and giving cafeteria staff real-time demand visibility.

**Specific Objectives:**
1. Survey and analyse existing cafeteria and food ordering systems to identify strengths, weaknesses, and the gap in the local university context.
2. Design and develop a native Android application for USIU-A with real-time order placement, scheduled pre-ordering with auto-deduction, an in-app wallet, and role-based access for students and staff.
3. Test and evaluate the prototype against the identified problems, measuring order flow reliability, pre-order cut-off accuracy, wallet transaction integrity, and real-time sync performance.

---

## Chapter 4 — Proposed Project

**Phase 1 — Research, Requirements Gathering & System Design (Weeks 1–4)**
- Literature review of 6 systems
- Requirements elicitation (student + staff interviews)
- System design: architecture, ERD, use case, DFDs, wireframes
- Tech stack finalisation

**Phase 2 — System Development / Implementation (Weeks 5–10)**
- Firebase project setup (Firestore, Auth, FCM, Cloud Functions)
- Student-side: Menu, Cart, Orders, Pre-orders, Profile/Wallet fragments
- Staff-side: order management, menu availability toggle, wallet top-up
- Real-time Firestore listeners + FCM notifications
- Cart state via shared ViewModel
- Cloud Functions: pre-order cut-off cron jobs (10 AM lunch, 5 PM dinner)
- Wallet deduction logic with Firestore transactions (atomic)

**Phase 3 — Testing, Documentation & Presentation (Weeks 11–13)**
- Unit tests + integration tests
- User Acceptance Testing (UAT) with students and cafeteria staff
- Performance profiling
- Report write-up and presentation

**Gantt Chart:** 13 weeks, Sep–Dec 2025 (same structure as lec's)

**Requirements:**

| Category | Items |
|----------|-------|
| Hardware | Laptop (dev), Android device (testing, min API 24) |
| Software | Android Studio, Firebase Console, Node.js (Cloud Functions), Git, GitHub |
| Server | Firebase (Firestore, Auth, FCM, Cloud Functions) — Blaze plan required for Cloud Functions |
| Budget | Minimal — Firebase Blaze pay-as-you-go (free tier covers prototype scale) |

---

## Chapter 5 — System Analysis and Design

**Architecture:** 3-tier
- Client: Android app (student + staff)
- Middle: Firebase SDK (auth, Firestore listeners, FCM) + Cloud Functions (scheduled jobs)
- Backend: Firebase Firestore (NoSQL cloud database)

**Use Case Actors:** Student, Staff

**Key Use Cases:**
- Student: Register/Login, Browse Menu, Add to Cart, Place Order (wallet/cash), Track Order, View Estimated Wait, View History, Reorder, Schedule Pre-order, Set Recurring Pre-order, Cancel Pre-order, View Wallet Balance, Receive Notifications
- Staff: Login, View Orders, Update Order Status (Preparing/Ready), Toggle Item Availability, Top Up Student Wallet, View Daily Summary

**ERD — Collections:**
- `users` (uid PK, name, studentId, email, role, walletBalance, deviceToken)
- `menuItems` (id PK, name, category, price, available, imageUrl)
- `orders` (id PK, userId FK, items[], status, paymentMethod, total, pickupTime, createdAt)
- `orderItems` (embedded in orders[] array: menuItemId, name, price, quantity)
- `preOrders` (id PK, userId FK, items[], mealType, pickupTime, recurring, dayOfWeek, status, total, createdAt)
- `walletTransactions` (id PK, userId FK, type, amount, description, createdAt)

**DFD Level 0:** Student ↔ App ↔ Firebase ↔ Staff / Cloud Functions

**DFD Level 1 processes:**
1. Authenticate user
2. Load menu from Firestore
3. Manage cart (add/remove/update)
4. Place order (write to Firestore; deduct wallet if wallet payment)
5. Track order (real-time listener)
6. Calculate estimated wait time (count pending/preparing orders × avg prep time)
7. Send FCM notification (on status = Ready)
8. Schedule pre-order (write to preOrders collection)
9. Cut-off job: Cloud Function scans preOrders at 10 AM / 5 PM → deduct wallet → cancel + FCM if insufficient
10. Staff: update order status
11. Staff: toggle item availability
12. Staff: credit student wallet (write to walletTransactions + update users.walletBalance)

**Flowchart A — Regular order:**
Login → Browse → Add to Cart → Choose payment (wallet/cash) → Place Order → Pending → (Staff: Preparing) → (Staff: Ready) → FCM → Student picks up

**Flowchart B — Pre-order:**
Login → Pre-orders tab → Schedule order → Set pickup time + meal type → Set recurring (optional) → Save → [Cut-off time arrives: Cloud Function runs] → Sufficient funds? → Yes: deduct + confirm / No: FCM alert + cancel → Student picks up at scheduled time

**Class Diagram — Key classes:**
- `MenuFragment`, `CartFragment`, `OrdersFragment`, `PreOrdersFragment`, `ProfileFragment`
- `StaffOrdersFragment`, `StaffMenuFragment`, `StaffWalletFragment`
- `MenuAdapter`, `OrderAdapter`, `CartAdapter`, `PreOrderAdapter`
- `MenuItem`, `Order`, `OrderItem`, `PreOrder`, `User`, `WalletTransaction` (model classes)
- `FirestoreRepository` (all Firestore reads/writes centralised here)
- `CartViewModel` (shared, survives fragment switches)
- `WalletViewModel` (balance + transaction history)

**Wireframes (5 screens):**
- Menu: top tab bar (All / Mains / Snacks / Drinks / Specials), item cards (image, name, price, availability chip), FAB to cart
- Cart: item list with +/– quantity, subtotal, payment method toggle (Wallet / Cash), "Place Order" button; wallet shows current balance
- Orders: active order card with 3-step status bar (Pending → Preparing → Ready) + estimated wait time chip; scrollable history below
- Pre-orders: upcoming pre-orders list; FAB to schedule new; each card shows meal, time, recurring badge, status; swipe to cancel
- Profile/Wallet: wallet balance card, recent transactions list, top-up note ("top up at the counter"), student name + ID, logout

**Functional Requirements:**
1. Students shall browse the menu filtered by category
2. Students shall add items to cart and adjust quantity
3. Students shall place an order and choose wallet or cash payment
4. Students shall track their order status in real time
5. Students shall see an estimated wait time on their active order
6. Students shall receive a push notification when their order is ready
7. Students shall schedule a pre-order for a future pickup time
8. Students shall set a pre-order as recurring on a selected day of the week
9. The system shall automatically deduct wallet balance for pre-orders at the meal cut-off time
10. The system shall send an FCM notification and cancel a pre-order if wallet balance is insufficient at cut-off
11. Staff shall view all incoming orders in real time
12. Staff shall update order status (Preparing / Ready)
13. Staff shall toggle menu item availability
14. Staff shall credit a student's wallet after receiving cash

**Non-Functional Requirements:**
1. Menu shall load within 3 seconds on a standard Android device
2. Order status updates shall propagate within 2 seconds
3. Pre-order cut-off Cloud Function shall execute within 60 seconds of the scheduled time
4. Wallet deductions shall be atomic (Firestore transactions — no partial writes)
5. App shall support minimum Android API level 24 (Android 7.0)
6. Firebase security rules shall prevent students from reading other users' orders or wallet data
7. App shall handle concurrent orders without data conflicts

---

## Chapter 6 — Implementation

**Key implementation points to cover:**

| Component | Detail |
|-----------|--------|
| `MenuFragment` | Firestore `addSnapshotListener` on `menuItems`; CategoryAdapter for tab filtering |
| `CartFragment` | Observes `CartViewModel` LiveData; payment toggle updates checkout flow; shows wallet balance |
| `CartViewModel` | `MutableLiveData<List<CartItem>>`; `addItem`, `removeItem`, `clearCart`; paymentMethod state |
| `OrdersFragment` | Firestore listener on `orders` where userId == currentUser; estimated wait = count(Pending+Preparing orders ahead) × 5 min |
| `PreOrdersFragment` | Reads `preOrders` where userId == currentUser, status == scheduled; FAB opens schedule dialog |
| `WalletViewModel` | Reads `users/{uid}.walletBalance`; reads `walletTransactions` where userId == currentUser |
| `FirestoreRepository` | All Firestore reads/writes here; wallet deduction uses `runTransaction()` for atomicity |
| `FCM` | Cloud Function triggers on order status → Ready → sends FCM to `users/{uid}.deviceToken` |
| Cloud Function: `lunchCutoff` | Cron: `0 10 * * *` — queries preOrders (mealType=lunch, status=scheduled, today) → deduct or cancel |
| Cloud Function: `dinnerCutoff` | Cron: `0 17 * * *` — same for dinner |
| Firebase Auth | `createUserWithEmailAndPassword`; stores user doc with walletBalance=0 on first login |
| Role check | On login, read `users/{uid}.role`; staff see additional bottom nav items |
| Firestore rules | Students: read own orders + preOrders + walletTransactions; read all menuItems; write own orders + preOrders. Staff: read/write all orders + preOrders + menuItems + walletTransactions |

**Deployment:** APK sideloaded on test devices; Firebase project on Blaze plan (free tier scale)

---

## Chapter 7 — Testing and Evaluation

**20 test cases (proposed — all should pass):**

| # | Test | Expected | Metric |
|---|------|----------|--------|
| 1 | Menu loads from Firestore | Items display within 3s | < 3s |
| 2 | Category filter | Only matching items shown | Instant |
| 3 | Add to cart | Item appears in cart, total updates | Instant |
| 4 | Quantity increment/decrement | Total recalculates correctly | Instant |
| 5 | Place order (cash) | Order doc created, status = Pending, no wallet change | < 2s |
| 6 | Place order (wallet — sufficient funds) | Order created, walletBalance decremented atomically | < 2s |
| 7 | Place order (wallet — insufficient funds) | Error shown, order not placed, balance unchanged | < 1s |
| 8 | Order appears on staff screen | Staff sees new order in real time | < 2s |
| 9 | Staff marks Preparing | Student order screen updates | < 2s |
| 10 | Staff marks Ready | FCM notification received by student | < 5s |
| 11 | Estimated wait time | Correct count of orders ahead × 5 min displayed | ✓ |
| 12 | Pre-order scheduled | preOrders doc created with correct pickupTime and status=scheduled | < 2s |
| 13 | Recurring pre-order | dayOfWeek field set; future instances created by Cloud Function | ✓ |
| 14 | Pre-order cut-off — sufficient funds | Wallet deducted, status → confirmed, FCM sent | Within 60s of cut-off |
| 15 | Pre-order cut-off — insufficient funds | Status → cancelled, FCM alert sent, balance unchanged | Within 60s of cut-off |
| 16 | Staff tops up wallet | walletBalance incremented, walletTransaction doc created | < 2s |
| 17 | Order history persists | Past orders visible after app restart | ✓ |
| 18 | Student cannot see another student's orders | Firestore rules block access | ✓ |
| 19 | Unavailable item shows correct badge | "Sold Out" chip displayed | ✓ |
| 20 | App performance under load | Smooth scroll at 60fps, < 80MB memory | ✓ |

**UAT:** Test with 5 students + 2 cafeteria staff members. Collect feedback on ease of pre-ordering, wallet clarity, and overall speed vs. physical queuing.

---

## Chapter 8 — Conclusion

**Achievements:**
- Fully functional Android cafeteria ordering app
- Real-time order tracking via Firestore listeners
- Estimated wait time based on live queue depth
- Smart Pre-order system with recurring scheduling and automated cut-off processing
- In-app wallet with atomic deduction and staff-managed top-up
- Role-based access (student / staff) in a single APK
- FCM push notifications for order ready and insufficient funds
- Material Design 3 UI with USIU branding

**Challenges (anticipated):**
- Firestore security rules — students read own data only, staff read all
- Atomic wallet deductions — `runTransaction()` required to prevent race conditions
- Cloud Function timing — Blaze plan required; ensuring cron fires within acceptable window
- Recurring pre-order logic — generating next week's instance after each cut-off
- FCM device token refresh — updating token on re-login

**Future Work:**
- M-Pesa STK push for wallet top-up (remove need for cash top-up at counter)
- Dietary filters (halal, vegetarian, allergens)
- Kitchen Display System (KDS) — dedicated screen for kitchen
- Analytics dashboard — peak hour heatmap, top-selling items
- Ratings and feedback per order
- Multi-cafeteria support

---

## Chapter 9 — References (~16 APA sources)

Key sources to cite:

| Source | Use |
|--------|-----|
| Pressman & Maxim (2020) — *Software Engineering: A Practitioner's Approach* | SDLC, testing methodology |
| Sommerville (2016) — *Software Engineering (10th ed.)* | Requirements, design |
| Dennis, Wixom & Tegarden (2020) — *Systems Analysis & Design* | Use case, DFD, ERD |
| Firebase Documentation (Google, 2024) | Firestore, Auth, FCM, Cloud Functions |
| Android Developers Documentation (Google, 2024) | Fragments, ViewModel, RecyclerView |
| Material Design 3 Guidelines (Google, 2024) | UI/UX design |
| Tan & Chong (2023) — mobile ordering UX study | Literature review |
| MIT Dining Services documentation | Ch 2 — MIT system |
| NUS uNivUS documentation | Ch 2 — NUS system |
| UCT Campus Services documentation | Ch 2 — UCT system |
| Jumia Food Kenya press/reports | Local context, Kenyan mobile ordering |
| Strathmore University website | Ch 2 — local comparison |
| University of Nairobi website | Ch 2 — local comparison |
| GSMA Mobile Economy Sub-Saharan Africa (2023) | Context for mobile adoption in Kenya |
| Statista Android market share Kenya (2024) | Justification for Android platform choice |
| UN SDG 2 / SDG 9 references | Impact analysis in Ch 7 |
