# USIU Cafeteria Ordering System — Presentation Slides
# Copy each slide block into PowerPoint as a new slide.

---

## SLIDE 1 — TITLE

**TITLE:**
Design and Development of a Cafeteria Ordering System for USIU-Africa

**SUBTITLE:**
APT 3065 Final Year Project Presentation

**BODY:**
- Student: Ishimwe Beni | ID: 669396
- Supervisor: Prof. Paul Okanda
- School of Science and Technology
- April 2026

---

## SLIDE 2 — CHAPTER 1: INTRODUCTION

**TITLE:**
Chapter 1: Introduction

**BODY:**
- USIU-Africa cafeteria serves hundreds of students daily with no digital ordering system
- Peak-hour queues of 20–40 students during short breaks between lectures
- Five core problems identified:
  1. Physical queuing exceeds available break time
  2. No menu visibility before arrival
  3. No pre-ordering capability
  4. No advance demand visibility for staff
  5. Cash-only transactions add friction for all parties
- **Solution proposed:** Native Android ordering app with real-time tracking, in-app wallet, and smart pre-orders

---

## SLIDE 3 — CHAPTER 2: LITERATURE REVIEW

**TITLE:**
Chapter 2: Literature Review

**BODY:**
Six systems reviewed across three levels:

| System | Level | Key Feature | Gap |
|---|---|---|---|
| UMass Dining (USA) | Global | Menu visibility, nutritional info | No advance ordering |
| NUSmart Dining (Singapore) | Global | Order tracking, favourites | No recurring pre-orders |
| UCT Dining (South Africa) | Regional | QR-code meal plan access | No ordering, no wallet |
| Campus Gusto (Nigeria) | Regional | Mobile meal scheduling | No institutional wallet |
| UoN Cafeteria (Kenya) | Local | Baseline — fully manual | No digital component |
| Strathmore (Kenya) | Local | QR code menu (PDF) | No ordering, no payment |

**Finding:** No Kenyan university currently offers mobile ordering, in-app wallet, pre-scheduling, or real-time tracking.

---

## SLIDE 4 — CHAPTER 3: AIMS AND OBJECTIVES

**TITLE:**
Chapter 3: Aims and Objectives

**BODY:**
**General Aim:**
Design, develop, and evaluate a cafeteria ordering system for USIU-Africa comprising a native Android app and a web-based admin panel.

**Three Specific Objectives:**
1. Conduct a literature survey on existing cafeteria and food ordering systems to identify strengths and weaknesses
2. Design and develop a prototype Android application with real-time ordering, advance meal scheduling, automated wallet deduction, and role-based access on a Firebase backend
3. Test and evaluate the prototype to assess the extent to which it resolves the identified problems

---

## SLIDE 5 — CHAPTER 4: PROPOSED PROJECT

**TITLE:**
Chapter 4: Proposed Project (Methodology)

**BODY:**
**Three-Phase Approach — 13 Weeks (January–April 2026)**

| Phase | Weeks | Key Activities | Milestone |
|---|---|---|---|
| Phase 1: Research & Design | 1–3 | Literature review, requirements, UML diagrams, wireframes | Design blueprint complete |
| Phase 2: Development | 4–8 | Firebase setup, Android app (12 steps), Supabase Edge Functions, admin panel | System implementation complete |
| Phase 3: Testing & Delivery | 9–10 | 20 test cases, UAT, documentation, final presentation | Testing & submission complete |

**Total Infrastructure Cost: KES 0**
All services operate on free tiers — Firebase Spark, Supabase, cron-job.org, Vercel.

---

## SLIDE 6 — CHAPTER 5: SYSTEM ANALYSIS AND DESIGN

**TITLE:**
Chapter 5: System Analysis and Design

**BODY:**
**Three-Tier Architecture:**
- **Presentation Layer:** Android app (students + staff) + Next.js web admin panel
- **Application Layer:** Firebase Android SDK + Supabase Edge Functions (TypeScript/Deno)
- **Data Layer:** Firestore (5 collections) + Firebase Auth + Firebase Cloud Messaging

**Five Firestore Collections:**
`users` · `menuItems` · `orders` · `preOrders` · `walletTransactions`

**Diagrams Produced:**
Use Case · ERD · DFD (Level 0, 1, 2) · Flowcharts · Activity · Sequence · Class · Wireframes

