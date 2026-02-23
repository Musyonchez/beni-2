# 5.1.3 Entity Relationship Diagram (ERD)

## Introduction

An ERD illustrates the logical structure of the database by showing entities, their attributes, and the relationships between them. For the USIU Cafeteria Ordering System, the ERD maps the five Firestore collections that store all system data — users, menuItems, orders, preOrders, and walletTransactions (with orderItems embedded as arrays) — and defines how they relate to one another.

> [Figure 14: Entity Relationship Diagram (ERD) — six Firestore collections with attributes and relationships]

Figure 14 above shows the Entity-Relationship Diagram for the USIU Cafeteria Ordering System. The five key entities are as follows:

**users** — Represents all registered users of the system. Attributes: uid (PK), name, studentId (students only; absent for staff and admin), email, role (student | staff | admin), walletBalance (students only; absent for staff and admin), fcmToken (renamed from deviceToken in the original plan), firstLogin (boolean; staff and admin only — set to true on account creation, cleared to false after the first-login password change), createdAt. A student user can place many orders, many pre-orders, and have many wallet transactions.

**menuItems** — Represents each item available in the cafeteria menu. Attributes: id (PK), name, category (Mains | Snacks | Drinks | Specials), price, available (boolean), imageUrl. A menu item can appear in many order items and many pre-order items.

**orders** — Represents a single order placed by a student. Attributes: id (PK), userId (FK → users), status (Pending | Preparing | Ready | Collected), paymentMethod (wallet | cash), total, pickupTime, createdAt. Each order belongs to one user and contains one or more embedded orderItems.

**orderItems** — Embedded within each order document as an array. Attributes: menuItemId (FK → menuItems), name, price, quantity. This denormalised structure avoids a separate collection lookup on every order read.

**preOrders** — Represents an advance meal scheduled by a student. Attributes: id (PK), userId (FK → users), mealType (breakfast | lunch | dinner), pickupTime, recurring (boolean), dayOfWeek (0–6, used if recurring), status (scheduled | confirmed | cancelled | completed), total, createdAt. Items are embedded as an array identical to orderItems.

**walletTransactions** — Represents each credit or debit event on a student's wallet. Attributes: id (PK), userId (FK → users — the student whose wallet was changed), type (credit | debit), amount, description, staffId (FK → users — the UID of the staff member who performed the transaction, for audit purposes), createdAt. Provides a complete, accountable audit trail for every wallet balance change, linking each transaction to the responsible staff member.

The ERD illustrates that all financial and ordering activity is anchored to the users entity through foreign key relationships. Firestore security rules use three helper functions — isStudent(), isStaff(), and isAdmin() — to gate access per collection, with an isPrivileged() helper combining staff and admin permissions for shared operations such as order status updates and menu management. This ensures each student sees only their own data while staff and admin can access the records necessary for their respective roles.
