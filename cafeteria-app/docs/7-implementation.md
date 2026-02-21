# 7 — Implementation Guide

This doc defines the recommended build order, setup steps, and concrete patterns for
turning the specs in docs 1–6 into working code. Follow the order below — each layer
depends on the one before it.

---

## Step 0 — Android Studio project setup

1. Create a new project: **Empty Activity**, package `com.usiu.cafeteria`, language Java,
   min SDK 26 (Android 8.0), target SDK 34.
2. Delete the auto-generated `activity_main.xml` content and `MainActivity.java` body —
   you will rewrite them from scratch.
3. In `build.gradle (:app)` add all dependencies from `docs/1-overview.md §Key dependencies`.
   Use the Firebase BOM so versions stay in sync:
   ```groovy
   implementation platform('com.google.firebase:firebase-bom:33.x.x')
   ```
4. Add the Google Services plugin to both `build.gradle` files (project and app level).
5. In the Firebase console:
   - Create a project named `usiu-cafeteria`
   - Add an Android app (package `com.usiu.cafeteria`)
   - Download `google-services.json` → place in `app/`
   - Enable **Email/Password** auth, **Firestore**, **Cloud Messaging**, **Cloud Functions**
6. Run a blank build to confirm Firebase is linked before writing any app code.

---

## Step 1 — Resources (colors, strings, themes)

Do this first so every subsequent layout compiles without errors.

### `res/values/colors.xml`
```xml
<color name="navy">#002147</color>
<color name="gold">#CFB991</color>
<color name="background">#FAFAFA</color>
<color name="surface">#FFFFFF</color>
```

### `res/values/themes.xml`
Base the theme on `Theme.Material3.Light.NoActionBar`. Override:
- `colorPrimary` → `@color/navy`
- `colorSecondary` → `@color/gold`
- `android:windowBackground` → `@color/background`

### `res/values/strings.xml`
Define every user-visible string referenced in layouts. At minimum:
`app_name`, `nav_menu`, `nav_cart`, `nav_orders`, `nav_preorders`, `nav_profile`,
`nav_staff_orders`, `nav_staff_menu`, `nav_staff_wallet`, `btn_place_order`,
`btn_add_to_cart`, `btn_schedule`, `btn_top_up`, `label_wallet_balance`.

---

## Step 2 — Model classes

All models live in `java/com/usiu/cafeteria/models/`. Each must have:
- A **no-arg constructor** (Firestore SDK deserialises with it)
- **Public getters and setters** for every field
- Field names that **exactly match** the Firestore field names in `docs/3-data-model.md`

### Build order
```
User.java → MenuItem.java → OrderItem.java → Order.java → PreOrder.java → WalletTransaction.java
```

`OrderItem` before `Order` because `Order` embeds a `List<OrderItem>`.

### Pattern for each model
```java
public class MenuItem {
    private String itemId;
    private String name;
    private String description;
    private double price;
    private String category;    // "breakfast" | "lunch" | "dinner"
    private String imageUrl;
    private boolean available;
    private com.google.firebase.Timestamp createdAt;

    public MenuItem() {}       // required by Firestore

    // getters and setters for every field
}
```

---

## Step 3 — FirestoreRepository

Single class: `java/com/usiu/cafeteria/repository/FirestoreRepository.java`

**Rules:**
- Every Firestore call in the entire app lives here — nothing else touches Firestore directly.
- Methods return `Task<>` objects or accept callback interfaces — never block the main thread.
- Group methods by collection for readability.

