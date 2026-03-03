# Chapter 5 — Figures (PlantUML)

Render each block with PlantUML (e.g. `plantuml -tpng ch-5.md` or paste into https://www.plantuml.com/plantuml/uml/).
Figures marked **[NOT PLANTUML — placeholder]** must be drawn manually (Figma, draw.io, Lucidchart, etc.).

---

## Figure 12: System Architecture Diagram

```plantuml
@startuml Figure12_SystemArchitecture
skinparam componentStyle rectangle
skinparam backgroundColor white
skinparam defaultFontName Arial
title Figure 12: System Architecture Diagram

package "Presentation Layer" {
  [Android App\n(Java + XML, Material Design 3)\nStudent & Staff roles] as android
  [Next.js Admin Panel\n(TypeScript + Tailwind CSS)\nAdmin role — hosted on Vercel] as nextjs
}

package "Application Layer" {
  [Firebase Android SDK\n(Auth · Firestore · FCM)] as androidsdk
  [Firebase Client SDK\n(Browser — auth + reads)] as clientsdk
  [Firebase Admin SDK\n(Next.js server-side API routes)] as adminsdk
  [Supabase Edge Functions\n(TypeScript / Deno)\nprocess-cutoff · notify-order-ready] as supabase
  [cron-job.org\n(HTTP scheduler\n10:00 AM & 5:00 PM EAT)] as cron
}

package "Data Layer" {
  database "Firebase Firestore\nusers · menuItems · orders\npreOrders · walletTransactions" as firestore
  [Firebase Authentication\n(email/password — 3 roles)] as firebaseauth
  [Firebase Cloud Messaging\n(push notifications)] as fcm
}

android      --> androidsdk
nextjs       --> clientsdk
nextjs       --> adminsdk
androidsdk   --> firestore
androidsdk   --> firebaseauth
androidsdk   --> fcm
clientsdk    --> firestore
clientsdk    --> firebaseauth
adminsdk     --> firebaseauth
adminsdk     --> firestore
supabase     --> firestore : Admin SDK\n(REST mode)
supabase     --> fcm
cron         --> supabase : HTTP POST

@enduml
```

---

## Figure 13: Use Case Diagram

```plantuml
@startuml Figure13_UseCaseDiagram
left to right direction
skinparam actorStyle awesome
skinparam backgroundColor white
skinparam defaultFontName Arial
title Figure 13: Use Case Diagram

actor Student
actor "Staff (Cashier)" as Staff
actor Admin
actor "System\n(Supabase / cron-job.org)" as System

rectangle "USIU Cafeteria Ordering System" {
  usecase "Register / Login" as UC1
  usecase "Browse Menu by Category" as UC2
  usecase "Manage Cart\n(add · adjust · remove)" as UC3
  usecase "Select Payment Method\n(Wallet or Cash)" as UC4
  usecase "Place Order" as UC5
  usecase "Track Order Status\n(real-time)" as UC6
  usecase "View Estimated Wait Time" as UC7
  usecase "View Order History" as UC8
  usecase "Schedule Pre-order" as UC9
  usecase "View Wallet Balance &\nTransaction History" as UC10
  usecase "Receive Push Notification" as UC11

  usecase "Login (staff — account\ncreated by Admin)" as UC12
  usecase "Change Password\n(first login)" as UC13
  usecase "View All Incoming Orders" as UC14
  usecase "Update Order Status\n(Preparing / Ready / Collected)" as UC15
  usecase "Toggle Menu Item Availability" as UC16
  usecase "Top Up Student Wallet" as UC17
  usecase "Deduct Student Wallet" as UC18

  usecase "Login (Admin)" as UC19
  usecase "Manage Menu Items\n(add · edit · delete)" as UC20
  usecase "Create Staff Account" as UC21
  usecase "View All Users" as UC22

  usecase "Execute Pre-order Cut-off\n(10 AM lunch / 5 PM dinner)" as UC23
  usecase "Send FCM Notification" as UC24
}

Student --> UC1
Student --> UC2
Student --> UC3
Student --> UC4
Student --> UC5
Student --> UC6
Student --> UC7
Student --> UC8
Student --> UC9
Student --> UC10
UC11     .> Student : notify

Staff   --> UC12
Staff   --> UC13
Staff   --> UC14
Staff   --> UC15
Staff   --> UC16
Staff   --> UC17
Staff   --> UC18

Admin   --> UC19
Admin   --> UC13
Admin   --> UC20
Admin   --> UC21
Admin   --> UC22

System  --> UC23
System  --> UC24

UC5     ..> UC24 : <<include>>
UC15    ..> UC24 : <<include>>
UC23    ..> UC24 : <<include>>
UC21    ..> UC13 : <<extend>>

@enduml
```

---

## Figure 14: Entity Relationship Diagram (ERD)

```plantuml
@startuml Figure14_ERD
hide circle
skinparam classAttributeIconSize 0
skinparam backgroundColor white
skinparam defaultFontName Arial
title Figure 14: Entity Relationship Diagram (ERD)

entity "users" as users {
  * uid : String <<PK>>
  --
  name : String
  studentId : String
  email : String
  role : student | staff | admin
  walletBalance : Number
  fcmToken : String
  firstLogin : Boolean
  createdAt : Timestamp
}

entity "menuItems" as menu {
  * id : String <<PK>>
  --
  name : String
  category : Mains|Snacks|Drinks|Specials
  price : Number
  available : Boolean
  imageUrl : String
}

entity "orders" as orders {
  * id : String <<PK>>
  --
  userId : String <<FK>>
  status : Pending|Preparing|Ready|Collected
  paymentMethod : wallet | cash
  total : Number
  pickupTime : Timestamp
  createdAt : Timestamp
  orderItems : Array (embedded)
}

entity "orderItems" as oi {
  menuItemId : String <<FK>>
  name : String
  price : Number
  quantity : Number
}

entity "preOrders" as preorders {
  * id : String <<PK>>
  --
  userId : String <<FK>>
  mealType : breakfast|lunch|dinner
  pickupTime : Timestamp
  recurring : Boolean
  dayOfWeek : 0-6
  status : scheduled|confirmed|cancelled|completed
  total : Number
  createdAt : Timestamp
  orderItems : Array (embedded)
}

entity "walletTransactions" as wallet {
  * id : String <<PK>>
  --
  userId : String <<FK>>
  type : credit | debit
  amount : Number
  description : String
  staffId : String <<FK>>
  createdAt : Timestamp
}

users       ||--o{ orders          : "places"
users       ||--o{ preorders       : "schedules"
users       ||--o{ walletTransactions : "has"
orders      ||--|{ oi              : "contains (embedded)"
preorders   ||--o{ oi              : "contains (embedded)"
menu        ||--o{ oi              : "referenced by"
users       ||--o{ walletTransactions : "performed by (staffId)"

@enduml
```

---

## Figure 15: Level 0 Context Diagram

> **[NOT PLANTUML — placeholder]**
> Draw manually in draw.io / Lucidchart using standard DFD notation:
> - External entities (rectangles): Student, Staff, Admin, System (cron-job.org / Supabase)
> - Single central process circle: "USIU Cafeteria Ordering System"
> - Data flows (arrows): menu requests, order data, pre-order schedules, wallet queries → System; menu data, confirmations, status updates, notifications, wallet info ← System; order status updates, menu changes, wallet ops → System; incoming orders, updated menus ← System; menu CRUD, staff account creation → System; menus, user directory ← System; scheduled cut-off triggers → System.

---

## Figure 16: Level 1 DFD

> **[NOT PLANTUML — placeholder]**
> Draw manually in draw.io / Lucidchart using standard DFD notation.
> Processes (numbered circles): 1. Authenticate User, 2. Load Menu, 3. Manage Cart, 4. Place Order, 5. Deduct Wallet, 6. Track Order, 7. Send Notification, 8. Manage Pre-order, 9. Execute Cut-off, 10. Staff: Update Order Status, 11. Staff: Manage Menu, 12. Staff: Wallet Operation, 13. Admin: Manage Users.
> Data stores (open rectangles): D1 users, D2 menuItems, D3 orders, D4 preOrders, D5 walletTransactions.
> External entities (rectangles): Student, Staff, Admin, cron-job.org.

---

## Figure 17: Level 2 DFD

> **[NOT PLANTUML — placeholder]**
> Draw manually in draw.io / Lucidchart. Expand two processes from Level 1:
>
> **Place Order sub-processes:** 4.1 Receive cart & payment selection, 4.2 Validate cart & item availability, 4.3 Check walletBalance >= total (wallet path), 4.4 runTransaction() — deduct balance + write walletTransaction + write order(Pending), 4.5 Return error if insufficient, 4.6 Write order(Pending) directly (cash path), 4.7 Return orderId, 4.8 Clear CartViewModel.
>
> **Execute Cut-off sub-processes:** 9.1 Receive cron HTTP POST + authenticate FUNCTIONS_SECRET, 9.2 Query preOrders (mealSlot, status=scheduled, today), 9.3 Read walletBalance per pre-order, 9.4 runTransaction() — deduct + confirm (if sufficient), 9.5 Cancel + send FCM (if insufficient), 9.6 Create next recurring occurrence, 9.7 Log result.

---

## Figure 18a: Flowchart A — Regular Order Flow

```plantuml
@startuml Figure18a_FlowchartA
skinparam backgroundColor white
skinparam defaultFontName Arial
title Figure 18a: Regular Order Flow

start
:Student launches app;
:Enter email & password;
if (Firebase Auth valid?) then (yes)
  :Navigate to Menu screen;
  :Load menuItems from Firestore;
  :Browse menu (filter by category);
  :Select items → add to Cart;
  :Review cart total;
  :Select payment method;
  if (Payment = Wallet?) then (yes)
    if (walletBalance >= order total?) then (yes)
      :runTransaction()\nDeduct walletBalance\nWrite walletTransaction\nWrite order (status = Pending);
    else (no)
      :Show "Insufficient wallet balance"\nerror on Cart screen;
      stop
    endif
  else (Cash)
    :Write order document\n(status = Pending, paymentMethod = cash);
  endif
  :Navigate to Orders screen;
  :Firestore listener — live status updates;
  :status: Pending → Preparing → Ready;
  :Receive FCM push notification\n"Your order is ready";
  :Collect order at counter;
  stop
else (no)
  :Show login error — retry;
  stop
endif
@enduml
```

---

## Figure 18b: Flowchart B — Pre-order Cut-off Flow

```plantuml
@startuml Figure18b_FlowchartB
skinparam backgroundColor white
skinparam defaultFontName Arial
title Figure 18b: Pre-order Cut-off Flow

start
:cron-job.org fires HTTP POST\nto process-cutoff edge function\n(10:00 AM or 5:00 PM EAT);
:Authenticate FUNCTIONS_SECRET header;
:Query preOrders\n(mealSlot = target, status = scheduled, scheduledDate = today);
while (More pre-orders to process?) is (yes)
  :Read users/{userId}.walletBalance;
  if (balance >= pre-order total?) then (yes)
    :runTransaction()\nDeduct walletBalance\nWrite walletTransaction (system)\nSet preOrder.status = confirmed;
    :Send FCM confirmation\nnotification to student;
    if (Pre-order is recurring?) then (yes)
      :Create next occurrence\npreOrder document\n(next matching weekday);
    endif
  else (no)
    :Set preOrder.status = cancelled;
    :Send FCM "Insufficient funds —\npre-order cancelled" notification;
  endif
endwhile (no)
:Log execution result;
stop
@enduml
```

---

## Figure 19: Activity Diagram — Full Order Lifecycle (Swimlanes)

```plantuml
@startuml Figure19_ActivityDiagram
skinparam backgroundColor white
skinparam defaultFontName Arial
title Figure 19: Activity Diagram — Order & Pre-order Lifecycle

|Student|
start
:Login via Firebase Auth;
:Browse Menu (filter by category);

fork
  |Student|
  :Add items to Cart;
  :Select payment method;
  if (Wallet & balance insufficient?) then (yes)
    :View insufficient funds error;
    stop
  endif
  :Place Order;

  |System|
  :Write order (status = Pending) to Firestore;
  :Calculate estimated wait time\n(activeOrders × 5 min);
  :Stream status update to student;

  |Staff|
  :View new order on StaffOrders screen\n(real-time snapshot listener);
  :Mark order Preparing;

  |System|
  :Update status → Preparing\nStream to student;

  |Staff|
  :Mark order Ready;
  :Android calls notify-order-ready\n(HTTP POST to Supabase);

  |System|
  :Update status → Ready;
  :Send FCM push notification\n(notify-order-ready edge function);

  |Student|
  :Receive push notification;
  :Collect order at counter;

fork again
  |Student|
  :Open Pre-orders tab → FAB;
  :Set meal type, pickup time,\nrecurring option → Save;

  |System|
  :Write preOrder (status = scheduled)\nto Firestore;
  :At cut-off time: process-cutoff\nedge function runs;
  :runTransaction() — deduct wallet\n+ confirm OR cancel preOrder;
  :Send FCM confirmation\nor cancellation notification;

  |Student|
  :Receive notification;

end fork

|Staff|
:Wallet operations:\nreceive cash → top up student wallet\n(creditWallet with staffId);

|System|
:runTransaction() — update walletBalance\nwrite walletTransaction with staffId;

stop
@enduml
```

---

## Figure 20a: Sequence Diagram A — Regular Order Placement

```plantuml
@startuml Figure20a_SequenceDiagramA
skinparam backgroundColor white
skinparam defaultFontName Arial
skinparam sequenceArrowThickness 1.5
skinparam sequenceParticipant underline
title Figure 20a: Sequence Diagram — Regular Order Placement

participant "Student\n(Android)" as Student
participant "FirebaseAuth" as Auth
participant "CartViewModel" as Cart
participant "FirestoreRepository" as Repo
participant "Firestore" as FS
participant "Staff\n(Android)" as Staff
participant "notify-order-ready\n(Supabase)" as FCMFn
participant "FCMService\n(Firebase)" as FCM

Student -> Auth : signIn(email, password)
Auth --> Student : authResult (uid, role = student)

Student -> Repo : loadMenu()
Repo -> FS : addSnapshotListener(menuItems)
FS --> Student : menuItems stream (real-time)

Student -> Cart : addItem(menuItem, qty)
Student -> Cart : setPaymentMethod(wallet)
Student -> Repo : placeOrder(cart, paymentMethod)
Repo -> FS : runTransaction()\n[deduct walletBalance\n+ write order(status=Pending)\n+ write walletTransaction]
FS --> Student : order confirmation (orderId)

Student -> Repo : listenToOrder(orderId)
FS --> Staff : new order snapshot
Staff -> Repo : updateOrderStatus(orderId, Preparing)
FS --> Student : status update → Preparing

Staff -> Repo : updateOrderStatus(orderId, Ready)
FS --> Student : status update → Ready
Staff -> FCMFn : HTTP POST\n(orderId, studentId)
FCMFn -> FS : read users/{studentId}.fcmToken
FCMFn -> FCM : send(fcmToken, "Your order is ready")
FCM --> Student : FCM push notification

@enduml
```

---

## Figure 20b: Sequence Diagram B — Pre-order Cut-off

```plantuml
@startuml Figure20b_SequenceDiagramB
skinparam backgroundColor white
skinparam defaultFontName Arial
skinparam sequenceArrowThickness 1.5
title Figure 20b: Sequence Diagram — Pre-order Cut-off Execution

participant "cron-job.org" as Cron
participant "process-cutoff\n(Supabase Edge Fn)" as Fn
participant "Firestore\n(Admin SDK — REST)" as FS
participant "FCMService\n(Firebase Admin)" as FCM

Cron -> Fn : HTTP POST\n{mealSlot: "lunch"}\n(10:00 AM EAT)
note right of Fn : Authenticates\nFUNCTIONS_SECRET header

Fn -> FS : query preOrders\n(mealSlot=lunch,\nstatus=scheduled, today)
FS --> Fn : [preOrder1, preOrder2, ...]

loop for each preOrder
  Fn -> FS : read users/{userId}.walletBalance
  alt balance >= total
    Fn -> FS : runTransaction()\n[deduct walletBalance\n+ write walletTransaction\n+ set preOrder.status=confirmed]
    Fn -> FCM : send(fcmToken, "Pre-order confirmed")
    opt recurring == true
      Fn -> FS : write next preOrder\n(next matching weekday)
    end
  else balance < total
    Fn -> FS : set preOrder.status = cancelled
    Fn -> FCM : send(fcmToken,\n"Insufficient funds — pre-order cancelled")
  end
end

Fn --> Cron : 200 OK (execution complete)

@enduml
```

---

## Figure 21: Class Diagram

```plantuml
@startuml Figure21_ClassDiagram
skinparam classAttributeIconSize 0
skinparam backgroundColor white
skinparam defaultFontName Arial
title Figure 21: Class Diagram — Android Application

package "Model" {
  class MenuItem {
    +id: String
    +name: String
    +category: String
    +price: double
    +available: boolean
    +imageUrl: String
  }
  class Order {
    +id: String
    +userId: String
    +items: List<OrderItem>
    +status: String
    +paymentMethod: String
    +total: double
    +pickupTime: Timestamp
    +createdAt: Timestamp
  }
  class OrderItem {
    +menuItemId: String
    +name: String
    +price: double
    +quantity: int
  }
  class PreOrder {
    +id: String
    +userId: String
    +items: List<OrderItem>
    +mealType: String
    +pickupTime: Timestamp
    +recurring: boolean
    +dayOfWeek: int
    +status: String
    +total: double
    +createdAt: Timestamp
  }
  class User {
    +uid: String
    +name: String
    +studentId: String
    +email: String
    +role: String
    +walletBalance: double
    +fcmToken: String
    +firstLogin: boolean
    +createdAt: Timestamp
  }
  class WalletTransaction {
    +id: String
    +userId: String
    +type: String
    +amount: double
    +description: String
    +staffId: String
    +createdAt: Timestamp
  }
}

package "ViewModel" {
  class CartViewModel {
    -cartItems: MutableLiveData<List<CartItem>>
    -paymentMethod: String
    +addItem(item: MenuItem): void
    +removeItem(menuItemId: String): void
    +updateQuantity(menuItemId: String, qty: int): void
    +clearCart(): void
    +setPaymentMethod(method: String): void
    +getTotal(): double
  }
  class WalletViewModel {
    -balance: MutableLiveData<Double>
    -transactions: MutableLiveData<List<WalletTransaction>>
    +loadWallet(userId: String): void
  }
}

package "Repository" {
  class FirestoreRepository {
    +loadMenu(): void
    +placeOrder(cart: List, method: String): void
    +listenToOrders(userId: String): void
    +updateOrderStatus(orderId: String, status: String): void
    +notifyOrderReady(orderId: String): void
    +schedulePreOrder(preOrder: PreOrder): void
    +deductWallet(userId: String, amount: double): void
    +creditWallet(userId: String, amount: double, staffId: String): void
    +deductWalletByStaff(userId: String, amount: double, staffId: String): void
    +loadWalletTransactions(userId: String): void
  }
}

package "Fragments (Student)" {
  class MenuFragment
  class CartFragment
  class OrdersFragment
  class PreOrdersFragment
  class ProfileFragment
}

package "Fragments (Staff)" {
  class StaffOrdersFragment
  class StaffMenuFragment
  class StaffWalletFragment
}

package "Adapters" {
  class MenuAdapter
  class CartAdapter
  class OrderAdapter
  class PreOrderAdapter
  class WalletTransactionAdapter
}

' Model relationships
Order       "1" *-- "1..*" OrderItem
PreOrder    "1" *-- "0..*" OrderItem

' Fragment → Repository
MenuFragment          --> FirestoreRepository
CartFragment          --> FirestoreRepository
CartFragment          --> CartViewModel
CartFragment          --> WalletViewModel
OrdersFragment        --> FirestoreRepository
PreOrdersFragment     --> FirestoreRepository
ProfileFragment       --> WalletViewModel
StaffOrdersFragment   --> FirestoreRepository
StaffMenuFragment     --> FirestoreRepository
StaffWalletFragment   --> FirestoreRepository

' Fragment → Adapter
MenuFragment          --> MenuAdapter
CartFragment          --> CartAdapter
OrdersFragment        --> OrderAdapter
PreOrdersFragment     --> PreOrderAdapter
ProfileFragment       --> WalletTransactionAdapter

' Repository maps to models
FirestoreRepository ..> MenuItem          : maps
FirestoreRepository ..> Order             : maps
FirestoreRepository ..> PreOrder          : maps
FirestoreRepository ..> User              : maps
FirestoreRepository ..> WalletTransaction : maps

@enduml
```

---

## Figure 22: Wireframes

> **[NOT PLANTUML — placeholder]**
> Design manually in Figma / draw.io. Required screens:
>
> **Android app (Student):** Menu (ChipGroup filter bar + grid of item cards with image, name, price, Add button), Cart (item list with quantity stepper, wallet balance chip, estimated wait label, Place Order button), Orders (Active/History tabs, status chips: grey=Pending / blue=Preparing / green=Ready), Pre-orders (card list with meal type badge + Scheduled chip, FAB to add), Profile/Wallet (balance card, transaction list).
>
> **Android app (Staff):** StaffOrders (order cards with student name, items, total, Start Preparing / Mark Ready buttons), StaffMenu (item list with availability toggle switch), StaffWallet (student ID input, amount input, Top Up / Deduct buttons).
>
> **Next.js Admin Panel:** Login page, Menu Management table (add/edit/delete rows), Staff Account Creation form, Users Directory (students tab + staff/admin tab).
