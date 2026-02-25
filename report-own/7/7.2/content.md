# 7.2 Unit Testing & System Testing

## Unit Testing

Unit testing for the USIU Cafeteria Ordering System focused on verifying the correctness of core business logic through code inspection and live app execution rather than automated JUnit or Espresso test suites. Three areas were assessed:

**CartViewModel logic (`CartViewModel.java`):** The ViewModel maintains a `MutableLiveData<List<CartItem>>`. The `addItem()` method checks whether an item already exists in the list by `menuItemId`; if it does, it increments the quantity rather than adding a duplicate. `removeItem()` deletes the entry entirely regardless of quantity. `updateQuantity()` replaces the quantity for an existing entry and removes the item if the new quantity reaches zero. `clearCart()` posts an empty list. These behaviours were verified by executing TC02 — adding two items, adjusting quantities, and removing one — and observing that the cart total recalculated correctly at each step.

**Wallet balance check (`CartFragment.java`):** Before invoking `FirestoreRepository.placeOrder()`, `CartFragment` reads the current `walletBalance` from `WalletViewModel` and compares it against the cart subtotal. If `balance < subtotal`, the order call is never made and a Snackbar error is displayed. This logic was verified through TC04, in which a wallet order was attempted with insufficient funds; the order was correctly blocked with no Firestore document created and the balance unchanged.

**Estimated wait time formula (`OrdersFragment.java`):** The wait time is calculated as `activeOrderCount x 5` minutes, with a minimum of 5 minutes when at least one active order exists. `activeOrderCount` represents orders with status `pending` or `preparing` belonging to the current student. The formula was confirmed via TC03 and TC06: the wait chip displayed "~5 min" for a single pending order and the chip was hidden after the order was marked Ready.

## System Testing

System testing evaluated the application as a complete, integrated product running against the live Firebase backend. Unlike unit testing, which targets individual logic blocks in isolation, system testing validated the end-to-end behaviour of all integrated components — authentication, Firestore reads and writes, Supabase Edge Function calls, FCM delivery, and the admin web panel — operating together under conditions that closely simulate real-world use.

The testing was structured into three subsections: functional testing (TC01–TC09), which verified the core user scenarios for all three roles; security testing (TC10–TC12), which validated Firestore security rule enforcement; and performance testing (TC13–TC15), which benchmarked response times for critical operations. Each test case was executed manually and the actual result was observed and recorded.
