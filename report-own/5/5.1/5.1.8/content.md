# 5.1.8 Class Diagram

A class diagram is a type of structural diagram in system design that illustrates the blueprint of an object-oriented system by showing the system's classes, their attributes, methods, and the relationships among them. It provides a static view of the system, helping developers understand how components interact and depend on one another. For the USIU Cafeteria Ordering System, the class diagram represents the Android application's Java class structure.

> [Figure 21: Class Diagram — Android fragment, adapter, ViewModel, model, and repository classes with relationships]

Figure 21 above shows the class diagram for the USIU Cafeteria Ordering System. The key classes are grouped by responsibility:

## Model Classes (Data)
- **MenuItem:** id, name, category, price, available, imageUrl
- **Order:** id, userId, items (List\<OrderItem\>), status, paymentMethod, total, pickupTime, createdAt
- **OrderItem:** menuItemId, name, price, quantity
- **PreOrder:** id, userId, items (List\<OrderItem\>), mealType, pickupTime, recurring, dayOfWeek, status, total, createdAt
- **User:** uid, name, studentId, email, role, walletBalance, deviceToken
- **WalletTransaction:** id, userId, type, amount, description, createdAt

## Fragment Classes (UI)
- **MenuFragment:** Observes menuItems via FirestoreRepository; uses MenuAdapter and CategoryAdapter; navigates to CartFragment
- **CartFragment:** Observes CartViewModel; displays wallet balance; calls FirestoreRepository.placeOrder()
- **OrdersFragment:** Observes active orders via FirestoreRepository; calculates estimatedWait(); uses OrderAdapter
- **PreOrdersFragment:** Reads/writes preOrders via FirestoreRepository; uses PreOrderAdapter
- **ProfileFragment:** Observes WalletViewModel; displays balance and transaction history
- **StaffOrdersFragment:** Observes all orders via FirestoreRepository; calls updateOrderStatus()
- **StaffMenuFragment:** Calls FirestoreRepository.updateItemAvailability()
- **StaffWalletFragment:** Calls FirestoreRepository.creditWallet()

## ViewModel Classes (State)
- **CartViewModel:** MutableLiveData\<List\<CartItem\>\>; addItem(), removeItem(), updateQuantity(), clearCart(), setPaymentMethod()
- **WalletViewModel:** MutableLiveData\<Double\> balance; MutableLiveData\<List\<WalletTransaction\>\>; loads from FirestoreRepository

## Adapter Classes (RecyclerView)
- **MenuAdapter, CategoryAdapter, CartAdapter, OrderAdapter, PreOrderAdapter, WalletTransactionAdapter**
  Each extends RecyclerView.Adapter; binds the relevant model class to a ViewHolder layout

## Repository Class (Data Access)
- **FirestoreRepository:** Central data access object; all Firestore reads and writes pass through this class. Key methods: loadMenu(), placeOrder(), listenToOrder(), updateOrderStatus(), schedulePreOrder(), deductWallet() (runTransaction), creditWallet() (runTransaction), loadWalletTransactions()

Relationships: Fragments depend on FirestoreRepository and ViewModel classes. ViewModels hold LiveData observed by Fragments. Adapters are owned by Fragments. FirestoreRepository maps Firestore documents to model class instances using Firestore's toObject() method.
