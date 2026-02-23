# 4.2.2 Phase Two: System Development / Implementation

## i. System Frontend

The system frontend consists of two client applications. The primary client is a native Android application developed in Java and XML, with Material Design 3 components and USIU branding (navy #002147, gold #CFB991). It provides students with five main screens (Menu, Cart, Orders, Pre-orders, and Profile/Wallet) and presents cafeteria staff with three operational screens (Staff Orders, Staff Menu, and Staff Wallet). Role-based navigation is determined at login from the Firestore user document; staff accounts are created exclusively by an administrator and cannot self-register. Fragment-based navigation with hide/show logic preserves state across tab switches. A shared CartViewModel maintains cart state across fragments.

The secondary client is a web-based admin panel built with Next.js and TypeScript, styled with Tailwind CSS, and hosted on Vercel. It is accessible only to users with the admin role and provides menu management (add, edit, delete, and toggle availability of items), staff account creation (with temporary password and enforced first-login password change), and a user directory with separate views for students and staff/admin accounts.

## ii. System Backend

The system backend will be implemented through Firebase services, eliminating the need for a self-managed server. Firebase Firestore provides the real-time NoSQL database for menus, orders, pre-orders, users, and wallet transactions. Firebase Authentication handles user registration and login across all three roles (student, staff, admin). Firebase Cloud Messaging delivers push notifications for order-ready alerts and insufficient-funds warnings on pre-order cancellation. All Firestore reads and writes will be centralised in a FirestoreRepository class to keep fragment code clean and testable.

## iii. Scheduled Edge Functions

The two server-side scheduled jobs that process pre-order cut-offs are implemented as Supabase Edge Functions (TypeScript, Deno runtime), deployed on the Supabase free tier. The process-cutoff edge function is called via HTTP POST by cron-job.org at 10:00 AM EAT (lunch) and 5:00 PM EAT (dinner) daily. For each pre-order with the matching mealSlot, status=scheduled, and scheduledDate=today, it checks the student's wallet balance: if sufficient, it deducts the total via a Firestore transaction and sets the pre-order to confirmed; if insufficient, it cancels the pre-order and sends an FCM push notification. A second edge function, notify-order-ready, is called directly by the Android app (via an HTTP POST after writing the status update to Firestore) when a staff member marks an order as Ready, sending an FCM notification to the student.

## iv. System Integration

Integration connects both client applications to Firebase through their respective SDKs — the Firebase Android SDK on mobile and the Firebase Admin SDK (server-side via Next.js API routes) on the web panel. Firestore real-time listeners (addSnapshotListener) push live updates to the Orders and Menu screens without polling. Wallet deductions for immediate orders use Firestore runTransaction() to ensure atomicity. The Supabase Edge Functions authenticate with Firebase Admin SDK using a shared secret (FUNCTIONS_SECRET) to read Firestore and send FCM messages. End-to-end integration is validated by testing the full order lifecycle from student placement to staff status update to student notification.

## v. Core Functionality Implementation

This phase will implement all features that define the system's value proposition: menu browsing with category filtering, cart management with payment method selection, order placement and real-time status tracking, estimated wait time calculation from queue depth, smart pre-order scheduling with day-of-week recurring support, automated cut-off processing via Supabase Edge Functions, in-app wallet with staff-managed top-up and deduction, three-role access control (student, staff, admin), and a web-based admin panel for menu and account management. Together these features constitute the Minimum Viable Product (MVP) that directly addresses all five problems identified in the problem statement.
