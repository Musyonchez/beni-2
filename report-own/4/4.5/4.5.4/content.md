# 4.5.4 Budget Requirements

The budget for the USIU Cafeteria Ordering System is minimal. All primary development tools are free, and the entire cloud infrastructure operates on free tiers with no credit card or billing account required.

- **Firebase Spark Plan:** Free — used for Firestore, Authentication, and Cloud Messaging. No Cloud Functions needed, so no Blaze plan upgrade is required.
- **Supabase Free Plan:** Free — hosts the process-cutoff and notify-order-ready Edge Functions with no usage charges at prototype scale.
- **cron-job.org:** Free — provides two scheduled HTTP cron jobs for the pre-order cut-off triggers.
- **Vercel Free Plan:** Free — hosts and continuously deploys the Next.js admin panel from GitHub.
- **Android Studio:** Free (no license cost).
- **VS Code:** Free (no license cost).
- **GitHub:** Free for public and private repositories at student scale.
- **Design Tools:** Figma free tier sufficient for wireframes and diagrams.
- **Physical Android Device:** Required for FCM and performance testing — an existing personal device is sufficient; no additional purchase needed.

Total estimated cost: **KES 0** for a fully functional prototype comprising the Android mobile application, the Next.js web admin panel, and all supporting cloud infrastructure.
