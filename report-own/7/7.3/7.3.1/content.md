# 7.3.1 Developer Role Simulation

In Stage 1, the developer operated two physical Android devices simultaneously — one logged in as a student and one logged in as a staff member — to simulate the complete order lifecycle end-to-end. The student device was used to register a new account, browse the menu, assemble a cart, and place both a wallet order and a cash order. The staff device was used to observe the incoming orders on the StaffOrders screen, tap "Start Preparing" to change the order status, and tap "Mark Ready" to trigger the FCM notification.

The pre-order scheduling flow was tested for a future lunch slot using the bottom sheet dialog accessed from the Pre-orders tab FAB. The wallet top-up and manual deduction features were exercised from the StaffWallet tab on the staff device, with the resulting balance change confirmed on the student device's Profile/Wallet screen.

All operations completed without errors. Key observations recorded during this stage:

- Bottom navigation between tabs was smooth and preserved scroll position correctly.
- The order status chip colour progression (grey for Pending, blue for Preparing, green for Ready) was visually clear and updated in approximately 1 second after each staff action.
- The wallet balance update was immediately reflected in the Profile tab after a staff top-up, confirming that the real-time Firestore listener on `WalletViewModel` was functioning correctly.
- The FCM push notification arrived on the student device within approximately 3 seconds of the staff tapping "Mark Ready", consistent with the performance test result in TC14.
- The pre-order card appeared in the Pre-orders tab immediately after saving, with the correct meal type, time, and "Scheduled" status badge.
