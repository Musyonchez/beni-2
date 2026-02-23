# 4.3.2 Phase Two: System Development

## i. Firebase and Backend Setup

**Objective:** To configure all backend infrastructure before writing application code.

**Tasks:** Create Firebase project; enable Firestore, Authentication, and Cloud Messaging (Spark free plan — no Cloud Functions or Blaze plan required); define Firestore security rules for three roles (student, staff, admin) and deploy composite indexes; seed initial menu data; create Supabase project and deploy process-cutoff and notify-order-ready Edge Functions; configure cron-job.org with two scheduled jobs (10:00 AM and 5:00 PM EAT); set up Vercel project for admin panel hosting.

**Deliverables:** Configured Firebase project with correct rules and initial data; deployed Supabase Edge Functions with active cron schedule; Vercel deployment pipeline ready.

**Timeline:** Week 5

---

## ii. Student-Side Fragment Development

**Objective:** To build the four student-facing screens.

**Tasks:** Implement MenuFragment (Firestore snapshot listener, category tab filtering, MenuAdapter); CartFragment (CartViewModel, quantity controls, payment method toggle, wallet balance display); OrdersFragment (real-time order listener, 3-step status bar, estimated wait time calculation); PreOrdersFragment (schedule dialog, recurring toggle, upcoming pre-orders list).

**Deliverables:** All four student fragments functional with live Firestore data.

**Timeline:** Weeks 6–8

---

## iii. Profile, Wallet, Staff Screens, and Admin Panel

**Objective:** To build the remaining Android screens and the web admin panel.

**Tasks:** Implement ProfileFragment (wallet balance, transaction history, student info, logout); StaffOrdersFragment (live incoming orders, Preparing/Ready/Collected actions); StaffMenuFragment (availability toggle); StaffWalletFragment (student wallet top-up and deduction with staffId audit); implement ChangePasswordActivity for staff/admin forced first-login password change. Develop the Next.js admin panel: login page (with role check and firstLogin redirect), menu management page (CRUD + image URL), staff account creation page (Firebase Admin SDK via API route), and users directory page (students and staff/admin tables).

**Deliverables:** All Android staff screens functional; admin panel deployed to Vercel with full menu and user management.

**Timeline:** Week 9

---

## iv. Edge Functions and End-to-End Integration

**Objective:** To complete scheduled cut-off automation and push notification delivery.

**Tasks:** Write and deploy process-cutoff Supabase Edge Function (TypeScript/Deno, Firebase Admin SDK with preferRest setting, Firestore transaction deductions, FCM on cancellation); deploy notify-order-ready Edge Function (called by Android after staff marks order Ready); configure FUNCTIONS_SECRET in Supabase and Android gradle.properties; test end-to-end order flow from student placement to staff update to student FCM notification; test pre-order cut-off by triggering edge function manually and verifying wallet deduction and notification.

**Deliverables:** Both Supabase Edge Functions deployed and integrated; full order lifecycle and pre-order cut-off verified end-to-end.

**Timeline:** Week 10
