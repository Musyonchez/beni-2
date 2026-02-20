# 5.1.5 Flowchart

A flowchart is a visual representation that outlines the step-by-step flow of processes within a system. It uses standardised symbols — ovals for start/end, rectangles for processes, diamonds for decisions, and arrows for flow direction. The USIU Cafeteria Ordering System has two distinct operational flows that are each represented as a separate flowchart: the regular order flow and the pre-order cut-off flow.

## Flowchart A: Regular Order Flow

> [Figure 18a: Flowchart A — Regular order flow from student login to order collection]

Figure 18a shows Flowchart A. The process begins when the Student launches the app and logs in. The system verifies credentials via Firebase Auth. If login fails, the user is prompted to retry. On success, the student is directed to the Menu screen where menu items are loaded from Firestore. The student browses the menu, selects items, and adds them to the cart. At checkout, the student selects payment method — Wallet or Cash. If Wallet is selected, the system checks whether walletBalance >= order total. If insufficient, an error is shown and the student remains on the Cart screen. If sufficient (or Cash is selected), the order is placed — a Firestore transaction deducts the wallet balance (if applicable) and writes the order document with status=Pending. The student is navigated to the Orders screen where the live status tracker shows Pending → Preparing → Ready in real time. When the staff marks the order Ready, an FCM notification is sent to the student's device. The student collects the order at the counter.

## Flowchart B: Pre-order Cut-off Flow

> [Figure 18b: Flowchart B — Pre-order cut-off flow executed by Firebase Cloud Function]

Figure 18b shows Flowchart B. The process begins when the Firebase Cloud Function cron fires at the scheduled cut-off time (10:00 AM for lunch, 5:00 PM for dinner). The function queries all pre-orders with matching mealType, status=scheduled, and today's pickup date. For each pre-order found: the system reads the student's current walletBalance. If balance >= pre-order total, the system executes a Firestore transaction — deducts the balance, writes a walletTransactions record, and updates the pre-order status to confirmed. The cafeteria prepares the meal for the scheduled pickup time. If balance < pre-order total, the pre-order status is updated to cancelled and an FCM insufficient-funds notification is sent to the student's device. The student receives the notification and can top up their wallet and reschedule if they choose. The function logs its execution result and terminates until the next scheduled trigger.
