# Chapter 7 — Figure Placements

Each figure block contains:
- **PARAGRAPH BEFORE / AFTER** — exact surrounding text so you know where to insert the image
- **Caption** — the short label that appears *below* the image in the report
- **Image spec** — what the screenshot should actually show (not in the report, just for taking the shot)

Figures start at **Fig 48** (continuing from Fig 47, the last figure in Ch6).

---

## File: `7/7.2/content.md`

---

### Fig 48

**PARAGRAPH BEFORE:**
> These behaviours were verified by executing TC02 — adding two items, adjusting quantities, and removing one — and observing that the cart total recalculated correctly at each step.

**Caption:** Figure 48: Cart tab after TC02 — item quantities adjusted using the stepper controls with the running subtotal updating correctly in real time.

**Image spec:** Open the Cart tab with two items added (e.g. Beef Stew and Chapati). Increment Chapati to 2, then remove Beef Stew entirely. Screenshot the cart showing only Chapati ×2, the +/− stepper buttons visible on the row, and the correct total at the bottom. Place Order button should be visible.

**PARAGRAPH AFTER:**
> **Wallet balance check (`CartFragment.java`):** Before invoking `FirestoreRepository.placeOrder()`, `CartFragment` reads the current `walletBalance` from `WalletViewModel` and compares it against the cart subtotal.

---

### Fig 49

**PARAGRAPH BEFORE:**
> This logic was verified through TC04, in which a wallet order was attempted with insufficient funds; the order was correctly blocked with no Firestore document created and the balance unchanged.

**Caption:** Figure 49: Cart screen during TC04 — insufficient wallet balance warning preventing order placement when the wallet balance is below the cart total.

**Image spec:** Set wallet balance to KES 50 via StaffWallet. Add items totalling KES 200 to the cart, select the Wallet payment option, then tap Place Order. Screenshot the moment the Snackbar appears at the bottom of the screen reading "Insufficient wallet balance". The Place Order button should appear disabled or greyed out.

**PARAGRAPH AFTER:**
> **Estimated wait time formula (`OrdersFragment.java`):** The wait time is calculated as `activeOrderCount x 5` minutes, with a minimum of 5 minutes when at least one active order exists.

---

## File: `7/7.2/7.2.1/content.md`

Figures 50–54 are inserted as a group in the slot **after the last row of Table 7.1** and **before the summary paragraph**. Insert them in order.

**PARAGRAPH BEFORE THE GROUP** *(last row of Table 7.1 — TC09):*
> | TC09 | Staff wallet top-up | Staff opens StaffWallet tab, enters student ID, enters amount KES 200, taps Top Up | walletBalance incremented by 200; walletTransaction document created with type "credit" and staffId | Student balance updated; transaction recorded with correct staffId; student Profile/Wallet screen reflected new balance | **Passed** |

---

### Fig 50

**Caption:** Figure 50: TC01 — Student registration screen and Menu tab after successful account creation, confirming automatic Firestore user document creation and wallet initialisation.

**Image spec:** Show two screenshots side by side (or a composite): (left) the Registration screen with the name, student ID, email, and password fields filled in just before tapping Register; (right) the Menu tab fully loaded with all menu items visible immediately after registration completes.

---

### Fig 51

**Caption:** Figure 51: TC06 and TC07 — Order status chip progression on the student Orders tab from Pending (grey) to Preparing (blue) to Ready (green), driven by staff actions on the StaffOrders screen.

**Image spec:** Capture the student Orders tab at three different moments as a composite or sequence: (1) grey "Pending" chip right after the order is placed; (2) blue "Preparing" chip ~1 second after the staff device taps Start Preparing; (3) green "Ready" chip ~1 second after the staff device taps Mark Ready. Can be three cropped screenshots of just the order card arranged horizontally.

---

### Fig 52

**Caption:** Figure 52: TC07 — FCM push notification received on the student device confirming the order is ready for collection, delivered approximately 3 seconds after the staff marked the order Ready.

**Image spec:** Pull down the notification shade on the student device immediately after the staff taps Mark Ready. Screenshot the notification banner or the notification in the shade showing the order-ready message. The app should be in the background (home screen visible) to show the notification arriving passively.

---

### Fig 53

**Caption:** Figure 53: TC08 — Pre-orders tab after scheduling a new lunch pre-order, showing the order card with meal type, scheduled time, and Scheduled status badge.

**Image spec:** Complete the pre-order scheduling flow (meal type: Lunch, future date and time, at least one item selected). Screenshot the Pre-orders tab immediately after saving. The card should show the meal type, date/time, item name(s), and the yellow "Scheduled" badge. The FAB (+) should be visible in the bottom-right corner.

---

### Fig 54

**Caption:** Figure 54: TC09 — StaffWallet tab after crediting a student's wallet with KES 200, with the updated balance confirmed on both the staff and student screens.

**Image spec:** On the staff device, complete a KES 200 top-up for a student via the StaffWallet tab. Screenshot the staff screen showing the confirmation (student name, amount credited, new balance). If possible, also capture the student's Profile/Wallet tab showing the same updated balance as a small inset — this demonstrates the real-time listener working.

