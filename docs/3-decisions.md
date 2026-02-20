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
| Abstract | ~250 words: problem (queues, no digital ordering), solution (Android app + Firebase), method (3-phase), outcome (working prototype, all tests passed) |
| Table of Contents | Match chapter structure below |

---

## Chapter 1 — Introduction

**History (1.1):**
Trace the evolution of food ordering systems:
- 1950s–1980s: Physical menus, manual queuing, paper tickets in institutional canteens
- 1990s: Digital point-of-sale (POS) terminals in fast food chains
- 2000s: Online ordering websites (Pizza Hut, Domino's)
- 2010s: Mobile apps — McDonald's, Starbucks, Uber Eats
- Present: QR-code menus, self-service kiosks, real-time kitchen display systems (KDS)
- Kenyan context: M-Pesa integration in food delivery (Jumia Food, Glovo Kenya)
- University context: Most Kenyan universities still rely on manual cafeteria queuing

**Problem Statement (1.2):**
- USIU-A cafeteria serves hundreds of students daily with no digital system
- Peak hours (10–11 AM, 1–2 PM) produce queues of 20–40 students
- No menu visibility — students walk to cafeteria only to find items unavailable
- No pre-ordering — preparation only starts when student reaches counter
- Staff cannot anticipate demand — leads to stock shortages mid-rush
- Identified gap: no mobile ordering solution exists for USIU-A or comparable Kenyan university cafeterias

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
- Strength: seamless ID integration, full nutritional data
- Weakness: requires deep institutional IT integration — not replicable cheaply

**2.2.2 NUS uNivUS (National University of Singapore)**
- Features: canteen stall browsing, queue status, digital payment, campus-wide service hub
- Tech: native Android + iOS, NUS SSO login
- Strength: combines ordering + campus services in one app
- Weakness: heavy dependency on NUS IT infrastructure; no offline capability

### Regional Perspective

**2.3.1 UCT Campus Dining / Hungry Rhino (South Africa)**
- Features: web-based pre-ordering for UCT dining halls, menu display, collection slots
- Tech: responsive web app, integrated with UCT student portal
- Strength: addresses the same queue problem; works across devices
- Weakness: web-only (no native mobile); limited real-time updates

**2.3.2 UNILAG Cafeteria System (Nigeria)**
- Features: basic digital menu board, cashless payments via student ID card
- Tech: POS terminal + student card NFC
- Strength: cashless reduces transaction time
- Weakness: no mobile ordering, no pre-ordering, queue problem unaddressed

### Local Perspective

**2.4.1 UoN Cafeteria (Kenya)**
- Fully manual — physical queuing, cash only, chalkboard menu
- No digital component
- Represents the current baseline for Kenyan university cafeterias

**2.4.2 Strathmore University Cafeteria (Kenya)**
- QR-code menu boards (static PDF) introduced post-COVID
- No ordering functionality — display only
- Closest local comparison; still no mobile ordering

### Strengths and Weaknesses Summary Table

| System | Pre-order | Real-time | Mobile | Offline | Local context |
|--------|-----------|-----------|--------|---------|---------------|
| MIT Dining | ✓ | ✓ | ✓ | ✗ | ✗ |
| NUS uNivUS | ✓ | ✓ | ✓ | ✗ | ✗ |
| UCT Hungry Rhino | ✓ | Partial | ✗ | ✗ | Partial |
| UNILAG | ✗ | ✗ | ✗ | ✓ | Partial |
| UoN | ✗ | ✗ | ✗ | ✓ | ✓ |
| Strathmore | ✗ | ✗ | ✗ | ✓ | ✓ |
| **Our system** | **✓** | **✓** | **✓** | **✗** | **✓** |

**Gap identified:** No existing local/regional system offers mobile pre-ordering + real-time order tracking + university-specific context for Kenya.

### Conclusion (2.6)
Summarise the gap and position the proposed system as addressing it.

---

## Chapter 3 — Aims and Objectives

**General Aim:**
To design and develop a mobile cafeteria ordering system for USIU-Africa that reduces queue time, improves menu visibility, and provides cafeteria staff with real-time order management.

**Specific Objectives:**
1. Survey and analyse existing cafeteria and food ordering systems to identify strengths, weaknesses, and the gap in the local university context.
2. Design and develop a native Android application for USIU-A with real-time order placement, Firebase-backed data management, and role-based access for students and staff.
3. Test and evaluate the prototype against the identified problems, measuring order flow reliability, real-time sync performance, and user acceptance.

---

## Chapter 4 — Proposed Project

**Phase 1 — Research, Requirements Gathering & System Design (Weeks 1–4)**
- Literature review of 6 systems
- Requirements elicitation (student + staff interviews)
- System design: architecture, ERD, use case, DFDs, wireframes
- Tech stack finalisation

**Phase 2 — System Development / Implementation (Weeks 5–10)**
- Firebase project setup (Firestore, Auth, FCM)
- Student-side: Menu, Cart, Orders, Profile fragments
- Staff-side: order management, menu availability toggle
- Real-time Firestore listeners + FCM push notifications
- Cart state via shared ViewModel

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
| Software | Android Studio, Firebase Console, Git, GitHub |
| Server | Firebase (Firestore, Auth, FCM, Storage) — free Spark plan sufficient for prototype |
| Budget | Minimal — Firebase free tier, no paid APIs |

---

## Chapter 5 — System Analysis and Design

**Architecture:** 3-tier
- Client: Android app (student + staff)
- Middle: Firebase SDK (auth, Firestore listeners, FCM)
- Backend: Firebase Firestore (NoSQL cloud database)

**Use Case Actors:** Student, Staff

**Key Use Cases:**
- Student: Register/Login, Browse Menu, Add to Cart, Place Order, Track Order, View History, Reorder
- Staff: Login, View Orders, Update Order Status, Toggle Item Availability, View Daily Summary

**ERD — Collections:**
- `users` (uid PK, name, studentId, email, role)
- `menuItems` (id PK, name, category, price, available, imageUrl)
- `orders` (id PK, userId FK, status, pickupTime, createdAt, total)
- `orderItems` (embedded in orders[] array: menuItemId, name, price, quantity)

**DFD Level 0:** Student ↔ App ↔ Firebase ↔ Staff

**DFD Level 1 processes:**
1. Authenticate user
2. Load menu from Firestore
3. Manage cart (add/remove/update)
4. Place order (write to Firestore)
5. Track order (real-time listener)
6. Send FCM notification (on status = Ready)
7. Staff: update order status
8. Staff: toggle item availability

**Flowchart:** Student order flow: Login → Browse → Add to Cart → Checkout → Order Pending → (Staff marks Preparing) → (Staff marks Ready) → FCM notification → Student picks up

**Class Diagram — Key classes:**
- `MenuFragment`, `CartFragment`, `OrdersFragment`, `ProfileFragment`
- `StaffOrdersFragment`, `StaffMenuFragment`
- `MenuAdapter`, `OrderAdapter`, `CartAdapter`
- `MenuItem`, `Order`, `OrderItem`, `User` (model classes)
- `FirestoreRepository` (data layer — all Firestore calls go here)
- `CartViewModel` (shared, survives fragment switches)

**Wireframes (4 screens):**
- Menu: top tab bar (All / Mains / Snacks / Drinks / Specials), item cards (image, name, price, availability chip), FAB to cart
- Cart: item list with +/– quantity, subtotal, pickup time picker, "Place Order" button
- Orders: active order card with 3-step status bar (Pending → Preparing → Ready), scrollable history below
- Profile: avatar, name, student ID, logout; staff panel if role = staff

**Functional Requirements:**
1. Students shall browse the menu filtered by category
2. Students shall add items to cart and adjust quantity
3. Students shall place an order and receive a confirmation
4. Students shall track their order status in real time
5. Students shall receive a push notification when order is ready
6. Staff shall view all incoming orders in real time
7. Staff shall update order status (Preparing / Ready)
8. Staff shall toggle menu item availability

**Non-Functional Requirements:**
1. Menu shall load within 3 seconds on a standard Android device
2. Order status updates shall propagate within 2 seconds
3. App shall support minimum Android API level 24 (Android 7.0)
4. Firebase security rules shall prevent students from accessing other users' orders
5. App shall handle concurrent orders without data conflicts (Firestore transactions)

---

## Chapter 6 — Implementation

**Key implementation points to cover:**

| Component | Detail |
|-----------|--------|
| `MenuFragment` | Firestore `addSnapshotListener` on `menuItems` collection; CategoryAdapter for tab filtering |
| `CartFragment` | Observes `CartViewModel` LiveData; updates total on every item change |
| `CartViewModel` | `MutableLiveData<List<CartItem>>`; `addItem`, `removeItem`, `clearCart` methods |
| `OrdersFragment` | Firestore listener on `orders` where userId == currentUser; maps status to progress bar step |
| `FirestoreRepository` | All Firestore reads/writes centralised here; returns `Task<>` or `LiveData` |
| `FCM` | Cloud Function triggers on order status change to "Ready" → sends FCM to user's device token |
| Firebase Auth | `createUserWithEmailAndPassword` + `signInWithEmailAndPassword`; stores user doc on first login |
| Role check | On login, read `users/{uid}.role`; if "staff" show staff navigation items |
| Firestore rules | Students: read own orders + all menuItems; write own orders only. Staff: read/write all orders + menuItems |

**Deployment:** APK sideloaded on test devices; Firebase project on free Spark plan

---

## Chapter 7 — Testing and Evaluation

**16 test cases (proposed — all should pass):**

| # | Test | Expected | Metric |
|---|------|----------|--------|
| 1 | Menu loads from Firestore | Items display within 3s | < 3s |
| 2 | Category filter | Only matching items shown | Instant |
| 3 | Add to cart | Item appears in cart, total updates | Instant |
| 4 | Quantity increment/decrement | Total recalculates correctly | Instant |
| 5 | Place order | Order doc created in Firestore, status = Pending | < 2s |
| 6 | Order appears on staff screen | Staff sees new order in real time | < 2s |
| 7 | Staff marks Preparing | Student order screen updates | < 2s |
| 8 | Staff marks Ready | FCM notification received by student | < 5s |
| 9 | Order history persists | Past orders visible after app restart | ✓ |
| 10 | Cart clears after order placed | Cart empty post-checkout | ✓ |
| 11 | Student cannot see another student's orders | Firestore rules block access | ✓ |
| 12 | Unavailable item shows correct badge | "Sold Out" chip displayed | ✓ |
| 13 | Staff toggles item unavailable | Menu updates for student in real time | < 2s |
| 14 | Invalid login rejected | Error message shown, no navigation | ✓ |
| 15 | Rapid cart interactions | No crashes, no duplicate items | ✓ |
| 16 | App performance under load | Smooth scroll at 60fps, < 80MB memory | ✓ |

**UAT:** Test with 5 students + 2 cafeteria staff members. Collect feedback on ease of use, speed, and reliability.

---

## Chapter 8 — Conclusion

**Achievements:**
- Fully functional Android cafeteria ordering app
- Real-time order tracking via Firestore listeners
- Role-based access (student / staff) in a single APK
- FCM push notifications for order ready
- Material Design 3 UI with USIU branding

**Challenges (anticipated):**
- Firestore security rules — ensuring students can only read their own orders while staff can read all
- Real-time sync edge cases — order placed at same time staff marks item unavailable
- FCM device token management — refreshing tokens on re-login
- Fragment back-stack management — cart state when navigating back from orders

**Future Work:**
- M-Pesa / card payment integration
- Dietary filters (halal, vegetarian, allergens)
- Kitchen Display System (KDS) — dedicated screen for kitchen staff
- Analytics dashboard — peak hour heatmap, top-selling items
- Ratings and feedback per order
- Multi-cafeteria support (if USIU expands)

---

## Chapter 9 — References (~16 APA sources)

Key sources to cite:

| Source | Use |
|--------|-----|
| Pressman & Maxim (2020) — *Software Engineering: A Practitioner's Approach* | SDLC, testing methodology |
| Sommerville (2016) — *Software Engineering (10th ed.)* | Requirements, design |
| Dennis, Wixom & Tegarden (2020) — *Systems Analysis & Design* | Use case, DFD, ERD |
| Firebase Documentation (Google, 2024) | Firestore, Auth, FCM |
| Android Developers Documentation (Google, 2024) | Fragments, ViewModel, RecyclerView |
| Material Design 3 Guidelines (Google, 2024) | UI/UX design |
| Tan & Chong (2023) — mobile ordering UX study | Literature review |
| MIT Dining Services documentation | Ch 2 — MIT system |
| NUS uNivUS documentation | Ch 2 — NUS system |
| UCT Campus Services documentation | Ch 2 — UCT system |
| Jumia Food Kenya press/reports | Local context |
| Strathmore University website | Ch 2 — local comparison |
| University of Nairobi website | Ch 2 — local comparison |
| GSMA Mobile Economy Sub-Saharan Africa (2023) | Context for mobile adoption in Kenya |
| Statista Android market share Kenya (2024) | Justification for Android platform choice |
| UN SDG 4 / SDG 9 references | Impact analysis in Ch 7 |
