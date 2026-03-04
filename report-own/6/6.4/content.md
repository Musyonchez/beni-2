# 6.4 Integration

The integration phase of the USIU Cafeteria Ordering System aimed at establishing seamless connectivity between the Android UI layer, the FirestoreRepository data layer, the ViewModel state management layer, and the external Supabase Edge Functions. The objective was to ensure that data flows predictably through the system in all directions: menu updates from staff propagate to students in real time; cart actions in CartViewModel trigger atomic Firestore transactions; order status advances by staff notify students via push notifications; and pre-order cut-offs run automatically on a schedule without any user intervention. Through consistent architectural patterns and careful integration testing across all workflows, the system achieved a reliable, cohesive user experience.

## i. Fragment Navigation Integration

The navigation backbone of the Android application is a BottomNavigationView inside each of the two main activities — MainActivity for students and StaffMainActivity for staff. Fragment switching is implemented using the hide/show pattern rather than the standard replace pattern. When the user taps a navigation item, the currently visible fragment is hidden (hide()) and the target fragment is shown (show()). If the target fragment has not yet been added to the back stack, it is created and added (add()) at that point. Subsequent visits to the same tab simply call show() on the already-created instance.

This approach has a critical practical advantage for the cafeteria context: the MenuFragment's RecyclerView scroll position and the OrdersFragment's tab selection are preserved when the student navigates away and back. If replace() were used instead, each return to the fragment would reconstruct the view hierarchy from scratch, losing state and causing an unnecessary re-attachment of Firestore listeners.

> [Figure 34: MainActivity navigation — hide/show fragment transaction logic for student bottom navigation with role-based activity routing]

Figure 34 shows the fragment management logic inside MainActivity. The activity holds references to all five fragment instances. When a navigation item is selected, the transaction manager hides all other fragments and shows the selected one. This ensures only one Firestore snapshot listener per collection is active at any time within the student session, preventing duplicate data emissions.

## ii. Cart and Wallet Integration

The cart workflow integrates CartViewModel, WalletViewModel, and FirestoreRepository in a coordinated sequence. The student builds the cart entirely in memory using CartViewModel — no Firestore writes occur until "Place Order" is tapped. This design keeps Firestore costs low and avoids creating incomplete order documents in the database.

When "Place Order" is tapped with wallet payment selected, CartFragment first checks WalletViewModel's current balance against the cart subtotal. If insufficient, it shows a dialog and does not proceed. If sufficient, it calls FirestoreRepository.placeOrderWithWalletDeduction(), which executes a Firestore runTransaction containing three operations: reading the current walletBalance, deducting the order total, and writing both the new order document and the walletTransactions document. Because all three operations run atomically inside the transaction, the system guarantees that either all succeed together or none are committed. This prevents partial states such as a deduction without an order record, or an order created without a corresponding balance reduction.

For cash orders, CartFragment calls FirestoreRepository.placeOrderCash(), which writes only the order document with paymentMethod set to "cash". No wallet check or deduction occurs. On success in both paths, CartViewModel.clearCart() is called, the student is navigated to the Orders tab, and a snackbar confirms the placement.

The estimated wait time displayed in CartFragment is computed once when the fragment resumes by calling FirestoreRepository.getActiveOrderCount(), which runs a one-shot Firestore query counting documents with status "pending" or "preparing". The result is multiplied by five (minutes per order) and displayed as "Estimated wait: ~X min". A minimum of five minutes is shown even when the queue is empty, representing the preparation time for the student's own order.

> [Figure 35: Cart-Wallet integration — placeOrderWithWalletDeduction() Firestore runTransaction atomically deducting balance, writing order, and writing walletTransactions document]

Figure 35 shows the cart-to-order transaction sequence. The runTransaction encompasses four operations — reading the current balance, writing the deducted balance, writing the order document, and writing the walletTransactions record — as a single atomic unit on the Firestore server. This guarantees that no partial state can persist regardless of network conditions or concurrent requests from other devices.

