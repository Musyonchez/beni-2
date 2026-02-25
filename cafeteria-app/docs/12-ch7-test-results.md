# Chapter 7 — Test Results

---

## 7.2 Unit Testing

Unit testing for this project focused on verifying core logic components through code inspection
and live app execution rather than automated JUnit or Espresso tests. Three key areas were
assessed:

**CartViewModel logic (`CartViewModel.java`)**
The ViewModel holds a `MutableLiveData<List<CartItem>>`. The `addItem()` method checks whether
the item already exists in the list (by `menuItemId`); if it does, it increments the quantity
rather than adding a duplicate. `removeItem()` deletes the entry entirely regardless of quantity.
`updateQuantity()` replaces the quantity for an existing item and removes the item if the new
quantity is zero. `clearCart()` posts an empty list. These behaviours were verified by running
TC02 — adding two items, adjusting quantity, and removing one — and confirming the cart total
updated correctly at each step.

**Wallet balance check (`CartFragment.java`)**
Before calling `FirestoreRepository.placeOrder()`, `CartFragment` reads the current
`walletBalance` from `WalletViewModel` and compares it against the cart subtotal. If
`balance < subtotal`, the order call is never made and a Snackbar error is shown. This was
verified by TC04 — placing a wallet order with insufficient funds — which correctly blocked the
order and displayed a warning without creating any Firestore document.

**Estimated wait time formula (`OrdersFragment.java`)**
The wait time is calculated as `activeOrderCount × 5` minutes, with a floor of 5 minutes when
at least one active order exists. `activeOrderCount` is the number of orders with status
`pending` or `preparing` that belong to the current student. The formula was confirmed via TC03
and TC06 — the wait chip showed "~5 min" for a single pending order and updated to "~0 min"
(chip hidden) after the order was marked Ready.

---

## 7.3.1 Functional Testing

### Table: Functional Test Cases

| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC01 | Student registration | Open app → Register → enter name, student ID (e.g. 671234), email, password → tap Register | Lands on Menu screen; user document created in Firestore with role "student" and walletBalance 0 | Registered successfully; Menu screen loaded with all items; Firestore document confirmed in console | **Passed** |
| TC02 | Cart management | Add "Beef Stew" and "Chapati" to cart → increase Chapati quantity to 2 → remove Beef Stew → verify total | Cart shows Chapati ×2 only; total = Chapati price × 2 | Cart updated correctly at each step; total recalculated instantly; removing item cleared it from list | **Passed** |
| TC03 | Wallet order — sufficient funds | Top up wallet to KES 500 via staff → add items totalling KES 150 → select Wallet → Place Order | Order doc created in Firestore with status "pending"; walletBalance decremented by 150; order appears in Orders tab | Order appeared in Orders tab with status chip "Pending" and estimated wait "~5 min"; wallet balance reduced correctly | **Passed** |
| TC04 | Wallet order — insufficient funds | Set wallet balance to KES 50 → add items totalling KES 200 → select Wallet → Place Order | App blocks order; shows insufficient funds warning; no Firestore doc created; balance unchanged | Snackbar appeared: "Insufficient wallet balance"; no order created; balance remained at KES 50 | **Passed** |
| TC05 | Cash order | Add items to cart → select Cash → Place Order | Order doc created with paymentMethod "cash" and status "pending"; no wallet deduction | Order appeared in Orders tab with status "Pending"; wallet balance unchanged; paymentMethod field confirmed as "cash" in Firestore | **Passed** |
| TC06 | Staff marks Preparing | Log in as staff → open StaffOrders → tap "Start Preparing" on student's order | Order status updates to "preparing" in Firestore; student's order chip turns blue | Staff tapped button; student Orders tab refreshed within ~1 second; status chip turned blue ("Preparing") | **Passed** |
| TC07 | Staff marks Ready → FCM | Staff taps "Mark Ready" on order | Order status → "ready"; student receives FCM push notification; chip turns green | Status updated; push notification arrived on student device in ~3 seconds; chip turned green ("Ready") | **Passed** |
| TC08 | Pre-order scheduling | Student opens Pre-orders tab → FAB → set meal type Lunch, future date/time → Save | Pre-order doc created in Firestore with status "scheduled"; appears in Pre-orders list | Pre-order card appeared immediately with correct meal type, time, and "Scheduled" badge | **Passed** |
| TC09 | Staff wallet top-up | Staff opens StaffWallet tab → enter student ID → enter amount KES 200 → tap Top Up | walletBalance incremented by 200; walletTransaction doc created with type "credit" and staffId | Student balance updated; transaction recorded with correct staffId; student's Profile/Wallet screen reflected new balance | **Passed** |

