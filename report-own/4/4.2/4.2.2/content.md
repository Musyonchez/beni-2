# 4.2.2 Phase Two: System Development / Implementation

## i. System Frontend

The system frontend will be developed as a native Android application using Java and XML, with Material Design 3 components and USIU branding (navy #002147, gold #CFB991). The frontend will provide students with a clean, intuitive interface across five main screens: Menu, Cart, Orders, Pre-orders, and Profile/Wallet. Staff users, identified by a role flag in their Firestore document, will see additional navigation items for order management, menu availability control, and wallet top-up. Fragment-based navigation with hide/show logic will be used to preserve state across tab switches without reloading. A shared CartViewModel will maintain cart state across fragments.

## ii. System Backend

The system backend will be implemented entirely through Firebase services, eliminating the need for a dedicated server. Firebase Firestore provides the real-time NoSQL database for menus, orders, pre-orders, users, and wallet transactions. Firebase Authentication handles user registration and login. Firebase Cloud Messaging delivers push notifications for order-ready alerts and insufficient-funds warnings. All Firestore reads and writes will be centralised in a FirestoreRepository class to keep fragment code clean and testable.

## iii. Cloud Functions

Firebase Cloud Functions will handle the two server-side scheduled jobs that cannot be performed reliably from a client device. The lunchCutoff function runs at 10:00 AM daily and processes all pre-orders with mealType set to lunch and status set to scheduled for the current day — deducting the total from the student's wallet balance using a Firestore transaction, or cancelling the pre-order and sending an FCM notification if the balance is insufficient. The dinnerCutoff function runs at 5:00 PM daily and applies the same logic for dinner pre-orders. A third function triggers on order status changes to send FCM notifications when staff marks an order as Ready.

## iv. System Integration

Integration will connect the Android client to Firebase through the Firebase Android SDK. Firestore real-time listeners (addSnapshotListener) will push live updates to the Orders and Menu screens without polling. Wallet deductions for immediate orders will use Firestore runTransaction() to ensure atomicity. Cloud Function triggers will be tested against Firestore document writes to confirm correct execution timing and notification delivery.

## v. Core Functionality Implementation

This phase will implement all features that define the system's value proposition: menu browsing with category filtering, cart management with payment method selection, order placement and real-time status tracking, estimated wait time calculation from queue depth, smart pre-order scheduling with day-of-week recurring support, automated cut-off processing, in-app wallet with staff-managed top-up, and role-based access in a single APK. Together these features constitute the Minimum Viable Product (MVP) that directly addresses all five problems identified in the problem statement.
