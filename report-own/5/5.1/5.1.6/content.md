# 5.1.6 Activity Diagram

An activity diagram is a type of behavioural diagram used in system modelling to represent the flow of activities and actions within a process. It focuses on how different tasks are carried out in sequence, the decisions made along the way, and how various activities connect to achieve a specific goal. For the USIU Cafeteria Ordering System, the activity diagram visualises the complete workflow across both student and staff roles, from login through to order collection, and separately for the pre-order scheduling lifecycle.

> [Figure 19: Activity Diagram — parallel swimlanes for Student, Staff, and System showing the full order and pre-order lifecycle]

Figure 19 above shows the activity diagram for the USIU Cafeteria Ordering System using swimlanes to separate the responsibilities of the Student, Staff, and System (Cloud Functions).

In the **Student swimlane**, activities begin with Login. On successful authentication, the student browses the menu, filtering by category if desired. The student selects items and adds them to the cart, adjusts quantities, and selects a payment method. If the chosen method is Wallet, a balance check occurs — if funds are insufficient, the student is returned to the cart. Otherwise, the order is placed and the student waits on the Orders screen, monitoring the live status tracker and the estimated wait time indicator. When the Ready notification arrives, the student collects their meal. For the pre-order path, the student instead navigates to the Pre-orders screen, enters meal details, selects a pickup time, optionally sets a recurring day, and saves the pre-order.

In the **Staff swimlane**, activities begin with Login (staff role). The staff views the incoming orders list in real time. On receiving a new order, the staff marks it as Preparing and begins preparation. When the meal is ready, the staff marks it as Ready. For menu management, staff can toggle item availability at any point. For wallet management, staff receive cash from a student, input the amount and student ID, and submit the top-up.

In the **System swimlane**, activities include: streaming menu updates to all connected student devices when availability changes; updating the order status display on the student's Orders screen when staff writes a status change; calculating estimated wait time on each incoming order update; and executing the cut-off cron job — scanning scheduled pre-orders, deducting wallets, and sending notifications — at the designated times.

The activity diagram demonstrates that the system operates across three concurrent actors, with the System swimlane bridging the Student and Staff swimlanes through real-time data propagation.
