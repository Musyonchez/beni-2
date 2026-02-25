# 7.5 Impact Analysis

The implementation of the USIU Cafeteria Ordering System has delivered measurable improvements in ordering efficiency, communication between students and staff, and cafeteria resource management. Its impact spans all three user roles and demonstrates the practical value of cloud-based mobile technology in a university setting.

## i. Students

Students can browse the cafeteria menu, add items to a cart, and place orders from anywhere on campus without physically queuing at the counter. The in-app wallet removes the friction of carrying exact cash for every transaction. The real-time status chips and FCM push notification inform students exactly when their meal is ready, eliminating the need to wait at the counter or repeatedly check back. The pre-order feature allows students with tight lecture timetables to schedule meals in advance and collect them at a guaranteed time, directly addressing the peak-hour queue problem identified in the problem statement.

## ii. Cafeteria Staff

Cafeteria staff receive a real-time, structured view of all incoming orders on the StaffOrders screen, replacing the current ad-hoc verbal and handwritten counter system. The "Start Preparing" and "Mark Ready" actions communicate order status to students automatically, reducing verbal overhead at the counter. The StaffWallet interface enables staff to credit student accounts immediately upon receiving cash, with an audit trail that records the staff member's ID on every transaction — improving financial accountability without requiring a separate cashiering system.

## iii. Administrators

The Next.js admin web panel gives administrators full menu and user management control without requiring Android device access. Administrators can add, edit, and toggle the availability of menu items in real time, and can create new staff accounts with a forced first-login password change for security. The centralised dashboard provides a directory of all registered students and staff, supporting oversight without direct database access.

## iv. Platform Maintainability

The architecture separates concerns clearly across three tiers: the Android app handles student and staff interactions, the Next.js panel handles administration, and Supabase Edge Functions handle scheduled server-side logic. All three tiers connect to the same Firebase Firestore backend. This separation allows each component to be updated independently. Firestore's schemaless document model allows new fields to be added without schema migrations, as demonstrated during development by the addition of `firstLogin`, `staffId`, and `fcmToken` fields. The free-tier infrastructure — Firebase Spark, Supabase free tier, and Vercel — ensures zero operational cost for the prototype phase.

## v. SDGs

**SDG 4 — Quality Education:** By reducing time lost to cafeteria queuing during short breaks between lectures, the system helps students spend more of their limited break time resting or preparing for class. Pre-ordering further supports students with tight timetables who would otherwise skip meals to avoid missing the start of a lecture.

**SDG 9 — Industry, Innovation and Infrastructure:** The system demonstrates that low-cost, cloud-based mobile solutions — built entirely on free tiers (Firebase Spark, Supabase, Vercel, cron-job.org) — can deliver production-grade university infrastructure without capital expenditure. This model is directly applicable to other Kenyan higher education institutions seeking to digitise manual campus processes.

**SDG 11 — Sustainable Cities and Communities:** Digitising the food ordering process reduces paper-based operations such as manual receipts and chalkboard menus. The pre-order visibility gives cafeteria staff advance demand data, enabling more accurate food preparation and reducing over-production and food waste — contributing to a more resource-efficient campus community.