### Skeleton structure
```java
public class FirestoreRepository {
    private static FirestoreRepository instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirestoreRepository getInstance() {
        if (instance == null) instance = new FirestoreRepository();
        return instance;
    }

    // Users
    public Task<DocumentSnapshot> getUser(String uid) { ... }
    public Task<Void> createUser(User user) { ... }
    public Task<QuerySnapshot> getUserByStudentId(String studentId) { ... }

    // Menu Items
    public ListenerRegistration listenToMenuItems(EventListener<QuerySnapshot> listener) { ... }
    public Task<Void> addMenuItem(MenuItem item) { ... }
    public Task<Void> updateMenuItem(String itemId, Map<String, Object> updates) { ... }
    public Task<Void> updateMenuItemAvailability(String itemId, boolean available) { ... }

    // Orders
    public Task<Void> placeOrderCash(Order order) { ... }
    public Task<Void> placeOrderWithWalletDeduction(Order order) { ... }
    public ListenerRegistration listenToMyOrders(String userId, EventListener<QuerySnapshot> l) { ... }
    public ListenerRegistration listenToAllActiveOrders(EventListener<QuerySnapshot> l) { ... }
    public Task<Void> updateOrderStatus(String orderId, String newStatus) { ... }
    public Task<QuerySnapshot> getActiveOrderCount() { ... }

    // Pre-Orders
    public Task<Void> createPreOrder(PreOrder preOrder) { ... }
    public ListenerRegistration listenToMyPreOrders(String userId, EventListener<QuerySnapshot> l) { ... }
    public Task<Void> cancelPreOrder(String preOrderId) { ... }

    // Wallet
    public Task<Void> topUpWallet(String userId, double amount) { ... }
    public ListenerRegistration listenToWalletTransactions(String userId, EventListener<QuerySnapshot> l) { ... }
}
```

### placeOrderWithWalletDeduction — full pattern
```java
public Task<Void> placeOrderWithWalletDeduction(Order order) {
    String uid = auth.getCurrentUser().getUid();
    DocumentReference userRef = db.collection("users").document(uid);
    DocumentReference orderRef = db.collection("orders").document();
    DocumentReference txRef   = db.collection("walletTransactions").document();

    order.setOrderId(orderRef.getId());

    return db.runTransaction(transaction -> {
        DocumentSnapshot userSnap = transaction.get(userRef);
        double balance = userSnap.getDouble("walletBalance");
        if (balance < order.getTotalAmount()) {
            throw new FirebaseFirestoreException(
                "Insufficient funds", FirebaseFirestoreException.Code.ABORTED);
        }
        transaction.update(userRef, "walletBalance", balance - order.getTotalAmount());
        transaction.set(orderRef, order);

        WalletTransaction wt = new WalletTransaction();
        wt.setTxId(txRef.getId());
        wt.setUserId(uid);
        wt.setType("deduction");
        wt.setAmount(order.getTotalAmount());
        wt.setDescription("Order #" + order.getOrderId());
        wt.setRelatedOrderId(order.getOrderId());
        wt.setCreatedAt(FieldValue.serverTimestamp());
        transaction.set(txRef, wt);
        return null;
    });
}
```

---

## Step 4 — ViewModels

Three ViewModels in `java/com/usiu/cafeteria/viewmodels/`.

### CartViewModel
- Holds `MutableLiveData<List<OrderItem>> cartItems`
- Methods: `addItem(MenuItem)`, `removeItem(String itemId)`, `updateQuantity(String, int)`, `clearCart()`
- Exposes `LiveData<Double> subtotal` computed from cartItems
- Exposes `LiveData<Integer> estimatedWaitMin` populated by a one-shot repository call
- Extend `AndroidViewModel` so it can hold application context if needed

### WalletViewModel
- Holds `MutableLiveData<Double> walletBalance`
- Holds `MutableLiveData<List<WalletTransaction>> transactions`
- Starts a Firestore listener in constructor, removes it in `onCleared()`

### OrdersViewModel
- Holds `MutableLiveData<List<Order>> myOrders` (student view)
- Holds `MutableLiveData<List<Order>> allActiveOrders` (staff view)
- Starts listeners in constructor, removes in `onCleared()`

### ViewModel listener pattern
```java
public class WalletViewModel extends ViewModel {
    private final MutableLiveData<Double> walletBalance = new MutableLiveData<>();
    private ListenerRegistration listener;

    public WalletViewModel() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listener = FirestoreRepository.getInstance()
            .listenToWalletTransactions(uid, (snap, e) -> {
                if (snap != null) {
                    // process snap, update walletBalance LiveData
                }
            });
    }

    public LiveData<Double> getWalletBalance() { return walletBalance; }

    @Override
    protected void onCleared() { listener.remove(); }
}
```

---

## Step 5 — Authentication screens

### LoginActivity
Layout `activity_login.xml`: email EditText, password EditText, Login button, Register link.

