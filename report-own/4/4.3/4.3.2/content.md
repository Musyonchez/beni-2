# 4.3.2 Phase Two: System Development

## i. Firebase Project Setup

**Objective:** To configure the backend infrastructure before writing application code.

**Tasks:** Create Firebase project; enable Firestore, Authentication, Cloud Messaging, and Cloud Functions; define Firestore security rules; seed initial menu data; upgrade to Blaze plan for Cloud Functions.

**Deliverables:** Configured Firebase project with correct rules and initial data.

**Timeline:** Week 5

---

## ii. Student-Side Fragment Development

**Objective:** To build the four student-facing screens.

**Tasks:** Implement MenuFragment (Firestore snapshot listener, category tab filtering, MenuAdapter); CartFragment (CartViewModel, quantity controls, payment method toggle, wallet balance display); OrdersFragment (real-time order listener, 3-step status bar, estimated wait time calculation); PreOrdersFragment (schedule dialog, recurring toggle, upcoming pre-orders list).

**Deliverables:** All four student fragments functional with live Firestore data.

**Timeline:** Weeks 6–8

---

## iii. Profile, Wallet, and Staff Screens

**Objective:** To build the remaining screens and staff interface.

**Tasks:** Implement ProfileFragment (wallet balance, transaction history, student info, logout); StaffOrdersFragment (live incoming orders, Preparing/Ready actions); StaffMenuFragment (availability toggle); StaffWalletFragment (student wallet top-up).

**Deliverables:** Profile/Wallet screen and all staff screens functional.

**Timeline:** Week 9

---

## iv. Cloud Functions and Integration

**Objective:** To implement scheduled cut-off jobs and push notifications.

**Tasks:** Write and deploy lunchCutoff and dinnerCutoff Cloud Functions (cron triggers, Firestore transaction deductions, FCM on cancellation); implement onOrderStatusChanged trigger for FCM on Ready; test end-to-end order flow from student placement to staff update to student notification.

**Deliverables:** Cloud Functions deployed and integrated; full order lifecycle verified end-to-end.

**Timeline:** Week 10