---

**PARAGRAPH AFTER THE GROUP:**
> All nine functional test cases passed. The complete student order lifecycle — from registration through menu browsing, cart management, order placement, real-time status tracking, and FCM notification — executed without errors on a physical Android device.

---

## File: `7/7.2/7.2.2/content.md`

Figures 55–56 go in the slot **after the last row of Table 7.2** and **before the summary paragraph**.

**PARAGRAPH BEFORE THE GROUP** *(last row of Table 7.2 — TC12):*
> | TC12 | Staff can read all orders | In Rules Playground, simulate a GET on the orders collection using a staff auth token (role = "staff") | Read allowed | Rules Playground returned ALLOWED; the `isPrivileged()` rule function (which includes `isStaff()` and `isAdmin()`) grants read access to all orders | **Passed** |

---

### Fig 55

**Caption:** Figure 55: TC10 and TC11 — Firebase Console Rules Playground returning DENIED for a student attempting to write their own wallet balance (TC10) and for an unauthenticated read of the orders collection (TC11).

**Image spec:** In the Firebase Console Rules Playground, run two simulated requests: (1) a write to users/{studentUid} with walletBalance: 99999 using a student auth token — screenshot the DENIED result; (2) a GET on the orders collection with no auth token — screenshot the DENIED result. Combine both screenshots vertically or side by side with labels "TC10" and "TC11".

---

### Fig 56

**Caption:** Figure 56: TC12 — Firebase Console Rules Playground returning ALLOWED for a staff-authenticated read of the orders collection, confirming the isPrivileged() rule function grants correct elevated access.

**Image spec:** In the Firebase Console Rules Playground, run a GET on the orders collection with a staff user auth token (role = "staff"). Screenshot the green ALLOWED result panel. The auth token and collection path should be visible in the request pane.

---

**PARAGRAPH AFTER THE GROUP:**
> All three security test cases passed. The Firestore security rules correctly enforce all role boundaries tested. Unauthenticated requests are rejected at the outermost `isAuth()` guard before any collection-level rule is evaluated.

---

## File: `7/7.2/7.2.3/content.md`

### Fig 57

**PARAGRAPH BEFORE** *(last row of Table 7.3 — TC15):*
> | TC15 | Order appears on staff screen | Student places order; observe StaffOrders screen on staff device | New order visible on staff screen within 3 seconds | Order appeared on staff screen in approximately 1–2 seconds; Firestore snapshot listener triggered on the new document | **Passed** |

**Caption:** Figure 57: TC13 — Menu screen after cold launch, fully populated in approximately 1.8 seconds, well within the 3-second target threshold.

**Image spec:** Force-stop the app (recent apps → swipe away, or Settings → Force Stop). Cold-launch the app and screenshot the Menu tab the moment all items are fully loaded. Add a text annotation "~1.8 s" to the screenshot, or show the Android developer timing overlay if available. All category chips and item cards should be visible.

**PARAGRAPH AFTER:**
> All three performance test cases passed and met their targets comfortably. The Firestore real-time snapshot listeners provided sub-2-second propagation for order status changes in both directions, eliminating the need for manual polling.

---

## File: `7/7.3/7.3.1/content.md`

### Fig 58

**PARAGRAPH BEFORE** *(last bullet in 7.3.1):*
> - The pre-order card appeared in the Pre-orders tab immediately after saving, with the correct meal type, time, and "Scheduled" status badge.

**Caption:** Figure 58: Developer two-device role simulation — student device (left) displaying the Orders tab with a live order status chip, and staff device (right) displaying the StaffOrders tab with the matching order card and action buttons, both connected to the same live Firestore backend.

**Image spec:** Place both physical Android devices next to each other on a flat surface. On the student device, open the Orders tab with an active pending order. On the staff device, open the StaffOrders tab showing the same order card with the Start Preparing button visible. Take a single photo or screenshot showing both screens simultaneously. Landscape orientation works well for this.

**PARAGRAPH AFTER:**
> *(Section 7.3.2 — Peer Review Session begins)*

---

## File: `7/7.3/7.3.2/content.md`

### Fig 59

**PARAGRAPH BEFORE** *(final paragraph in 7.3.2):*
> The main usability observation from this stage was that the "Pre-orders" section label was not immediately self-explanatory to first-time users unfamiliar with the concept of advance scheduling. This is noted as a future enhancement — a short onboarding label or tooltip on first launch would improve discoverability — and is recorded as a minor UI refinement rather than a functional defect. Overall, the application was received positively, with participants expressing that the ordering flow was intuitive and that the real-time notification feature added significant value.

**Caption:** Figure 59: UAT peer review session — a USIU-Africa peer participant navigating the student-role Android app independently, without developer guidance, during the structured 15-minute review session.

**Image spec:** During the peer UAT session, photograph one of the student participants holding or using the Android device while on the Menu or Cart screen. The app UI should be clearly visible. The setting should look natural (desk, cafeteria, or similar). No need for the participant's face to be in frame if they prefer not — a shot of hands holding the phone with the app open is sufficient.

**PARAGRAPH AFTER:**
> *(Section 7.4 begins)*