Logic:
1. `FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)`
2. On success → read `users/{uid}` from Firestore for role
3. `role == "staff"` → start `StaffMainActivity`, `finish()`
4. `role == "student"` → start `MainActivity`, `finish()`
5. On failure → Snackbar with friendly message (not raw exception text)

### RegisterActivity
Layout `activity_register.xml`: name, studentId, email, password, Register button.

Logic:
1. `FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)`
2. On success → build `User` object (`role="student"`, `walletBalance=0.0`)
3. Write to `users/{uid}` via `FirestoreRepository.createUser()`
4. Start `MainActivity`, `finish()`

---

## Step 6 — MainActivity (student nav)

### Layout `activity_main.xml`
```xml
<FrameLayout android:id="@+id/fragment_container" ... />
<BottomNavigationView android:id="@+id/bottom_nav"
    app:menu="@menu/bottom_nav_student" ... />
```

### Logic
- On create: if `FirebaseAuth.getCurrentUser() == null` → start `LoginActivity`, `finish()`
- Add all 5 student fragments once in `onCreate` using hide/show
- `BottomNavigationView.setOnItemSelectedListener` → call `showFragment(target)`

### Fragment hide/show pattern
```java
// In onCreate — add all fragments once
getSupportFragmentManager().beginTransaction()
    .add(R.id.fragment_container, menuFrag,     "menu")
    .add(R.id.fragment_container, cartFrag,     "cart")
    .add(R.id.fragment_container, ordersFrag,   "orders")
    .add(R.id.fragment_container, preOrderFrag, "preorders")
    .add(R.id.fragment_container, profileFrag,  "profile")
    .hide(cartFrag).hide(ordersFrag).hide(preOrderFrag).hide(profileFrag)
    .commit();

// On nav item selected
private void showFragment(Fragment target) {
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    for (Fragment f : allFragments) {
        if (f == target) ft.show(f); else ft.hide(f);
    }
    ft.commit();
}
```

### Shared ViewModels — obtain in MainActivity
```java
cartViewModel   = new ViewModelProvider(this).get(CartViewModel.class);
walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);
ordersViewModel = new ViewModelProvider(this).get(OrdersViewModel.class);
```

Fragments access them with `new ViewModelProvider(requireActivity())` — same scope, same instance.

---

## Step 7 — Student fragments

Build in this order:

### 7a. MenuFragment
- Observe `LiveData<List<MenuItem>>` (from a MenuViewModel or direct repo listener)
- Chip filter group (Breakfast / Lunch / Dinner) → filter list by category
- `MenuAdapter` binds: name, price, description, Glide image, availability greying
- "Add to Cart" click → `cartViewModel.addItem(menuItem)` → Snackbar

### 7b. CartFragment
- Observe `cartViewModel.cartItems` → bind `CartAdapter`
- Observe `cartViewModel.subtotal` → update subtotal TextView
- Observe `cartViewModel.estimatedWaitMin` → update wait-time TextView
- Call `cartViewModel.refreshEstimatedWait()` in `onResume()`
- Payment toggle: two `MaterialButton` in a `MaterialButtonToggleGroup`
- "Place Order":
  - Wallet → `repo.placeOrderWithWalletDeduction(order)` → clear cart, navigate to Orders
  - Cash → `repo.placeOrderCash(order)` → clear cart, navigate to Orders
  - "Insufficient funds" exception → show `MaterialAlertDialog`, do nothing else

### 7c. OrdersFragment
- Two tabs (Active / History) via `TabLayout` + `ViewPager2` or manual toggle
- Observe `ordersViewModel.myOrders` → split by status → bind `OrdersAdapter`
- Status chip colours from `docs/6-ui.md`

### 7d. PreOrdersFragment
- Observe pre-orders list → bind `PreOrdersAdapter`
- FAB → `BottomSheetDialogFragment` with new pre-order form:
  - Meal slot RadioGroup, date picker, item multi-select, recurring toggle + day chips
  - Validate then call `repo.createPreOrder(preOrder)` → dismiss, show Snackbar
- Cancel button on "Scheduled" item → confirm dialog → `repo.cancelPreOrder(id)`

### 7e. ProfileWalletFragment
- Observe `walletViewModel.walletBalance` → update balance display
- Observe `walletViewModel.transactions` → bind `WalletTransactionAdapter`
- Logout button → `FirebaseAuth.signOut()` → start `LoginActivity`, `finish()`

