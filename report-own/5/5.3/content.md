# 5.3 Non-Functional Requirements

Non-functional requirements describe the quality attributes and constraints that the Cafeteria Ordering System must satisfy beyond its core features.

- **NFR1: Performance Requirements**
  The system shall display the current menu and cart within 2 seconds on a stable mobile data or Wi-Fi connection. Real-time order status updates shall be reflected in the app within 3 seconds of a staff status change. The estimated wait time calculation shall execute entirely on the client using a cached Firestore query result and shall complete within 1 second.

- **NFR2: Security Requirements**
  All users must authenticate via Firebase Authentication before accessing any system function. Firestore security rules shall enforce role-based access using three helper functions — isStudent(), isStaff(), and isAdmin() — with an isPrivileged() helper for shared staff/admin operations: students may only read and write their own orders, pre-orders, and wallet records; staff may read and update all orders and perform wallet top-ups and deductions; admin may read and write all collections. No client may directly modify another user's wallet balance without a server-side Firestore transaction. Supabase Edge Functions authenticate via a shared FUNCTIONS_SECRET header; the admin panel's privileged operations (staff account creation) execute via Next.js server-side API routes using the Firebase Admin SDK, never exposing admin credentials to the browser. Wallet deductions use Firestore runTransaction() to guarantee atomicity and prevent double-spending.

- **NFR3: Usability Requirements**
  The application shall follow Material Design 3 guidelines with USIU branding (navy #002147, gold #CFB991) to ensure a consistent and familiar experience. Navigation between the five main screens (Menu, Cart, Orders, Pre-orders, Profile/Wallet) shall require no more than one tap from any screen. The pre-order form shall clearly display the applicable cut-off time and wallet-only payment constraint so that students are not surprised by automatic deductions.

- **NFR4: Reliability Requirements**
  The system shall rely on Firebase's managed infrastructure, which provides 99.95% uptime SLA, to store all order and wallet data. Pre-order cut-off processing shall be performed by the Supabase process-cutoff Edge Function, triggered by cron-job.org at the scheduled times, rather than client devices — ensuring that deductions occur even when no student has the app open. All Firestore writes shall use offline persistence so that students can browse the menu and build a cart without an active connection, with synchronisation occurring when connectivity is restored.

- **NFR5: Scalability**
  The system shall use Firebase Firestore, a horizontally scalable NoSQL database, allowing the number of concurrent users, menu items, and orders to grow without schema migrations or infrastructure changes. Adding new meal slots, menu categories, or staff accounts shall require only Firestore document inserts or Firebase Authentication role assignments, with no changes to application code.

- **NFR6: Maintainability**
  All Firestore interactions in the Android app shall be centralised in a FirestoreRepository class, so that query logic can be updated in one place without modifying Activity or Fragment code. Business logic for pre-order cut-off shall reside exclusively in the Supabase process-cutoff Edge Function (TypeScript), making it independently deployable, testable via HTTP POST, and updatable without rebuilding the Android APK. The admin panel's privileged Firebase operations shall reside in Next.js API routes, keeping admin SDK credentials off the client.

- **NFR7: Compliance**
  The system shall not store any payment card or M-Pesa credentials. Wallet top-ups are performed in person (cash at the counter), and all financial records are maintained within the university's own Firestore project, reducing data-sovereignty and regulatory risk.