All nine functional test cases passed. The complete student order lifecycle — from registration
through menu browsing, cart management, order placement, real-time status tracking, and FCM
notification — executed without errors on a physical Android device. The pre-order scheduling
and wallet top-up flows likewise completed correctly, with Firestore documents reflecting the
expected state after each operation.

---

## 7.3.2 Security Testing

### Table: Security Test Cases

| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC10 | Student cannot self-modify wallet | In Firebase Console Rules Playground, simulate a write to `users/{studentUid}` with `walletBalance: 99999` using a student auth token | Write denied by Firestore rules | Rules Playground returned DENIED; student rule only permits writing to `orders` and `preOrders` collections, not the `users` document | **Passed** |
| TC11 | Unauthenticated read denied | In Rules Playground, simulate a GET on `orders` collection with no auth token | Read denied | Rules Playground returned DENIED; `isAuth()` check blocks all unauthenticated access across every collection | **Passed** |
| TC12 | Staff can read all orders | In Rules Playground, simulate a GET on `orders` collection using a staff auth token (role = "staff") | Read allowed | Rules Playground returned ALLOWED; `isPrivileged()` function (which includes `isStaff()` and `isAdmin()`) grants read access to all orders | **Passed** |

All three security test cases passed. The Firestore security rules correctly enforce role-based
access: unauthenticated requests are rejected at the outermost `isAuth()` guard, students are
limited to their own data partitions, and privileged roles (staff and admin) have full read/write
access to operational collections. The wallet balance field is write-protected against student
modification, ensuring wallet integrity depends entirely on server-side staff operations.

---

## 7.3.3 Performance Testing

### Table: Performance Test Cases

| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC13 | Menu load time | Cold launch app (process killed) → measure time from app open to Menu screen fully populated | Menu items visible within 3 seconds | Menu loaded with all 13 items in approximately **1.8 seconds** on a physical Android device over Wi-Fi | **Passed** |
| TC14 | Order status propagation (staff → student) | Staff marks order Ready on one device; observe student device | Status update visible on student screen within 2 seconds | Student's order chip updated in approximately **1 second**; Firestore real-time listener reflected the change near-instantly | **Passed** |
| TC15 | Order appears on staff screen | Student places order; observe staff StaffOrders screen | New order visible on staff screen within 2 seconds | Order appeared on staff screen in approximately **1–2 seconds**; Firestore snapshot listener triggered on the new document | **Passed** |

All three performance test cases passed and met their targets. The Firestore real-time listeners
provided sub-2-second propagation for order status changes in both directions. Cold launch menu
load time of ~1.8 seconds falls well within the 3-second target, benefiting from Firestore's
client-side caching after the first fetch. These results reflect typical performance on a
mid-range Android device connected to a Wi-Fi network.

---

## 7.4 User Acceptance Testing (UAT)

UAT was conducted in two stages: a developer-led role simulation and a peer review session with
two fellow students.

**Stage 1 — Developer role simulation**
The developer played the student role on one device and the staff role on a second device
simultaneously. The complete order lifecycle — registration, menu browsing, cart assembly, wallet
payment, real-time status tracking, and FCM notification — was executed end-to-end. The
pre-order scheduling flow was tested for a future lunch slot, and the wallet top-up and manual
deduction features were verified from the staff side. All operations completed without errors.
Observations: the bottom navigation between tabs was smooth; the status chip colour progression
(grey → blue → green) was intuitive; the wallet balance update was immediately visible in the
Profile tab after a top-up.

**Stage 2 — Peer review**
Two USIU-Africa students (playing the student role) and one peer (playing the staff role) used
the app for approximately 15 minutes each. Feedback gathered:

- *"Adding items and seeing the total change in the cart feels fast and natural."*
- *"I wasn't sure what 'Pre-order' meant at first — a short tooltip or label would help."*
- *"The notification when the order was ready was a nice touch — I'd actually use this."*
- *"The wallet balance being visible in the cart before paying is very useful."*

No functional failures were observed during peer testing. The pre-order section label was noted
as potentially confusing for first-time users; this is identified as a future improvement (see
7.5d).

---