---

## Step 8 — Adapters

One adapter per list, all extend `RecyclerView.Adapter<VH>`.

| Adapter | Item layout | Key bindings |
|---|---|---|
| `MenuAdapter` | `item_menu.xml` | name, price, Glide image, available state, add-to-cart click |
| `CartAdapter` | `item_cart.xml` | name, qty stepper `[−][qty][+]`, line total, remove click |
| `OrdersAdapter` | `item_order.xml` | order id, status chip, items summary, total, timestamp |
| `PreOrdersAdapter` | `item_preorder.xml` | slot, date, status chip, items, total |
| `WalletTransactionAdapter` | `item_wallet_tx.xml` | type icon (up=green / down=red), description, amount, date |

### Glide pattern (MenuAdapter)
```java
Glide.with(holder.itemView.getContext())
    .load(item.getImageUrl())
    .placeholder(R.drawable.ic_food_placeholder)
    .error(R.drawable.ic_food_placeholder)
    .into(holder.imageView);
```

Use `submitList(List)` if the adapter extends `ListAdapter<T, VH>` (DiffUtil) — gives free
animated inserts/removes without calling `notifyDataSetChanged()`.

---

## Step 9 — Staff screens

### StaffMainActivity
Mirror of `MainActivity` with `bottom_nav_staff.xml` (3 items) and the 3 staff fragments.
Role routing in `LoginActivity` determines which activity is launched.

### StaffOrdersFragment
- Observe `ordersViewModel.allActiveOrders` → bind an adapter
- Each card shows status-dependent action button:
  - `pending` → "Start Preparing"
  - `preparing` → "Mark Ready"
  - `ready` → "Mark Collected" (cash orders get a confirmation dialog first)
- Button click → `repo.updateOrderStatus(orderId, newStatus)`

### StaffMenuFragment
- Observe menu items → bind adapter
- Availability `SwitchMaterial` on each card → `repo.updateMenuItemAvailability(itemId, checked)`
- Card tap → edit dialog (pre-filled), confirm → `repo.updateMenuItem(itemId, updates)`
- FAB → add-item dialog, confirm → `repo.addMenuItem(item)`

### StaffWalletFragment
- "Find Student" button → `repo.getUserByStudentId(studentId)` → show name + balance
- "Confirm Top-up" button → `repo.topUpWallet(userId, amount)` → Snackbar with new balance

---

## Step 10 — Cloud Functions

Located in `functions/` at the project root (sibling of the Android `app/` directory).

### Setup
```bash
firebase init functions   # choose JavaScript, Node 18
npm install               # inside functions/
```

### `functions/index.js` — key structure
```js
const { onSchedule }        = require("firebase-functions/v2/scheduler");
const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
admin.initializeApp();
const db = admin.firestore();

// 10:00 AM EAT = 07:00 UTC
exports.processLunchPreOrders = onSchedule("0 7 * * *", async () => {
    await processSlot("lunch");
});

// 5:00 PM EAT = 14:00 UTC
exports.processDinnerPreOrders = onSchedule("0 14 * * *", async () => {
    await processSlot("dinner");
});

async function processSlot(mealSlot) {
    const today = new Date().toISOString().slice(0, 10); // "YYYY-MM-DD"
    const snap = await db.collection("preOrders")
        .where("mealSlot", "==", mealSlot)
        .where("scheduledDate", "==", today)
        .where("status", "==", "scheduled")
        .get();

    for (const doc of snap.docs) {
        const preOrder = doc.data();
        const userRef  = db.collection("users").doc(preOrder.userId);
        await db.runTransaction(async tx => {
            const userSnap = await tx.get(userRef);
            const balance  = userSnap.data().walletBalance;
            if (balance >= preOrder.totalAmount) {
                tx.update(userRef, { walletBalance: balance - preOrder.totalAmount });
                tx.update(doc.ref, { status: "confirmed" });
                const txRef = db.collection("walletTransactions").doc();
                tx.set(txRef, {
                    txId: txRef.id, userId: preOrder.userId,
                    type: "deduction", amount: preOrder.totalAmount,
                    description: `Pre-order #${doc.id} — ${mealSlot}`,
                    relatedOrderId: doc.id,
                    createdAt: admin.firestore.FieldValue.serverTimestamp()
                });
            } else {
                tx.update(doc.ref, { status: "cancelled" });
            }
        });
        // After transaction: send FCM + create next recurring doc
        // See docs/5-firebase.md for FCM and recurring scheduling logic
    }
}

