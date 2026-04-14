# APPENDIX A: USER MANUAL FOR THE USIU CAFETERIA ORDERING SYSTEM

This appendix serves as a quick-start guide for the three user roles of the USIU Cafeteria Ordering System: Students, Cafeteria Staff, and System Administrators.

---

## A.1 For Students (Android App)

### Getting Started

**Registration:** Open the app and tap **Register**. Enter your name, student ID, USIU email address, and create a password. Your in-app wallet will be created automatically with a balance of KES 0.00.

**Login:** Enter your email and password. The system will read your role from Firestore and direct you automatically to the Student Dashboard.

### Placing a Regular Order

**Browse the Menu:** On the **Menu** tab, use the category chips at the top of the screen — All, Mains, Snacks, Drinks, Specials — to filter items. Items that are sold out display a red "Sold Out" chip and a disabled Add to Cart button. Tap **Add to Cart** on any available item. A snackbar will confirm the addition.

**Review the Cart:** Navigate to the **Cart** tab. Use the `+` and `−` stepper buttons to adjust item quantities. Removing the last unit of an item removes it from the cart entirely. The subtotal and an estimated wait time update in real time at the bottom of the list.

**Select a Payment Method:**
- **Wallet:** Select the Wallet option. Your current balance is shown alongside the toggle. If the balance is insufficient, a warning appears and the Place Order button is disabled. Top up your wallet at the cafeteria counter before proceeding.
- **Cash:** Select the Cash option. No balance check is performed. You will pay physically at the counter when you collect your order.

Tap **Place Order** to confirm. For wallet orders, the deduction and order creation happen atomically — if either fails, neither is committed.

**Track Your Order:** Go to the **Orders** tab. Your active order displays a three-step status bar that updates in real time without any manual refresh:
- *Pending* (grey chip) — order received, awaiting preparation.
- *Preparing* (blue chip) — cafeteria staff have started your meal.
- *Ready* (green chip) — your meal is ready for collection at the counter.

A push notification will also arrive on your device when the status changes to Ready, even if the app is in the background.

### Smart Pre-order (Scheduling a Meal in Advance)

Navigate to the **Pre-orders** tab and tap the **+** button (bottom-right corner).

In the dialog that appears:
1. Select the **Meal Type**: Lunch or Dinner.
2. Choose a **Date** and **Pickup Time** for the scheduled meal.
3. Select your items and quantities from the menu list.
4. Optionally, toggle **Recurring** and select the day(s) of the week to repeat the pre-order automatically each week.

Tap **Save**. The pre-order will appear in your list with a "Scheduled" badge.

> **Important:** Pre-orders are funded entirely from your wallet. No deduction occurs at scheduling time. The deduction is processed automatically at the cut-off time — 10:00 AM EAT for Lunch slots, 5:00 PM EAT for Dinner slots. Ensure your wallet has sufficient funds before the cut-off. If the balance is insufficient at cut-off time, the pre-order will be cancelled and you will receive a push notification. You may then top up and reschedule.

To cancel a pre-order, tap the **Cancel** button on its card. Cancellation is only possible while the status is "Scheduled" — once the cut-off has processed and the status is "Confirmed", the order cannot be cancelled.

### Managing Your Wallet

Go to the **Profile/Wallet** tab.

- **Balance:** Your current wallet balance is displayed in a prominent card at the top of the screen.
- **Top-up:** Wallet top-ups are handled at the cafeteria counter only. Bring cash and provide your Student ID to the staff member, who will credit your account. The updated balance will appear on your Profile screen within seconds.
- **Transaction History:** Scroll down below the balance card to see a full list of credits (top-ups) and debits (order payments and pre-order deductions), each with a date, description, and amount.
- **Logout:** Tap the Logout button at the bottom of the screen to sign out and return to the Login screen.

---

## A.2 For Cafeteria Staff (Android App)

### Login and First-Time Setup

