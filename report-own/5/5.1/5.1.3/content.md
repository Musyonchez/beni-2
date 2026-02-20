# 5.1.3 Entity Relationship Diagram (ERD)

## Introduction

An ERD illustrates the logical structure of the database by showing entities, their attributes, and the relationships between them. For the USIU Cafeteria Ordering System, the ERD maps the six Firestore collections that store all system data — users, menu items, orders, pre-orders, and wallet transactions — and defines how they relate to one another.

> [Figure 14: Entity Relationship Diagram (ERD) — six Firestore collections with attributes and relationships]

Figure 14 above shows the Entity-Relationship Diagram for the USIU Cafeteria Ordering System. The six key entities are as follows:

**users** — Represents all registered users of the system. Attributes: uid (PK), name, studentId, email, role (student | staff), walletBalance, deviceToken. A user can place many orders, many pre-orders, and have many wallet transactions.

**menuItems** — Represents each item available in the cafeteria menu. Attributes: id (PK), name, category (Mains | Snacks | Drinks | Specials), price, available (boolean), imageUrl. A menu item can appear in many order items and many pre-order items.

**orders** — Represents a single order placed by a student. Attributes: id (PK), userId (FK → users), status (Pending | Preparing | Ready | Collected), paymentMethod (wallet | cash), total, pickupTime, createdAt. Each order belongs to one user and contains one or more embedded orderItems.

**orderItems** — Embedded within each order document as an array. Attributes: menuItemId (FK → menuItems), name, price, quantity. This denormalised structure avoids a separate collection lookup on every order read.

**preOrders** — Represents an advance meal scheduled by a student. Attributes: id (PK), userId (FK → users), mealType (breakfast | lunch | dinner), pickupTime, recurring (boolean), dayOfWeek (0–6, used if recurring), status (scheduled | confirmed | cancelled | completed), total, createdAt. Items are embedded as an array identical to orderItems.

**walletTransactions** — Represents each credit or debit event on a student's wallet. Attributes: id (PK), userId (FK → users), type (credit | debit), amount, description, createdAt. Provides a full audit trail for every wallet balance change.

The ERD illustrates that all financial and ordering activity is anchored to the users entity through foreign key relationships, enabling the system to present each student with a personalised view of their orders, pre-orders, and transaction history while preventing access to other users' data through Firestore security rules.