// Notify student when staff marks order "ready"
exports.onOrderReady = onDocumentUpdated("orders/{orderId}", async event => {
    const before = event.data.before.data();
    const after  = event.data.after.data();
    if (before.status !== "ready" && after.status === "ready") {
        const userSnap = await db.collection("users").doc(after.userId).get();
        const token = userSnap.data().fcmToken;
        if (token) {
            await admin.messaging().send({
                token,
                notification: {
                    title: "Order Ready!",
                    body:  "Your order is ready for collection at the cafeteria."
                }
            });
        }
    }
});
```

Deploy: `firebase deploy --only functions`

---

## Step 11 — Firestore indexes

Create these composite indexes in the Firebase console (Firestore → Indexes → Composite):

| Collection | Fields | Purpose |
|---|---|---|
| `orders` | `userId` ASC, `createdAt` DESC | Student order history |
| `orders` | `status` ASC, `createdAt` ASC | Staff active orders list |
| `preOrders` | `userId` ASC, `createdAt` DESC | Student pre-order list |
| `preOrders` | `mealSlot` ASC, `scheduledDate` ASC, `status` ASC | Cloud Functions query |
| `walletTransactions` | `userId` ASC, `createdAt` DESC | Wallet transaction history |

During development, Firestore throws an exception with a direct URL to create the missing
index when a query runs without one — you can create them reactively that way too.

---

## Step 12 — Firestore security rules

Deploy the rules specified in `docs/5-firebase.md`. Test with the Firebase emulator:
```bash
firebase emulators:start --only firestore
```

Key scenarios to test:
- Student cannot write `walletBalance` on their own user doc
- Student cannot read another student's orders or pre-orders
- Staff can read any user doc
- Pre-order cancellation blocked if `status != "scheduled"`

---

## Testing checkpoints

Work through these in sequence — each verifies a meaningful slice of the app.

| # | Verify | Method |
|---|---|---|
| 1 | Firebase linked | App launches; no crash in Logcat |
| 2 | Register + login | Account appears in Auth console; user doc in Firestore |
| 3 | Role routing | staff email → staff nav; student email → student nav |
| 4 | Menu loads | Add docs to Firestore manually; check MenuFragment displays them |
| 5 | Add to cart | Cart badge increments; CartFragment shows item with correct total |
| 6 | Place order (cash) | Order doc in Firestore; OrdersFragment shows it in Active tab |
| 7 | Place order (wallet) | walletBalance decreases; walletTransactions doc created atomically |
| 8 | Insufficient wallet | Dialog shown; no order created; balance unchanged |
| 9 | Real-time order status | Staff changes status → student OrdersFragment updates without refresh |
| 10 | Pre-order scheduling | preOrders doc created with `status="scheduled"` |
| 11 | Cloud Function | Test via `firebase functions:shell` or wait for scheduled time |
| 12 | FCM notification | "Order Ready" notification received on test device |
| 13 | Staff wallet top-up | Balance increases instantly on student ProfileWalletFragment |
| 14 | Staff menu toggle | Availability change → item greyed out on student MenuFragment in real time |

---

## Common pitfalls

| Pitfall | Fix |
|---|---|
| Firestore deserialization fails silently | Model must have a no-arg constructor; field names must exactly match Firestore |
| Wallet balance race condition | Never use plain `update()` for wallet — always `runTransaction()` |
| Fragment state lost on nav switch | Use hide/show not replace; add all fragments in `onCreate` exactly once |
| LiveData not shared between fragments | Obtain ViewModel from the activity scope: `new ViewModelProvider(requireActivity())` |
| Cloud Function misses today's pre-orders | Function runs in UTC — `scheduledDate` must be today's date in UTC+3 (EAT) |
| Stale FCM token | Refresh in `FirebaseMessagingService.onNewToken()` and write to `users/{uid}.fcmToken` |
| Composite index missing | Firestore exception includes a URL to create the index — use it |
