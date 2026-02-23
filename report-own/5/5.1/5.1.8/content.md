# 5.1.8 Class Diagram

A class diagram is a type of structural diagram in system design that illustrates the blueprint of an object-oriented system by showing the system's classes, their attributes, methods, and the relationships among them. It provides a static view of the system, helping developers understand how components interact and depend on one another. For the USIU Cafeteria Ordering System, the class diagram represents the Android application's Java class structure. The system also includes a separate web admin panel (Next.js, TypeScript) whose components are not represented here as they are not Java classes; a note on this is included at the end of this section.

> [Figure 21: Class Diagram — Android fragment, adapter, ViewModel, model, and repository classes with relationships]

Figure 21 above shows the class diagram for the USIU Cafeteria Ordering System. The key classes are grouped by responsibility:

## Model Classes (Data)
- **MenuItem:** id, name, category, price, available, imageUrl
- **Order:** id, userId, items (List\<OrderItem\>), status, paymentMethod, total, pickupTime, createdAt
- **OrderItem:** menuItemId, name, price, quantity
- **PreOrder:** id, userId, items (List\<OrderItem\>), mealType, pickupTime, recurring, dayOfWeek, status, total, createdAt
- **User:** uid, name, studentId, email, role, walletBalance, fcmToken, firstLogin, createdAt
- **WalletTransaction:** id, userId, type, amount, description, staffId, createdAt

## Fragment Classes (UI)
- **MenuFragment:** Observes menuItems via FirestoreRepository; uses MenuAdapter and CategoryAdapter; navigates to CartFragment
- **CartFragment:** Observes CartViewModel; displays wallet balance; calls FirestoreRepository.placeOrder()
- **OrdersFragment:** Observes active orders via FirestoreRepository; calculates estimatedWait(); uses OrderAdapter
- **PreOrdersFragment:** Reads/writes preOrders via FirestoreRepository; uses PreOrderAdapter
- **ProfileFragment:** Observes WalletViewModel; displays balance and transaction history
- **StaffOrdersFragment:** Observes all orders via FirestoreRepository; calls updateOrderStatus()
- **StaffMenuFragment:** Calls FirestoreRepository.updateItemAvailability()
- **StaffWalletFragment:** Calls FirestoreRepository.creditWallet() or FirestoreRepository.deductWallet(); records staffId on each transaction

## ViewModel Classes (State)
- **CartViewModel:** MutableLiveData\<List\<CartItem\>\>; addItem(), removeItem(), updateQuantity(), clearCart(), setPaymentMethod()
- **WalletViewModel:** MutableLiveData\<Double\> balance; MutableLiveData\<List\<WalletTransaction\>\>; loads from FirestoreRepository

## Adapter Classes (RecyclerView)
- **MenuAdapter, CategoryAdapter, CartAdapter, OrderAdapter, PreOrderAdapter, WalletTransactionAdapter**
  Each extends RecyclerView.Adapter; binds the relevant model class to a ViewHolder layout

## Repository Class (Data Access)
- **FirestoreRepository:** Central data access object; all Firestore reads and writes pass through this class. Key methods: loadMenu(), placeOrder(), listenToOrders(), updateOrderStatus(), notifyOrderReady() (HTTP POST to Supabase notify-order-ready edge function), schedulePreOrder(), deductWallet() (runTransaction), creditWallet() (runTransaction), deductWalletByStaff() (runTransaction, writes staffId), loadWalletTransactions()

Relationships: Fragments depend on FirestoreRepository and ViewModel classes. ViewModels hold LiveData observed by Fragments. Adapters are owned by Fragments. FirestoreRepository maps Firestore documents to model class instances using Firestore's toObject() method.

---

**Note — Admin Web Panel (out of scope for this Java class diagram):**
The system includes a separate web-based admin panel built with Next.js and TypeScript. Its components — login page, dashboard pages for menu management and staff account creation, Next.js API routes using the Firebase Admin SDK, and Tailwind CSS UI — are TypeScript/React components and are not represented in this Java class diagram. The admin panel interacts with the same Firestore database and Firebase Authentication instance as the Android app, governed by the same Firestore security rules.