## 7.5 Key Findings

### a) Functionality
All nine functional test cases passed. The core order lifecycle (browse → cart → pay → track →
notify) worked reliably on a physical device. The pre-order cut-off mechanism — executed by a
Supabase Edge Function triggered by cron-job.org — correctly deducted wallet balances and
dispatched FCM alerts at the scheduled cut-off times (10:00 AM for lunch, 5:00 PM for dinner).
Wallet operations (top-up, deduction, atomic order payment) maintained balance integrity across
all tested scenarios with no partial writes observed.

### b) Performance
All three performance targets were met. Menu load time (~1.8 s) and real-time order propagation
(~1 s) were within acceptable thresholds for a live ordering system. Firestore's snapshot
listeners provided a responsive experience without polling, and the client-side cache reduced
repeat menu load times to near-instant after the first fetch.

### c) Security
Firestore security rules correctly enforced all three role boundaries tested. Students cannot
modify their own wallet balance or read other students' orders. Unauthenticated access to all
collections is denied. Staff and admin roles receive the correct elevated permissions via the
`isPrivileged()` rule function. The admin web panel API routes use the Firebase Admin SDK
(server-side), which is separate from client-side rules and inherently trusted.

### d) Usability
The Material Design 3 component set and USIU branding (navy and gold) produced a consistent,
professional interface. Tab-based navigation was intuitive for both student and staff roles. The
main usability observation from UAT was that the "Pre-orders" label was not immediately
self-explanatory to first-time users. A brief onboarding tooltip or screen label would improve
discoverability. This is noted as a future enhancement rather than a defect.

---

## 7.6 Impact Analysis

### i. Students
Students can browse the cafeteria menu, place orders, and receive a push notification when their
meal is ready — eliminating the need to queue physically or wait at the counter. The wallet
feature removes the friction of carrying exact cash for every transaction. The pre-order feature
allows students to schedule meals around their lecture timetable, guaranteeing food availability
at a chosen time. These changes directly address the peak-hour queue problem identified in the
problem statement.

### ii. Cafeteria Staff
Staff receive a real-time view of all incoming orders on the StaffOrders screen, replacing the
current ad-hoc counter system. Order status updates (Preparing / Ready) are communicated
instantly to students, reducing verbal communication overhead at the counter. The wallet top-up
interface lets staff credit student accounts immediately upon receiving cash, with an audit trail
recording the staff member's ID on every transaction.

### iii. Administrators
The web-based admin panel (Next.js) gives administrators full control over the menu (add, edit,
delete items, toggle availability) and user management (create staff accounts, view all students
and staff) without requiring Android app access. Staff accounts are provisioned centrally with a
forced first-login password change, improving account security.

### iv. Platform Maintainability
The architecture separates concerns clearly: the Android app handles student and staff
interactions, the Next.js panel handles administration, and Supabase Edge Functions handle
server-side scheduled logic. All three tiers connect to the same Firebase Firestore backend.
This separation means each component can be updated independently. Firestore's schemaless
document model allows new fields to be added without migrations, as demonstrated by the addition
of `firstLogin` and `staffId` fields during development.

### v. SDGs

**SDG 4 — Quality Education:** By reducing time lost to cafeteria queuing during short breaks
between lectures, the system helps students spend more of their limited break time resting or
studying rather than waiting in line. Pre-ordering further supports students with tight
timetables.

**SDG 9 — Industry, Innovation and Infrastructure:** The system demonstrates that low-cost,
cloud-based mobile solutions (Firebase Spark free plan, Supabase free tier, Vercel free hosting)
can deliver production-grade university infrastructure without capital expenditure — a model
applicable across Kenyan higher education institutions.

**SDG 11 — Sustainable Cities and Communities:** Digitising food ordering reduces paper-based
processes (manual receipts, chalkboard menus) and optimises cafeteria resource use by giving
staff advance demand visibility through pre-orders, reducing over-preparation and food waste.

---

## 7.7 Conclusion

Testing confirmed that all functional, security, and performance requirements of the USIU
Cafeteria Ordering System were met. The complete student order lifecycle, the pre-order cut-off
automation, the in-app wallet, and the role-based access model all performed as specified. No
critical defects were identified during testing. Minor usability observations from UAT — notably
the pre-order label discoverability — have been noted as future enhancements. The system is
ready for deployment as a working prototype and provides a solid foundation for the future
improvements outlined in Chapter 8.
