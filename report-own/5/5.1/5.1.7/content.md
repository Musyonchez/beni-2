# 5.1.7 Sequence Diagram

A sequence diagram is a type of interaction diagram used in system design to show how objects and components interact with each other over time. It focuses on the order of messages exchanged between different entities to accomplish a specific function. For the USIU Cafeteria Ordering System, two sequence diagrams are presented: one for the regular order flow and one for the pre-order cut-off execution.

## Sequence Diagram A: Regular Order Placement

> [Figure 20a: Sequence Diagram A — Regular order placement from student to Firestore to staff to FCM notification]

Figure 20a shows the sequence of interactions for a regular order. The participants are: Student (Android client), FirebaseAuth, CartViewModel, FirestoreRepository, Firestore, StaffOrdersFragment, and FCMService.

1. Student → FirebaseAuth: signIn(email, password)
2. FirebaseAuth → Student: authResult (uid, role)
3. Student → FirestoreRepository: loadMenu()
4. FirestoreRepository → Firestore: addSnapshotListener(menuItems)
5. Firestore → Student: menuItems stream
6. Student → CartViewModel: addItem(menuItem, quantity)
7. Student → CartViewModel: setPaymentMethod(wallet)
8. Student → FirestoreRepository: placeOrder(cart, paymentMethod)
9. FirestoreRepository → Firestore: runTransaction() [deduct walletBalance + write order(status=Pending)]
10. Firestore → Student: order confirmation (orderId)
11. Student → FirestoreRepository: listenToOrder(orderId)
12. Firestore → StaffOrdersFragment: new order notification (snapshot)
13. Staff → FirestoreRepository: updateOrderStatus(orderId, Preparing)
14. Firestore → Student: status update (Preparing)
15. Staff → FirestoreRepository: updateOrderStatus(orderId, Ready)
16. Firestore → FCMService: onOrderStatusChanged trigger
17. FCMService → Student: FCM push notification ("Your order is ready")
18. Firestore → Student: status update (Ready)

## Sequence Diagram B: Pre-order Cut-off

> [Figure 20b: Sequence Diagram B — Scheduled Cloud Function executing pre-order cut-off with wallet deduction or cancellation]

Figure 20b shows the sequence for the pre-order cut-off. The participants are: CloudScheduler, lunchCutoffFunction, Firestore, and FCMService.

1. CloudScheduler → lunchCutoffFunction: trigger (10:00 AM)
2. lunchCutoffFunction → Firestore: query preOrders (mealType=lunch, status=scheduled, today)
3. Firestore → lunchCutoffFunction: [preOrder1, preOrder2, ...]
4. For each preOrder:
   a. lunchCutoffFunction → Firestore: read users/{userId}.walletBalance
   b. If balance >= total:
      - lunchCutoffFunction → Firestore: runTransaction() [deduct walletBalance + write walletTransaction + update preOrder.status=confirmed]
   c. If balance < total:
      - lunchCutoffFunction → Firestore: update preOrder.status=cancelled
      - lunchCutoffFunction → FCMService: sendNotification(deviceToken, "Insufficient funds — pre-order cancelled")
5. lunchCutoffFunction → CloudScheduler: execution complete
