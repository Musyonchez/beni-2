# 6.2 Frontend Implementation

The frontend of the USIU Cafeteria Ordering System comprises two distinct client applications. The primary deliverable is a native Android application built in Java with XML layouts, serving both students and cafeteria staff through role-specific navigation. The secondary deliverable is a web-based admin panel built with Next.js 14 and TypeScript, accessible via a desktop browser for system administrators. Both applications adhere to Material Design 3 principles and USIU branding guidelines, incorporating the university's signature navy (#002147) and gold (#CFB991) colour palette throughout all interface elements.

## i. Authentication and Role Routing

Authentication is handled centrally by Firebase Authentication using email and password credentials. The LoginActivity submits user credentials via Firebase Auth's signInWithEmailAndPassword method. On successful authentication, the app immediately reads the users/{uid} Firestore document to retrieve the role field. If the role is "student", the user is directed to MainActivity with the student bottom navigation configuration. If the role is "staff", the user is directed to StaffMainActivity with the staff navigation. This role check prevents students from accessing staff screens and vice versa without requiring server-side session management.

Students self-register through the RegisterActivity, which creates a Firebase Authentication user and simultaneously writes a Firestore document to the users collection with role: "student" and walletBalance: 0.0. Staff accounts cannot be self-registered; they are created exclusively by administrators via the web admin panel, which calls a Firebase Admin SDK API route to provision both the Auth user and the Firestore document. When a staff or admin user logs in for the first time, the firstLogin field on their Firestore document is true. The app detects this and redirects to ChangePasswordActivity before granting access to any operational screen, enforcing a mandatory password change on initial access.

> [Figure 23: LoginActivity — Firebase Authentication flow showing role-based routing to MainActivity (student), StaffMainActivity (staff), and ChangePasswordActivity (firstLogin check)]

Figure 23 illustrates the authentication flow. On login, the signInWithEmailAndPassword callback reads the user's Firestore document to determine role and firstLogin status. If firstLogin is true, the activity starts ChangePasswordActivity and finishes itself, preventing back-navigation to the pre-change state. Once the password is updated, firstLogin is set to false in Firestore, and the user is routed to the appropriate main activity.

## ii. Student Interface — Menu and Cart

The student interface is hosted within MainActivity, which manages five fragments through a BottomNavigationView. Navigation uses fragment hide/show transactions rather than replace transactions; this preserves the RecyclerView scroll position in the Menu and Orders tabs when the user switches between screens, eliminating the need to re-query Firestore on every tab switch.

The MenuFragment serves as the home screen for student users. It presents a horizontal ChipGroup at the top of the screen allowing the user to filter items by category: All, Breakfast, Lunch, and Dinner. Below the chips, a RecyclerView backed by a MenuAdapter displays item cards. Each card shows the item image (loaded asynchronously using Glide with a placeholder drawable), the item name, price in KES, and a short description. Items with available == false render with a "Sold Out" chip and a disabled, greyed-out "Add to Cart" button, ensuring that unavailable items are visible but not orderable. Tapping the "Add to Cart" button dispatches the item to CartViewModel and shows a snackbar confirming the addition. Category filtering is performed in-memory on the already-loaded LiveData list, requiring no additional network call.

> [Figure 24: MenuFragment — category chip filter (All/Breakfast/Lunch/Dinner), RecyclerView item cards with Glide image loading, and Add to Cart button with sold-out state]

Figure 24 shows the Menu screen. The RecyclerView data originates from FirestoreRepository.listenToMenuItems(), which attaches a Firestore real-time snapshot listener. Menu changes made by staff or admin are reflected on the student's screen within seconds without any manual refresh.

The CartFragment displays all items currently held in CartViewModel, which is a shared ViewModel that survives fragment switches. Each row in the cart RecyclerView shows the item name, unit price, and a quantity stepper with minus and plus buttons. Tapping minus on an item with quantity 1 removes it from the cart entirely. A summary section below the list shows the subtotal and an estimated wait time. The estimated wait is computed by querying the count of orders currently in "pending" or "preparing" status and multiplying by a constant of five minutes per order (minimum displayed is five minutes if the queue is empty). A RadioGroup beneath the summary lets the student toggle between wallet payment and cash payment. When wallet is selected, the current balance from WalletViewModel is displayed alongside the option. If the balance is insufficient, a warning label appears and the "Place Order" button remains disabled until the student switches to cash or the balance is topped up. Tapping "Place Order" triggers the appropriate Firestore transaction based on the selected payment method.

> [Figure 25: CartFragment — item quantity stepper, wallet/cash payment toggle with live balance, estimated wait time display, and Place Order button with insufficient-balance guard]

Figure 25 shows the Cart screen. The wallet balance on the payment toggle is sourced from WalletViewModel's real-time Firestore listener and updates automatically if a top-up occurs while the student is reviewing their cart. The insufficient-balance guard disables the Place Order button locally — the check runs against the LiveData balance before any Firestore write is attempted, so no server round-trip is needed to enforce it.

## iii. Student Interface — Orders, Pre-orders, and Wallet

The OrdersFragment presents a two-tab layout. The "Active" tab shows orders where the status is "pending", "preparing", or "ready", each displayed as a card with a status chip coloured amber (pending), blue (preparing), or green (ready). A real-time Firestore snapshot listener attached in FirestoreRepository.listenToStudentOrders() ensures that when cafeteria staff advance an order's status, the chip on the student's screen updates without requiring a manual refresh. When an order reaches "ready", the student also receives a Firebase Cloud Messaging push notification. The "History" tab displays orders with "collected" status in reverse chronological order.