**20 Functional Requirements** covering student ordering, staff management, admin control, and automated pre-order processing.

---

## SLIDE 7 — CHAPTER 6: IMPLEMENTATION

**TITLE:**
Chapter 6: Implementation

**BODY:**
**Android App (Java + XML, MVVM architecture):**
- 5 student screens: Menu, Cart, Orders, Pre-orders, Profile/Wallet
- 3 staff screens: StaffOrders, StaffMenu, StaffWallet
- Hide/show fragment navigation — preserves scroll state across tabs
- `FirestoreRepository` centralises all Firestore access
- `runTransaction()` enforces atomic wallet deductions — prevents double-spending

**Backend:**
- Supabase Edge Functions replace Firebase Cloud Functions (free, no Blaze plan needed)
  - `process-cutoff`: triggered by cron-job.org at 10 AM + 5 PM EAT daily
  - `notify-order-ready`: called by app after staff marks order Ready → sends FCM push
- **Key fix discovered:** `db.settings({ preferRest: true })` required — Deno does not support gRPC

**Admin Panel:** Next.js 14 + TypeScript, deployed on Vercel, Firebase Admin SDK server-side only.

---

## SLIDE 8 — CHAPTER 7: TESTING AND EVALUATION

**TITLE:**
Chapter 7: Testing and Evaluation

**BODY:**
**15 Test Cases — All Passed**

| Category | Cases | Result |
|---|---|---|
| Functional | TC01–TC09 | All 9 passed |
| Security (Firestore rules) | TC10–TC12 | All 3 passed |
| Performance | TC13–TC15 | All 3 passed |

**Performance Results:**
- Menu cold-launch load time: ~1.8 seconds (target: < 3 s) ✓
- Order status propagation (staff → student): ~1 second ✓
- New order on staff screen: ~1–2 seconds ✓

**User Acceptance Testing:**
- Developer 2-device role simulation — no failures
- 3 USIU-Africa peer participants — no functional failures
- Feedback: ordering flow intuitive, real-time notifications add real value
- One minor finding: "Pre-orders" label not immediately self-explanatory → noted for future onboarding tooltip

---

## SLIDE 9 — CHAPTER 8: CONCLUSION

**TITLE:**
Chapter 8: Conclusion

**BODY:**
**Achievements:**
- Fully functional 3-role system across two applications — zero operational cost
- Real-time order lifecycle with sub-second Firestore propagation and FCM push notifications
- Atomic in-app wallet with complete staff audit trail on every transaction
- Automated pre-order cut-off running daily via Supabase + cron-job.org — no persistent server needed

**Challenges Overcome:**
- Deno/gRPC incompatibility → resolved with `preferRest: true`
- Wallet race condition → resolved with Firestore `runTransaction()`
- Fragment state loss → resolved with hide/show navigation pattern

**Future Work:**
M-Pesa wallet top-up · Push notification preferences · Order analytics dashboard · Repeat-order shortcut · Multi-outlet support · Automated test suite

---

## SLIDE 10 — APPENDICES OVERVIEW

**TITLE:**
Appendices

**BODY:**
**Appendix A — User Manual**
Quick-start guide for all three roles:
- Students: registration, ordering, pre-scheduling, wallet management
- Staff: order processing workflow, menu management, wallet top-ups
- Admins: menu CRUD, staff account creation, users directory

**Appendix B — Technical Deployment Guide**
Step-by-step reference for deploying and maintaining the system:
- Firebase rules + indexes deployment
- Supabase Edge Functions deployment and secrets configuration
- cron-job.org scheduling configuration
- Android APK signing and generation
- Vercel admin panel deployment
- Common maintenance tasks (password reset, quota monitoring, secret rotation)

---

## SLIDE 11 — CLOSING / DEMO

**TITLE:**
Thank You

**BODY:**
**Live System — Fully Deployed at Zero Cost:**
- Android APK: installed on physical devices
- Firestore + Firebase Auth + FCM: live on Spark free plan
- Supabase Edge Functions: deployed and running
- cron-job.org: 2 jobs active (10 AM + 5 PM EAT daily)
- Next.js Admin Panel: live on Vercel

**Questions welcome.**

*"The technology to solve institutional cafeteria queuing has existed for over a decade.
This project is the first to bring it to the Kenyan university context."*