Your account is created by the System Administrator. You will receive a temporary password via email or in person. Open the app, enter your email and temporary password, and tap **Login**. On your first login, you will be redirected automatically to a **Change Password** screen. You must set a new password before any other screen is accessible. This step is mandatory and cannot be skipped.

### Processing Orders (StaffOrders Tab)

The **StaffOrders** tab is your primary operational screen. It displays all active orders from all students — those in Pending, Preparing, or Ready status — in a single real-time list. Each card shows the student's name, the ordered items and quantities, the total amount, the payment method (wallet or cash), and the current status.

Advance each order through the workflow using the action button on its card:

| Action | Result |
|---|---|
| Tap **Start Preparing** | Status changes from Pending to Preparing. Student's in-app chip turns blue. |
| Tap **Mark Ready** | Status changes from Preparing to Ready. A push notification is sent automatically to the student's device. Student's chip turns green. |
| Tap **Mark Collected** | Order is finalised. For cash orders, collect physical payment from the student before tapping this button. |

Orders disappear from the active list once marked Collected.

### Managing the Menu (StaffMenu Tab)

- **Toggle Availability:** Flip the switch on any item card to instantly mark it as "Sold Out" or "Available". The change is reflected on all connected student devices within seconds.
- **Add Item:** Tap the **+** FAB. Fill in the Name, Description, Category, Price, and Image URL. Tap Save.
- **Edit or Delete:** Tap an existing item card to open the edit dialog. Modify any field and save, or tap Delete to remove the item permanently.

### Managing Student Wallets (StaffWallet Tab)

This screen is used to credit or debit a student's in-app wallet balance in exchange for cash transactions at the counter.

1. Enter the student's **ID number** in the lookup field and tap **Find Student**. The student's name and current balance are displayed for confirmation.
2. Enter the **amount** of the transaction.
3. Tap **Credit** to add funds (top-up after receiving cash), or **Debit** to subtract funds (for corrections such as refunding an overcharge).
4. Tap **Confirm**. The transaction is recorded atomically and the student's balance updates immediately. Your staff ID is recorded on every transaction as part of the audit trail.

---

## A.3 For Administrators (Web Panel)

### Access

The admin panel is browser-based. Open a desktop browser and navigate to the deployed Vercel URL for your institution's admin panel. Log in with your administrator credentials. If this is your first login, you will be redirected to a password change page before accessing the dashboard.

> **Note:** The admin panel is role-gated. Any user who does not hold the `admin` role will be redirected to the login page immediately after authentication.

### Menu Management

The **Menu Management** page displays a full data table of all menu items. Each row shows the item name, category, price, current availability status, and image URL.

- **Add Item:** Click the **Add Item** button above the table. Fill in the Name, Description, Category (dropdown), Price, Image URL, and initial availability. Click Save. The item is immediately visible on all student Menu screens.
- **Edit:** Click the Edit (pencil) icon on any row. Modify the fields and save.
- **Delete:** Click the Delete (trash) icon on any row. This action is permanent.
- **Toggle Availability:** Flip the availability switch in any row to show or hide an item on the student menu without deleting it.

### Staff Account Creation

Navigate to the **Staff Accounts** page.

Fill in the new staff member's **Full Name**, **Email**, and a **Temporary Password**. Click **Create Staff Account**. The system calls a server-side API route that:
1. Creates a Firebase Authentication account with the provided credentials.
2. Writes a Firestore user document with `role: "staff"` and `firstLogin: true`.

On their first login, the staff member will be forced to change the temporary password before accessing any operational screen. A success message on this page confirms the new account's email address.

### Users Directory

The **Users Directory** page displays two separate tables:

- **Students:** Name, Student ID, Email, Wallet Balance, Date Joined.
- **Staff and Admins:** Name, Email, Role, First-Login Status (indicates whether the staff member has completed their first-login password change).

Both tables are read in real time from Firestore and reflect the current state of all registered accounts.
