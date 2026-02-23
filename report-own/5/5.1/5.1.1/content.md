# 5.1.1 System Architecture Diagram

A system architecture diagram is a visual representation that shows how the different components of a system interact and work together. It illustrates the structure, data flow, and relationships between the user interface, application logic, and data storage layers. For the USIU Cafeteria Ordering System, the diagram outlines how students, staff, and administrators — across two client applications — connect to Firebase backend services and Supabase Edge Functions to deliver real-time ordering, wallet management, and pre-order scheduling.

> [Figure 12: System Architecture Diagram — 3-tier architecture showing two client applications (Android app and Next.js admin panel), the Firebase SDK and Supabase Edge Functions layer, and the Firebase backend]

Figure 12 above shows that the architecture of the USIU Cafeteria Ordering System follows a three-tier design consisting of the Presentation Layer, Application Layer, and Data Layer.

## 1. Presentation Layer

This layer represents the user interface and the primary interaction point of the system. It consists of two separate client applications serving three user roles: Student, Staff (Cashier), and Admin.

The **native Android application** (Java + XML, Material Design 3) serves both Students and Staff. Students access five main screens: Menu (browse and filter by category), Cart (build order, select wallet or cash payment), Orders (track active order, view history, see estimated wait time), Pre-orders (schedule and manage advance meals), and Profile/Wallet (balance, transaction history, account details). Staff access three operational screens: Staff Orders (view and update all incoming orders), Staff Menu (toggle item availability), and Staff Wallet (top up or deduct student wallet balances and record each transaction with the staff member's ID). Role-based navigation is determined at login by reading the role field from the user's Firestore document. Staff accounts cannot self-register — they are created by an administrator and must change their temporary password on first login.

The **Next.js web admin panel** (TypeScript, Tailwind CSS, hosted on Vercel) serves Administrators only. It provides full menu management (add, edit, delete items; toggle availability; set image URLs), staff account creation (with a temporary password and an enforced first-login password change on the Android app), and a user directory with separate views for students and staff/admin accounts.

## 2. Application Layer

The Application Layer sits between the client applications and the Firebase backend. For the Android app, it is implemented through the Firebase Android SDK, which manages authentication sessions, Firestore real-time listeners, and FCM token registration on the device. For the admin panel, authentication and real-time reads use the Firebase Client SDK in the browser, while privileged operations such as creating staff accounts use the Firebase Admin SDK via Next.js server-side API routes.

On the server side, two **Supabase Edge Functions** (TypeScript/Deno runtime, deployed on the Supabase free plan) replace Firebase Cloud Functions for scheduled and event-driven logic. The process-cutoff function is invoked at 10:00 AM EAT (lunch) and 5:00 PM EAT (dinner) daily by cron-job.org. It queries all preOrders documents due that day, then for each: deducts the wallet balance via a Firestore transaction and marks the pre-order confirmed, or cancels it and sends an FCM notification if funds are insufficient. The notify-order-ready function is called directly by the Android app via an authenticated HTTP POST after a staff member marks an order as Ready, dispatching an FCM push notification to the student. All Firestore writes involving wallet deductions use runTransaction() to guarantee atomicity.

## 3. Data Layer

The Data Layer is managed by Firebase and consists of:

- **Firebase Firestore:** The primary cloud NoSQL database storing five collections — users, menuItems, orders, preOrders, and walletTransactions — with orderItems embedded as arrays within each order and pre-order document. Real-time snapshot listeners push changes to the Android client without polling.
- **Firebase Authentication:** Manages identity for all three roles (student, staff, admin) through email/password sign-in. Each user has a corresponding document in the users collection with a role field.
- **Firebase Cloud Messaging:** Delivers push notifications to registered FCM tokens stored in the fcmToken field of each user's document.

Supabase Edge Functions connect to Firebase using the Firebase Admin SDK configured for REST transport (gRPC is not supported in the Deno edge runtime). cron-job.org provides external scheduling for the pre-order cut-off triggers, and Vercel provides continuous deployment for the admin panel — both at zero cost.
