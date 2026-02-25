# 7.4 Key Findings

The evaluation of the USIU Cafeteria Ordering System confirmed that the application successfully meets its core functional, security, performance, and usability requirements. The testing process revealed both the strengths of the chosen architecture and a small number of areas identified for future improvement.

## a) Functionality

All nine functional test cases passed. The core order lifecycle — menu browsing, cart management, wallet and cash payment, real-time status tracking, and FCM push notification — executed reliably end-to-end on a physical Android device. The pre-order cut-off automation, executed by the Supabase Edge Function triggered by cron-job.org at 10:00 AM for lunch and 5:00 PM for dinner, correctly deducted wallet balances and dispatched FCM alerts at the scheduled times. Wallet operations — top-up, deduction, and atomic order payment via Firestore `runTransaction` — maintained balance integrity across all tested scenarios with no partial writes or inconsistencies observed.

## b) Performance

All three performance targets were met within the 3-second threshold. Menu cold-launch load time was approximately 1.8 seconds. Real-time order status propagation between staff and student devices consistently completed in approximately 1 second, enabled by Firestore snapshot listeners that react to document changes without polling. Client-side Firestore caching further reduced repeat menu load times to near-instant after the first fetch. These results confirm that the system delivers the responsiveness expected of a live ordering environment.

## c) Security

All three Firestore security rule tests passed. The role-based access model correctly blocked unauthenticated reads, prevented students from self-modifying the `walletBalance` field, and permitted staff to read all orders across all students. The `isPrivileged()` helper function cleanly combined the staff and admin roles, while the `isAuth()` outermost guard rejected all unauthenticated access before any collection-specific rule was evaluated. The admin web panel's API routes use the Firebase Admin SDK server-side, which operates outside client-facing rules and is inherently trusted.

## d) Usability

The Material Design 3 component set and USIU branding (navy #002147 and gold #CFB991) produced a consistent and professional interface. Tab-based navigation was intuitive for both student and staff roles, and the cart quantity stepper, status chip colours, and real-time balance display were appreciated by UAT participants. The main usability finding from the peer review was that the "Pre-orders" section label was not immediately self-explanatory to first-time users. A short onboarding tooltip or contextual label would improve discoverability. This is noted as a minor future enhancement and does not constitute a functional defect.
