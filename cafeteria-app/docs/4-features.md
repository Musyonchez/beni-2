# 4 — Feature Specifications

## Feature A: Estimated Wait Time

### Purpose
Show students a realistic wait time estimate when they are reviewing their cart, so they can decide whether to order now or come back later.

### Calculation (done on the client, not server)
```
estimatedWaitMinutes = pendingAndPreparingOrderCount × AVG_PREP_TIME_PER_ORDER
```
- `AVG_PREP_TIME_PER_ORDER` = 5 minutes (constant, defined in code)
- `pendingAndPreparingOrderCount` = count of orders with status "pending" OR "preparing" in Firestore at the moment the cart is open

### Implementation steps
1. In `FirestoreRepository`, add `getActiveOrderCount(callback)`:
   - Query `orders` where `status in ["pending", "preparing"]`
   - Return count via callback / LiveData
2. In `CartViewModel`, compute `estimatedWaitMinutes = count * 5`
3. In `CartFragment`, observe and display: `"Estimated wait: ~X min"`
4. Refresh when the fragment is resumed (not a real-time listener — one-shot query is fine)

### Edge cases
- Count = 0 → display "Estimated wait: ~5 min" (one order's worth, not 0)
- If query fails → do not show the estimate (hide the TextView silently)

---

## Feature B: In-App Wallet

### Purpose
Students hold a KES balance in the app funded by cash paid at the counter to staff. They can use this balance to pay for regular orders and all pre-orders.

### Top-up (staff side)
1. Staff opens `StaffWalletFragment`, enters student ID and amount
2. `FirestoreRepository.topUpWallet(userId, amount)`:
   ```
   db.runTransaction(tx -> {
       DocumentSnapshot user = tx.get(userRef);
       double newBalance = user.getDouble("walletBalance") + amount;
       tx.update(userRef, "walletBalance", newBalance);
       // create walletTransactions document inside transaction
       tx.set(txRef, transactionDoc);
       return null;
   })
   ```

### Deduction for regular orders (student side)
1. Student taps "Place Order" with wallet selected
2. `FirestoreRepository.placeOrderWithWalletDeduction(order)`:
   ```
   db.runTransaction(tx -> {
       DocumentSnapshot user = tx.get(userRef);
       double balance = user.getDouble("walletBalance");
       if (balance < order.getTotalAmount()) throw new Exception("Insufficient funds");
       tx.update(userRef, "walletBalance", balance - order.getTotalAmount());
       tx.set(orderRef, orderDoc);
       tx.set(txRef, walletTxDoc);  // type=deduction
       return null;
   })
   ```
3. On transaction failure with "Insufficient funds" → show dialog, do NOT place order

### Deduction for pre-orders (Cloud Functions side — see docs/5-firebase.md)
- Pre-orders are always wallet-only
- Deduction happens at cut-off time, not at scheduling time
- If insufficient funds at cut-off → cancel pre-order + send FCM

### Rules
- Students can never deduct below 0 (enforced by transaction + Firestore rules)
- Students cannot top up their own wallet (Firestore rules block writes to walletBalance by role=student)
- All deductions and top-ups create a `walletTransactions` document

---

## Feature C: Smart Pre-Order

### Purpose
Students can schedule a meal in advance for a specific day and meal slot. The system confirms and deducts payment automatically at a cut-off time. Recurring pre-orders repeat weekly.

### Constraints
| Meal slot | Cut-off time (EAT) | Who can order |
|-----------|--------------------|---------------|
| Lunch     | 10:00 AM           | Students      |
| Dinner    | 5:00 PM            | Students      |
| Breakfast | N/A                | Not available |

### Scheduling flow (student)
1. Student opens `PreOrdersFragment` → taps FAB → fills form:
   - Select: Lunch or Dinner
   - Select date (must be today before cut-off, or a future date)
   - Add items from menu (same menu, any category — student picks what they want)
   - Optionally toggle "Recurring" and pick days of week
2. App validates:
   - At least 1 item
   - Date is valid (not in the past)
   - If today: current time < cut-off for the selected slot
3. `FirestoreRepository.createPreOrder(preOrder)`:
   - status = "scheduled"
   - payment NOT deducted yet
4. Snackbar: "Pre-order scheduled! Payment will be deducted at [10:00 AM / 5:00 PM]."

### Cut-off processing flow (Cloud Functions — automated)
At cut-off time, the Cloud Function runs for every `preOrders` document where:
- `status == "scheduled"`
- `scheduledDate == today` (formatted "YYYY-MM-DD")
- `mealSlot == "lunch"` (for the 10 AM function) or `"dinner"` (for the 5 PM function)

For each matched document:
```
IF user.walletBalance >= preOrder.totalAmount:
    → deduct via runTransaction()
    → set preOrder.status = "confirmed"
    → create walletTransactions doc (type=deduction)
    → send FCM: "Your lunch pre-order has been confirmed. See you at the cafeteria!"
ELSE:
    → set preOrder.status = "cancelled"
    → send FCM: "Your pre-order was cancelled — insufficient wallet balance. Please top up."
```

### Recurring pre-orders
- When a recurring pre-order is processed (confirmed OR cancelled), Cloud Function creates a new `preOrders` document for the next occurrence date (same day of week, next week) with status = "scheduled"
- This way the chain continues automatically

### Cancellation by student
- Student can cancel a pre-order only if status == "scheduled" (not yet processed)
- `FirestoreRepository.cancelPreOrder(preOrderId)` → sets status = "cancelled"
- No refund needed since no deduction has occurred yet

### Non-pick-up policy
- Once confirmed and deducted, the student is responsible for picking up the meal
- No refund for confirmed pre-orders not picked up (non-refundable by design)
- Staff marks pre-orders as Collected via `StaffOrdersFragment` (pre-orders appear there too once confirmed, alongside regular orders)
