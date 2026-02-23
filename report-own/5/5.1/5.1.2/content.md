# 5.1.2 Use Case Diagram

A Use Case Diagram is used to model the interactions between users (actors) and the system, illustrating the different services the application provides. For the USIU Cafeteria Ordering System, the diagram identifies three human actors and one system actor, and the use cases each is associated with.

> [Figure 13: Use Case Diagram — Student, Staff, and Admin actors with their associated use cases and system-automated functions]

Figure 13 above shows the use case diagram illustrating the key interactions between actors and the USIU Cafeteria Ordering System.

**Student** is the primary actor, interacting via the Android app. Use cases include: Register/Login, Browse Menu by Category, Add Item to Cart, Adjust Item Quantity, Select Payment Method (Wallet or Cash), Place Order, Track Order Status (real-time), View Estimated Wait Time, Receive Push Notification (Order Ready / Pre-order Confirmed / Insufficient Funds), View Order History, Schedule Pre-order, Set Recurring Pre-order, Cancel Pre-order (before cut-off), View Wallet Balance, and View Wallet Transaction History.

**Staff (Cashier)** is the second human actor, interacting via the Android app. Use cases include: Login (account created by Admin — staff cannot self-register), Change Password on First Login, View All Incoming Orders (real-time), Mark Order as Preparing, Mark Order as Ready, Mark Order as Collected, Toggle Menu Item Availability, Top Up Student Wallet (after receiving cash at counter), and Deduct Student Wallet Balance (manual charge adjustment, recorded with staff ID for audit).

**Admin** is the third human actor, interacting exclusively via the Next.js web admin panel. Use cases include: Login, Change Password on First Login, Create Staff Account (generates temporary password; forces first-login change on Android), Add Menu Item, Edit Menu Item, Delete Menu Item, Toggle Menu Item Availability, and View All Users (students and staff/admin separately).

**System (Automated Functions)** represents the Supabase Edge Functions and cron-job.org scheduler. Automated use cases include: Execute Lunch Pre-order Cut-off (10:00 AM EAT daily — triggered by cron-job.org POST to process-cutoff edge function), Execute Dinner Pre-order Cut-off (5:00 PM EAT — same logic), Send FCM Notification on Order Ready (Android calls notify-order-ready edge function after staff status update), Send FCM Notification on Pre-order Confirmed, and Send FCM Notification on Pre-order Cancelled due to Insufficient Funds.

The diagram uses «include» relationships for shared processes such as authentication (required by all login-gated use cases) and Firestore read/write (required by all data operations). «extend» relationships cover optional or conditional processes such as recurring pre-order chain creation, cancellation notifications, and forced first-login password change.
