# 5.1.9 Wireframes

The wireframes for the USIU Cafeteria Ordering System serve as the visual blueprint for all application interfaces. They outline how each screen will be structured, what components will appear, and how users will navigate between functions. At this stage the focus is on layout and interaction flow rather than final colours or visual polish. The wireframes cover two applications: the Android mobile app (five student screens and three staff screens) and the web-based admin panel (four web pages). Together they ensure that every feature — menu browsing, cart checkout, order tracking, pre-order scheduling, wallet management, staff order processing, and admin account and menu control — is logically organised before implementation begins.

> [Figure 22: Wireframes — Android app (Menu, Cart, Orders, Pre-orders, Profile/Wallet) and web admin panel (Login, Menu Management, Staff Creation, Users Directory)]

Figure 22 above represents the wireframes for the USIU Cafeteria Ordering System.

## Screen 1: Menu

The Menu screen is the home screen for student users. A horizontal tab bar at the top allows filtering by category: All, Mains, Snacks, Drinks, and Specials. Below the tabs, a vertical scrollable list of item cards fills the main content area. Each card displays the item image, name, price, and an availability chip (green "Available" or red "Sold Out"). A floating action button (FAB) in the bottom-right corner shows the current cart item count and navigates to the Cart screen. A search icon in the top app bar expands an inline search field for text-based item lookup.

## Screen 2: Cart

The Cart screen displays all items currently in the cart in a scrollable list. Each row shows the item name, unit price, and +/– quantity controls, with a remove button. A subtotal updates dynamically below the list. A payment method toggle allows the student to switch between Wallet (showing current balance) and Cash. If Wallet is selected and the balance is insufficient, a warning indicator appears below the toggle. A prominent "Place Order" button at the bottom is disabled if the cart is empty or if Wallet is selected with insufficient funds.

## Screen 3: Orders

The Orders screen has two sections. The top section shows the active order (if one exists) with a three-step horizontal status bar: Pending → Preparing → Ready. The current step is highlighted. An estimated wait time chip (e.g., "~10 min") appears below the status bar, updating in real time as the queue changes. The bottom section is a scrollable history list of past completed orders, each showing date, items summary, total, and payment method. Tapping a past order expands it to show full details and a "Reorder" button.

## Screen 4: Pre-orders

The Pre-orders screen shows a list of upcoming scheduled pre-orders, each displayed as a card with meal type, pickup time, total, recurring badge (if set), and a cancel button. A FAB opens the Schedule Pre-order dialog. The dialog contains: a meal type selector (Breakfast / Lunch / Dinner), a date and time picker for the pickup slot, an item selection interface (same as the menu), a recurring toggle with a day-of-week selector, and a wallet balance preview showing post-deduction balance at cut-off.

## Screen 5: Profile / Wallet

The Profile/Wallet screen opens with a prominent wallet balance card at the top showing the current balance and a note indicating that top-ups are done at the cafeteria counter. Below the balance card is a scrollable list of recent wallet transactions (credits and debits) with date, description, and amount. Below transactions is the student's account details section (name, student ID, email) and a logout button.

---

## Web Admin Panel (Sixth Interface)

The admin panel is a browser-based application accessible only to users with the admin role. It consists of four pages:

**Admin Login Page:** A centred login form with email and password fields and a "Sign In" button. On successful login, the system checks the role field — if not admin, the user is redirected. If firstLogin is true, the user is redirected to the change password page before accessing the dashboard.

**Dashboard — Menu Management:** A full-width data table listing all menu items with columns for name, category, price, availability toggle (switch), and image URL. An "Add Item" button above the table opens an inline form or modal with fields for name, description, category (dropdown), price, imageUrl, and availability. Each row has Edit and Delete action buttons. Changes write to the Firestore menuItems collection via the Firebase Client SDK.

**Dashboard — Staff Account Creation:** A simple form with fields for full name, email, and temporary password, and a "Create Staff Account" button. On submission, the Next.js API route calls the Firebase Admin SDK to create a Firebase Auth user and write a Firestore users document with role=staff and firstLogin=true. A success message shows the new account's email.

**Dashboard — Users Directory:** Two separate tables on one page. The first table lists all students (name, student ID, email, wallet balance, joined date). The second table lists all staff and admin accounts (name, email, role, firstLogin status). Both tables are read from the Firestore users collection, filtered by role, via the Firebase Client SDK.
