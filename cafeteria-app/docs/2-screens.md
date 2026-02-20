# 2 — Screens

## Student screens (MainActivity bottom nav)

### 1. MenuFragment
**Tab icon:** fork-and-knife  **Label:** Menu

**What it shows:**
- Horizontal chip group at the top: "Breakfast" | "Lunch" | "Dinner" — filters the list
- RecyclerView of menu item cards, each showing:
  - Item name
  - Price (KES X.XX)
  - Short description
  - Image (Glide, placeholder drawable if no URL)
  - "Add to Cart" button — disabled and greyed out if `available == false`
- Items with `available == false` still show but with an "Unavailable" label

**Interactions:**
- Tap "Add to Cart" → adds 1 quantity to `CartViewModel` (shows snackbar "Added to cart")
- Tap item card (not button) → optionally show a bottom sheet with full description
- Chip filter → filters the RecyclerView in-place (no network call, filter already-loaded list)

**Data source:** `FirestoreRepository.listenToMenuItems()` → `MenuViewModel` LiveData

---

### 2. CartFragment
**Tab icon:** shopping-cart  **Label:** Cart  (badge shows item count)

**What it shows:**
- RecyclerView of cart items, each showing:
  - Item name + price per unit
  - Quantity stepper: [−] [2] [+]
  - Line total (price × quantity)
  - Remove (trash icon) button
- Divider then summary section:
  - Subtotal
  - "Estimated wait: ~X min" (computed locally — see docs/4-features.md)
- Payment method toggle (RadioGroup or SegmentedButton):
  - "Pay from Wallet (Balance: KES X.XX)"
  - "Pay Cash at Counter"
- "Place Order" button (full-width, primary colour)

**Interactions:**
- [+] / [−] → update quantity in `CartViewModel`; remove item if quantity reaches 0
- Trash icon → remove item from cart
- "Place Order":
  - If wallet selected: check `WalletViewModel.balance >= subtotal`; if not → show dialog "Insufficient wallet balance. Top up at the counter."
  - If wallet selected and sufficient: call `FirestoreRepository.placeOrderWithWalletDeduction()` (uses Firestore transaction)
  - If cash selected: call `FirestoreRepository.placeOrderCash()`
  - On success: clear cart, navigate to OrdersFragment, show snackbar "Order placed!"
  - On failure: show error snackbar

**Data source:** `CartViewModel` (in-memory, no Firestore until order is placed)

---

### 3. OrdersFragment
**Tab icon:** receipt  **Label:** Orders

**What it shows:**
- Two tabs: "Active" | "History"
- **Active tab:** RecyclerView of orders where status is Pending, Preparing, or Ready
  - Each card shows: order ID (short), item summary, total, status chip, time placed
  - Status chip colours: Pending=amber, Preparing=blue, Ready=green
- **History tab:** RecyclerView of Collected orders (most recent first)

**Interactions:**
- Real-time listener → status updates appear without user action
- Tap a card → expand to show full item list (accordion or new fragment)

**Data source:** `FirestoreRepository.listenToStudentOrders(userId)` → `OrdersViewModel`

---

### 4. PreOrdersFragment
**Tab icon:** calendar  **Label:** Pre-order

**What it shows:**
- FloatingActionButton (bottom-right): "New Pre-order"
- RecyclerView of this student's pre-orders, each showing:
  - Meal slot (Lunch / Dinner), scheduled date, items summary, total, status chip
  - Status: Scheduled (grey), Confirmed (green), Cancelled (red)

**New Pre-order bottom sheet / dialog contains:**
- Meal slot picker: Lunch | Dinner (radio)
- Date picker (default = today if before cut-off, else tomorrow)
- Item selection: same menu list filtered to lunch or dinner items, with Add buttons
- Recurring toggle (Switch): "Repeat weekly"
  - If ON → show day-of-week checkboxes (Mon Tue Wed Thu Fri Sat Sun)
- Order summary and total
- "Schedule Pre-order" button (wallet-only — show label "Payment will be deducted from wallet at cut-off time")

**Interactions:**
- "Schedule Pre-order" → validate (at least 1 item, date not in past, before cut-off) → `FirestoreRepository.createPreOrder()`
- Tap existing pre-order card → show cancel option (only if status == Scheduled)

**Data source:** `FirestoreRepository.listenToStudentPreOrders(userId)`

---

### 5. ProfileWalletFragment
**Tab icon:** account-circle  **Label:** Profile

**What it shows:**
- User name and email (top card)
- Wallet card:
  - Large balance display: "KES 450.00"
  - Info text: "Top up at the cafeteria counter (cash)"
- Transaction history header
- RecyclerView of `walletTransactions` (most recent first), each row:
  - Type icon: ↑ top-up (green) / ↓ deduction (red)
  - Description (e.g., "Order #XYZ3" or "Top-up by staff")
  - Amount (+KES 200 or −KES 85)
  - Date/time
- Logout button (bottom)

**Interactions:**
- Logout → `FirebaseAuth.signOut()` → start `LoginActivity`, clear back stack

**Data source:**
- `WalletViewModel` for balance (live listener on `users/{uid}.walletBalance`)
- `FirestoreRepository.listenToWalletTransactions(userId)` for history

---

## Staff screens (StaffMainActivity or role-gated)

### S1. StaffOrdersFragment
**Label:** Orders

**What it shows:**
- RecyclerView of ALL orders with status Pending, Preparing, or Ready (all students)
- Each card shows: student name, order items, total, payment method, current status
- Status action button on each card:
  - Pending → "Start Preparing" button
  - Preparing → "Mark Ready" button
  - Ready → "Mark Collected" button (also triggers cash confirmation if paymentMethod == cash)

**Interactions:**
- Button tap → `FirestoreRepository.updateOrderStatus(orderId, newStatus)`
- Real-time listener — new orders appear automatically

---

### S2. StaffMenuFragment
**Label:** Menu

**What it shows:**
- RecyclerView of all menu items with name, price, category, availability toggle
- FAB: "Add Item"

**Interactions:**
- Availability toggle (Switch) → `FirestoreRepository.updateMenuItemAvailability(itemId, bool)`
- "Add Item" FAB → form dialog: name, description, price, category, imageUrl (optional)
- Tap existing item card → edit dialog (pre-filled)
- `FirestoreRepository.addMenuItem()` / `FirestoreRepository.updateMenuItem()`

---

### S3. StaffWalletFragment
**Label:** Wallet Top-up

**What it shows:**
- A simple form:
  - Student ID field (EditText)
  - Amount field (EditText, numeric)
  - "Find Student" button
  - After lookup: shows student name and current balance
  - "Confirm Top-up" button

**Interactions:**
- "Find Student" → query `users` collection where `studentId == input` → show name + balance
- "Confirm Top-up" → `FirestoreRepository.topUpWallet(userId, amount)`:
  - Adds amount to `users/{uid}.walletBalance`
  - Creates a `walletTransactions` document (type=topup)
- Show success snackbar with new balance
