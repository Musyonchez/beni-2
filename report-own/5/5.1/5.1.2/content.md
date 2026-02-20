# 5.1.2 Use Case Diagram

A Use Case Diagram is used to model the interactions between users (actors) and the system, illustrating the different services the application provides. For the USIU Cafeteria Ordering System, the diagram identifies the two primary human actors and one system actor, and the use cases each is associated with.

> [Figure 13: Use Case Diagram — Student and Staff actors with their associated use cases and system-automated functions]

Figure 13 above shows the use case diagram illustrating the key interactions between actors and the USIU Cafeteria Ordering System.

**Student** is the primary actor. Use cases include: Register/Login, Browse Menu by Category, Search Menu Items, Add Item to Cart, Adjust Item Quantity, Select Payment Method (Wallet or Cash), Place Order, Track Order Status (real-time), View Estimated Wait Time, Receive Push Notification (Order Ready / Insufficient Funds), View Order History, Reorder from History, Schedule Pre-order, Set Recurring Pre-order, Cancel Pre-order, View Wallet Balance, and View Wallet Transaction History.

**Staff** is the secondary actor. Use cases include: Login, View Incoming Orders (real-time), Mark Order as Preparing, Mark Order as Ready, Toggle Menu Item Availability, Top Up Student Wallet, and View Daily Order Summary.

**System (Automated Functions)** represents Firebase Cloud Functions and Firestore triggers. Automated use cases include: Execute Lunch Cut-off (10:00 AM daily — deduct wallet or cancel pre-order), Execute Dinner Cut-off (5:00 PM daily — same logic), Send FCM Notification on Order Ready, and Send FCM Notification on Insufficient Funds.

The diagram uses «include» relationships for shared processes such as authentication (required by all login-gated use cases) and Firestore read/write (required by all data operations). «extend» relationships cover optional or conditional processes such as recurring pre-order instance creation and cancellation notifications.
