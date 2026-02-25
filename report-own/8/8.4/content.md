# 8.4 Future Work

While the current version of the USIU Cafeteria Ordering System meets its core objectives and is deployed as a functional prototype, several enhancements are identified for future iterations that would improve usability, extend functionality, and prepare the system for full production scale.

**Online payment integration.** The current wallet system requires students to deposit cash at the counter to credit their in-app balance. Integrating a mobile money gateway such as M-Pesa STK Push would allow students to top up their wallets remotely from anywhere, removing the last physical dependency on the counter for wallet management. This would significantly improve convenience for students and reduce the cash-handling workload for staff.

**Pre-order discoverability and onboarding.** UAT feedback identified that the "Pre-orders" section label was not immediately self-explanatory to first-time users. A future enhancement would add a brief onboarding screen or contextual tooltip on first launch, explaining how pre-ordering works and when cut-off times apply. This change requires minimal engineering effort but would meaningfully reduce the learning curve for new users.

**Push notification preferences.** Currently, all FCM notifications (order ready, pre-order confirmation, pre-order cancellation) are sent without user configuration. A future notification preferences screen in the Profile/Wallet tab would allow students to opt out of specific notification types or set quiet hours, improving the experience for students who prefer not to receive notifications during lectures.

**Order history analytics for staff and admin.** The current system stores all orders in Firestore but provides no aggregate reporting. A future admin dashboard module — built into the existing Next.js admin panel — could display daily order volumes, peak hours, most popular menu items, and wallet transaction summaries. This data would help cafeteria management plan staffing levels and stock quantities more accurately, directly improving operational efficiency.

**Repeat order and favourites shortcut.** A common friction point for regular users is rebuilding the same cart from scratch each day. A future "Reorder" button on a completed order in the Orders history would populate the cart with the same items in one tap, significantly reducing the time required to place routine orders.

**Cash payment confirmation by staff.** Currently, cash orders appear on the StaffOrders screen identically to wallet orders, with no mechanism for staff to confirm that cash was physically received before preparing the order. A future enhancement would add a "Confirm Payment" step for cash orders, preventing kitchen preparation from beginning before payment is collected at the counter.

**Scalability to multiple cafeteria outlets.** The current data model assumes a single cafeteria. Introducing a `outletId` field on `menuItems` and `orders` documents would allow the system to scale to multiple on-campus food outlets — such as a coffee kiosk or a snack bar — each with their own staff interface and menu, all under the same Firebase project and admin panel.

**Full automated test suite.** The current testing approach relies on manual execution of test cases. A future iteration would introduce a JUnit and Mockito unit test suite for `CartViewModel`, `WalletViewModel`, and `FirestoreRepository`, along with Espresso UI tests for the critical order placement and wallet deduction flows. Automated testing would enable continuous integration checks on every code change and reduce regression risk as the feature set grows.
