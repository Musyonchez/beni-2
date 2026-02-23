# 5.2 Functional Requirements

Functional requirements define the specific actions and features that the Cafeteria Ordering System must provide to meet the needs of students, staff, and administrators.

## Student Functional Requirements

- **FR1:** The system shall allow students to browse the cafeteria menu, filtered by category (breakfast, lunch, dinner) and item availability.
- **FR2:** The system shall allow students to add items to a cart, adjust quantities, and review the order before confirming.
- **FR3:** The system shall allow students to place a regular order and choose to pay via the in-app wallet or declare cash payment at the counter.
- **FR4:** The system shall display a real-time estimated wait time for each order, calculated from the number of pending and preparing orders ahead in the queue and the average preparation time per item.
- **FR5:** The system shall allow students to place a smart pre-order for a future meal slot (lunch or dinner), subject to cut-off times (10:00 AM for lunch, 5:00 PM for dinner).
- **FR6:** The system shall allow students to set a pre-order as recurring on selected days of the week (e.g., every Thursday at 12:30).
- **FR7:** The system shall allow students to view the status of their active and past orders in real time.
- **FR8:** The system shall allow students to view their wallet balance and a transaction history of top-ups and deductions.

## Staff Functional Requirements

- **FR9:** The system shall allow staff to view all incoming orders and update their status (Pending → Preparing → Ready → Collected).
- **FR10:** The system shall allow staff to mark a cash order as paid when the student collects and pays at the counter.
- **FR11:** The system shall allow staff to manage the menu — add, edit, enable, or disable menu items and update daily availability.
- **FR12:** The system shall allow staff to credit (top up) or debit (deduct) a student's wallet balance by entering the student's ID and the amount; each transaction shall record the staff member's ID (staffId) for accountability.

## Admin Functional Requirements

- **FR13:** The system shall allow administrators to create staff accounts via the web admin panel; each new staff account shall be assigned a temporary password, and the staff member shall be required to change it on first login before accessing any operational screens.
- **FR14:** The system shall allow administrators to add, edit, delete, and toggle the availability of menu items via the web admin panel, with changes reflected immediately on all student Menu screens through Firestore snapshot listeners.
- **FR15:** The system shall allow administrators to view a directory of all users, with students and staff/admin accounts displayed in separate tables.

## System Functional Requirements

- **FR16:** The system shall automatically process all confirmed pre-orders at the respective cut-off time via the Supabase process-cutoff Edge Function (called by cron-job.org at 10:00 AM EAT for lunch and 5:00 PM EAT for dinner): deduct the order total from the student's wallet if funds are sufficient, or cancel the pre-order and send an FCM push notification if the wallet balance is insufficient.
- **FR17:** The system shall send FCM push notifications to students when their order status changes to Ready (via the Supabase notify-order-ready Edge Function, called by the Android app after the staff status update) and when a pre-order is confirmed or cancelled due to insufficient funds (via the process-cutoff Edge Function).
- **FR18:** The system shall enforce secure authentication via Firebase Authentication, ensuring that students (Android app), staff (Android app), and administrators (web panel) access only the data and screens appropriate to their role, as enforced by Firestore security rules using isStudent(), isStaff(), isAdmin(), and isPrivileged() helper functions.
- **FR19:** The system shall perform all wallet deductions atomically using Firestore runTransaction() to prevent race conditions or double-spending.
- **FR20:** The system shall maintain a complete, timestamped audit trail of all wallet transactions (top-ups and deductions) in the walletTransactions Firestore collection, including the staffId of the staff member who performed each transaction.
