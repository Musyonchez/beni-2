# 5.1.4 Data Flow Diagram (DFD)

A Data Flow Diagram (DFD) is a visual representation of how data moves within a system. It focuses on the flow of information between processes, data stores, and external entities, showing how inputs are transformed into outputs. The DFD for the USIU Cafeteria Ordering System is developed across three levels, from a high-level context view down to detailed sub-process breakdowns.

## i. Level 0: Context Diagram

The Level 0 DFD represents the entire system as a single process and shows how it interacts with external entities at the highest level of abstraction.

> [Figure 15: Level 0 Context Diagram — Student and Staff as external entities exchanging data with the Cafeteria Ordering System]

Figure 15 above shows the context diagram. The two external entities are Student and Staff. Students provide inputs to the system — menu browsing requests, order data (items, quantity, payment method), pre-order schedules, and wallet queries. The system outputs menu data, order confirmations, real-time status updates, estimated wait times, push notifications, and wallet balance information back to the student. Staff provide inputs — order status updates (Preparing/Ready), menu availability changes, and wallet top-up actions. The system outputs incoming order lists and updated menu states to the staff. Firebase Cloud Functions interact with the system as an automated entity, providing scheduled cut-off triggers that drive pre-order deductions and cancellations.

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
7. **Send Notification** — Cloud Function trigger: on order status → Ready, reads deviceToken from users collection, sends FCM message.
8. **Manage Pre-order** — Receives pre-order details from Student; writes preOrders document; reads upcoming pre-orders list back to Student.
9. **Execute Cut-off** — Scheduled Cloud Function: queries preOrders (mealType, status=scheduled, today); calls Deduct Wallet for each; on insufficient funds, marks cancelled and triggers Notify Insufficient Funds.
10. **Staff: Update Order Status** — Receives status change from Staff; writes to orders collection; triggers Send Notification.
11. **Staff: Manage Menu** — Receives availability toggle from Staff; updates menuItems.available field; propagates to all student Menu screens via snapshot listener.
12. **Staff: Top Up Wallet** — Receives student UID and amount from Staff; calls Deduct Wallet in credit mode; writes walletTransactions record.

## iii. Level 2: Detailed DFD

The Level 2 DFD expands the two most complex processes — Place Order and Execute Cut-off — into their internal sub-steps.

> [Figure 17: Level 2 DFD — detailed sub-processes for Place Order and Execute Pre-order Cut-off]

Figure 17 above shows the Level 2 DFD. For **Place Order**: (1) Receive cart and payment selection from CartFragment; (2) Validate cart is non-empty and all items still available; (3) If payment = wallet, check walletBalance >= total; (4) If sufficient, call runTransaction() — deduct balance, write walletTransactions record, write order document with status=Pending; (5) If insufficient, return error to CartFragment without writing order; (6) If payment = cash, write order document directly with status=Pending; (7) Return order ID and confirmation to CartFragment; (8) Clear CartViewModel.

For **Execute Pre-order Cut-off**: (1) Cloud Function cron fires at scheduled time; (2) Query preOrders where mealType=target, status=scheduled, pickupDate=today; (3) For each pre-order: read users.walletBalance; (4) If balance >= total: call runTransaction() — deduct balance, write walletTransactions, update preOrder.status=confirmed; (5) If balance < total: update preOrder.status=cancelled, read users.deviceToken, send FCM insufficient-funds notification; (6) Log execution result.

This Level 2 breakdown reveals the exact data stores touched in each critical operation and confirms that wallet atomicity is enforced at every deduction point through Firestore transactions.