The PreOrdersFragment shows the student's scheduled pre-orders as a scrollable list. A floating action button opens a bottom sheet dialog where the student selects a meal slot (Lunch or Dinner), a scheduled date, and items from the menu. A recurring toggle presents day-of-week checkboxes for weekly repetition. Validation ensures the selected date is not in the past and that, if today is chosen, the current time is before the cut-off for the selected slot (10:00 AM for Lunch, 5:00 PM for Dinner). Students may cancel a pre-order only while its status is "scheduled", before the cut-off processing has occurred. No wallet deduction takes place at scheduling time; deduction occurs at the cut-off via the Supabase Edge Function.

The ProfileWalletFragment displays the student's current wallet balance in a prominent card at the top of the screen, with an informational note indicating that top-ups are handled at the cafeteria counter. Below the balance card, a RecyclerView backed by a WalletTransactionAdapter lists all wallet transactions in reverse chronological order. Each row shows a directional icon (green arrow for credits, red arrow for debits), the transaction description (e.g., "Top-up by staff" or "Order #abc123"), the amount, and the timestamp. A logout button at the bottom calls FirebaseAuth.signOut() and starts LoginActivity with the back stack cleared.

> [Figure 26: ProfileWalletFragment — wallet balance card, transaction history list with credit/debit indicators, and logout button]

Figure 26 shows the Profile/Wallet screen. The balance card sources its value from the same WalletViewModel instance shared across the app, so it reflects any top-up or deduction that occurred during the session without requiring the student to navigate away and return. Each transaction row shows a directional icon, description, amount, and timestamp, giving the student a full audit trail of their wallet activity.

## iv. Staff Interface

The staff interface is hosted in a separate StaffMainActivity with its own BottomNavigationView presenting three tabs: Orders, Menu, and Wallet. This separation ensures that staff users never see student navigation items and vice versa, reducing cognitive load and preventing accidental interactions.

The StaffOrdersFragment is the primary operational screen for cafeteria staff. It displays all active orders across all students — those in "pending", "preparing", or "ready" status — in a single real-time RecyclerView. Each card shows the student name, a list of ordered items with quantities, the total amount, the payment method (wallet or cash), and the current status. An action button on each card advances the order to its next state: "Start Preparing" moves a pending order to preparing; "Mark Ready" moves a preparing order to ready and simultaneously triggers an HTTP POST to the notify-order-ready Supabase Edge Function, which sends a push notification to the student's device. "Mark Collected" finalises the order. For cash orders, the "Mark Collected" step reminds the staff member to collect physical payment before confirming.

> [Figure 27: StaffOrdersFragment — real-time order queue with student name, items, payment method, and status action buttons (Start Preparing / Mark Ready / Mark Collected)]

Figure 27 shows the Staff Orders screen. The real-time snapshot listener attached in listenToAllActiveOrders() fires immediately when a new order is placed, inserting the new card at the top of the queue without requiring the staff member to refresh. Each status action button triggers both a Firestore write and, at the "Mark Ready" step, an HTTP POST to the notify-order-ready Edge Function to dispatch the student's push notification.

The StaffMenuFragment provides a CRUD interface for menu items. Each item card includes an availability toggle (Switch) that staff can flip to immediately mark an item as unavailable without reloading the screen. A floating action button opens an "Add Item" dialog with fields for name, description, category, price, image URL, and availability. Tapping an existing item opens a pre-filled edit dialog. All changes are written to the menuItems Firestore collection and immediately reflected on student devices through their active snapshot listeners.

The StaffWalletFragment allows staff to manage student wallet balances. The screen presents a lookup form where the staff member enters a student ID. Tapping "Find Student" queries the users collection and displays the student's name and current balance. The staff member then enters an amount and selects either "Credit" (top-up) or "Debit" (deduction, for cases such as correcting an overpayment). Tapping "Confirm" calls the appropriate FirestoreRepository method — creditWallet() or deductWalletByStaff() — both of which use Firestore runTransaction() for atomicity and record the staff member's UID in the staffId field of the resulting walletTransactions document for audit purposes.

> [Figure 28: StaffWalletFragment — student ID lookup form, current balance display, credit/debit amount entry, and confirmation button]

Figure 28 shows the Staff Wallet screen. The student ID lookup queries the users collection before any balance operation is initiated, ensuring the staff member confirms the correct student name and current balance before committing a credit or debit. Both operations use Firestore runTransaction for atomicity and embed the staff member's UID in the resulting walletTransactions document, providing a full audit trail.

## v. Admin Web Panel

The web-based admin panel is a separate Next.js 14 application deployed independently of the Android app. It is accessible only via desktop browser and is role-gated: login is handled through the Firebase Client SDK, and upon authentication the panel reads the user's role field; any non-admin user is redirected to the login page. Admin accounts with firstLogin: true are redirected to a change-password page before accessing the dashboard.

The dashboard consists of four pages. The Menu Management page presents a full-width data table of all menu items with inline availability toggles, edit buttons, and delete buttons. The Staff Account Creation page provides a form for entering the new staff member's name, email, and temporary password; submission calls a Next.js API route (/api/create-staff) which uses the Firebase Admin SDK to create the Firebase Auth user and write the Firestore document with role: "staff" and firstLogin: true. The Users Directory page displays two separate tables — one for students and one for staff and admin accounts — both read from Firestore, filtered by role, providing the administrator with a complete view of all system users.

> [Figure 29: Next.js admin panel — Staff Account Creation form and Users Directory page showing student and staff tables]

Figure 29 shows the admin panel's staff account creation interface. The Admin SDK API route runs server-side, keeping the service account credentials off the client and ensuring that only the server can create Auth users with arbitrary roles.