## iii. Order Status Flow Integration

The order status lifecycle integrates the staff's Android interface, Firestore, the Supabase notify-order-ready Edge Function, and the student's Android interface through a coordinated sequence of events.

When a staff member taps "Mark Ready" in StaffOrdersFragment, the fragment calls FirestoreRepository.updateOrderStatus(orderId, "ready"), which writes the new status to the Firestore orders document. This write triggers an immediate update in the student's OrdersFragment through the active listenToStudentOrders() snapshot listener, changing the status chip from blue ("Preparing") to green ("Ready"). Concurrently, after the Firestore write completes successfully, the Android app calls FirestoreRepository.notifyOrderReady(orderId, userId) as a background operation. This method sends an HTTP POST to the notify-order-ready Supabase Edge Function with the FUNCTIONS_SECRET Bearer token. The edge function reads the student's fcmToken from Firestore, constructs an FCM notification payload, and dispatches the push notification to the student's device.

This integration means the student receives both a real-time in-app UI update (via the Firestore listener) and a push notification (via FCM), regardless of whether the app is foregrounded. The notification call to the edge function is fail-silent: if the network request fails, the order status has already been updated in Firestore, so the student's in-app view will still reflect the "Ready" state. The notification is a supplementary alert, not the primary status mechanism.

> [Figure 36: Order status pipeline — staff marks Ready, Firestore write updates student UI via snapshot listener, Android triggers HTTP POST to notify-order-ready edge function, FCM push delivered to student]

Figure 36 shows the order status pipeline. The Firestore write is the primary mechanism driving the student's in-app update; the subsequent HTTP POST to the edge function is a supplementary notification path. Because the student's OrdersFragment holds an active snapshot listener, the status chip transitions from blue to green within milliseconds of the staff action, independent of FCM delivery.

## iv. Pre-order Cut-off Integration

The pre-order cut-off workflow integrates cron-job.org, the Supabase process-cutoff Edge Function, Firestore, and Firebase Cloud Messaging into a fully automated pipeline that runs without any user or administrator intervention.

Two cron jobs are configured in cron-job.org. The first executes at 07:00 UTC daily (10:00 AM East Africa Time) and sends an HTTP POST to the process-cutoff Edge Function with a JSON body specifying mealSlot as "lunch". The second executes at 14:00 UTC daily (5:00 PM East Africa Time) with mealSlot set to "dinner". Each request includes the FUNCTIONS_SECRET Bearer token for authentication.

Upon receiving the request, the process-cutoff function queries Firestore for all preOrders documents where status is "scheduled", scheduledDate equals today's date, and mealSlot matches the request parameter. For each matching document, the function performs the following steps inside a Firestore transaction: it reads the student's current walletBalance; if the balance is greater than or equal to the pre-order's totalAmount, it deducts the amount, sets the pre-order status to "confirmed", and creates a walletTransactions document of type "deduction"; if the balance is insufficient, it sets the status to "cancelled" without touching the wallet. In both cases, an FCM notification is sent to the student's device via the fcmToken stored in their users document — a confirmation message if confirmed, a cancellation alert with a request to top up if cancelled.

For recurring pre-orders (recurring == true), after processing the current document, the function creates a new preOrders document with the same items, mealSlot, and recurringDays but with scheduledDate set to the same day of the following week and status set to "scheduled". This new document will be picked up automatically at the next week's cut-off run, creating a continuous weekly chain that persists until the student cancels a scheduled instance.

> [Figure 37: Pre-order cut-off integration — cron-job.org trigger, process-cutoff query and runTransaction, FCM notification dispatch, and recurring next-occurrence document creation]

Figure 37 illustrates the end-to-end cut-off flow. The student has no involvement in this process beyond the initial scheduling; the wallet deduction, status update, and notification all occur automatically at the correct time each day.

