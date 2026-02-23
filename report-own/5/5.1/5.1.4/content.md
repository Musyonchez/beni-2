# 5.1.4 Data Flow Diagram (DFD)

A Data Flow Diagram (DFD) is a visual representation of how data moves within a system. It focuses on the flow of information between processes, data stores, and external entities, showing how inputs are transformed into outputs. The DFD for the USIU Cafeteria Ordering System is developed across three levels, from a high-level context view down to detailed sub-process breakdowns.

## i. Level 0: Context Diagram

The Level 0 DFD represents the entire system as a single process and shows how it interacts with external entities at the highest level of abstraction.

> [Figure 15: Level 0 Context Diagram — Student and Staff as external entities exchanging data with the Cafeteria Ordering System]

Figure 15 above shows the context diagram. The three external human entities are Student, Staff, and Admin. Students provide inputs — menu browsing requests, order data (items, quantity, payment method), pre-order schedules, and wallet queries. The system returns menu data, order confirmations, real-time status updates, estimated wait times, push notifications, and wallet balance information. Staff provide inputs — order status updates (Preparing/Ready/Collected), menu availability changes, and wallet top-up or deduction actions. The system returns incoming order lists and updated menu states to staff. Admin provides inputs via the web panel — menu item CRUD and staff account creation. The system returns updated menu lists and user directories to admin. The Supabase Edge Function (triggered by cron-job.org) interacts with the system as an automated entity at 10:00 AM and 5:00 PM EAT, providing scheduled cut-off processing that drives pre-order wallet deductions and cancellations.

## ii. Level 1: Detailed DFD

The Level 1 DFD decomposes the single context process into the main functional sub-processes of the system.

> [Figure 16: Level 1 DFD — main system processes with data flows between external entities, processes, and Firestore data stores]

Figure 16 above shows the Level 1 DFD. The main processes are:

1. **Authenticate User** — Receives login credentials from Student/Staff, validates against Firebase Auth, returns session token.
2. **Load Menu** — Reads menuItems collection from Firestore; streams updates to student via snapshot listener.
3. **Manage Cart** — Receives add/remove/update actions from Student; maintains cart state in CartViewModel (device memory, no Firestore write until order placed).
4. **Place Order** — Receives confirmed cart and payment method from Student; writes order document to Firestore; if wallet payment, calls Deduct Wallet process atomically.
5. **Deduct Wallet** — Executes Firestore runTransaction() to deduct total from users.walletBalance and write a walletTransactions record simultaneously.
6. **Track Order** — Listens to orders collection for status changes; calculates estimated wait from count of Pending/Preparing orders ahead; streams updates to Student.
7. **Send Notification** — After the staff Android app writes status = Ready to Firestore, it sends an authenticated HTTP POST to the Supabase notify-order-ready Edge Function (fail-silent). The edge function reads the student's fcmToken from the users collection and dispatches an FCM push notification. This replaces the Firestore-trigger Cloud Function approach, which required the Firebase Blaze plan.
8. **Manage Pre-order** — Receives pre-order details from Student; writes preOrders document with status=scheduled; reads upcoming pre-orders list back to Student.
9. **Execute Cut-off** — Supabase Edge Function (process-cutoff) called by cron-job.org HTTP POST at 10:00 AM EAT (lunch) or 5:00 PM EAT (dinner): queries preOrders (mealSlot, status=scheduled, scheduledDate=today); for each match, calls Deduct Wallet via Firestore transaction; on insufficient funds, marks cancelled and sends FCM notification via Firebase Admin SDK (with preferRest: true — gRPC not supported in Deno runtime).
10. **Staff: Update Order Status** — Receives status change from Staff Android app; writes to orders collection; then app calls Send Notification (process 7).
11. **Staff: Manage Menu** — Receives availability toggle or CRUD action from Staff (Android) or Admin (web panel); updates menuItems collection; propagates to all student Menu screens via snapshot listener.
12. **Staff: Wallet Operation** — Receives student UID, amount, and operation type (credit or debit) from Staff; executes Firestore runTransaction() to update users.walletBalance; writes walletTransactions record including staffId for audit trail.
13. **Admin: Manage Users** — Admin creates staff account via web panel (Next.js API route → Firebase Admin SDK): creates Firebase Auth user, writes users document with role=staff and firstLogin=true. Admin views user directory from Firestore users collection.

## iii. Level 2: Detailed DFD

The Level 2 DFD expands the two most complex processes — Place Order and Execute Cut-off — into their internal sub-steps.

> [Figure 17: Level 2 DFD — detailed sub-processes for Place Order and Execute Pre-order Cut-off]

Figure 17 above shows the Level 2 DFD. For **Place Order**: (1) Receive cart and payment selection from CartFragment; (2) Validate cart is non-empty and all items still available; (3) If payment = wallet, check walletBalance >= total; (4) If sufficient, call runTransaction() — deduct balance, write walletTransactions record, write order document with status=Pending; (5) If insufficient, return error to CartFragment without writing order; (6) If payment = cash, write order document directly with status=Pending; (7) Return order ID and confirmation to CartFragment; (8) Clear CartViewModel.

For **Execute Pre-order Cut-off**: (1) cron-job.org HTTP POST fires at the scheduled time (10:00 AM or 5:00 PM EAT) with mealSlot in the request body; (2) Supabase process-cutoff Edge Function authenticates the request using FUNCTIONS_SECRET header; (3) Firebase Admin SDK (REST mode — gRPC unsupported in Deno) queries preOrders where mealSlot=target, status=scheduled, scheduledDate=today; (4) For each pre-order: read users.walletBalance inside a Firestore transaction; (5) If balance >= total: runTransaction() — deduct balance from walletBalance, write walletTransactions document (with staffId omitted for system transactions), set preOrder.status=confirmed, send FCM confirmation via users.fcmToken; (6) If balance < total: set preOrder.status=cancelled, send FCM insufficient-funds notification via users.fcmToken; (7) If pre-order is recurring: create next occurrence document in preOrders for the next matching weekday; (8) Log execution result.

This Level 2 breakdown reveals the exact data stores touched in each critical operation and confirms that wallet atomicity is enforced at every deduction point through Firestore transactions.
