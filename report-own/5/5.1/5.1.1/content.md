# 5.1.1 System Architecture Diagram

A system architecture diagram is a visual representation that shows how the different components of a system interact and work together. It illustrates the structure, data flow, and relationships between the user interface, application logic, and data storage layers. For the USIU Cafeteria Ordering System, the diagram outlines how students, staff, Firebase services, and Cloud Functions connect to deliver real-time ordering, wallet management, and pre-order scheduling.

> [Figure 12: System Architecture Diagram — 3-tier architecture showing Android client, Firebase SDK layer, and Firebase backend services]

Figure 12 above shows that the architecture of the USIU Cafeteria Ordering System follows a three-tier design consisting of the Presentation Layer, Application Layer, and Data Layer.

## 1. Presentation Layer

This layer represents the user interface and the primary interaction point of the system. It consists of two user roles — Student and Staff — both of whom access the system through the same native Android application. Role-based navigation is determined at login by reading the user's role field from Firestore.

Students interact with five main screens: Menu (browse and search), Cart (build order, select payment method), Orders (track active order, view history, see estimated wait time), Pre-orders (schedule and manage advance meals), and Profile/Wallet (balance, transactions, account details).

Staff interact with three additional screens: Staff Orders (view and update incoming orders), Staff Menu (toggle item availability), and Staff Wallet (top up student accounts after receiving cash). All screens communicate with Firebase through the Application Layer via the Firebase Android SDK.

## 2. Application Layer

The Application Layer sits between the Android client and the Firebase backend. It is implemented through the Firebase Android SDK, which manages authentication sessions, Firestore real-time listeners, and FCM token registration on the device. On the server side, Firebase Cloud Functions handle the two scheduled pre-order cut-off jobs (lunchCutoff at 10:00 AM, dinnerCutoff at 5:00 PM) and the onOrderStatusChanged trigger that fires FCM notifications when an order is marked Ready. All Firestore writes that involve wallet deductions use runTransaction() to guarantee atomicity.

## 3. Data Layer

The Data Layer is managed entirely by Firebase and consists of:

- **Firebase Firestore:** The primary cloud NoSQL database storing six collections — users, menuItems, orders, preOrders, walletTransactions, and embedded orderItems within order documents. Real-time snapshot listeners push changes to the Android client without polling.
- **Firebase Authentication:** Manages student and staff identity through email/password sign-in. Each authenticated user has a corresponding document in the users collection.
- **Firebase Cloud Messaging:** Delivers push notifications to registered device tokens stored in the users collection.
- **Firebase Cloud Functions:** Serverless Node.js runtime executing scheduled and trigger-based server-side logic independently of any client device.
