# 4.5.3 Server Requirements

The USIU Cafeteria Ordering System uses a combination of managed cloud services for its server infrastructure, eliminating the need for a self-managed web or database server. All services operate on free tiers — no credit card or billing account is required.

- **Database:** Firebase Firestore — fully managed NoSQL cloud database with real-time sync and offline SDKs. No server configuration required.
- **Authentication:** Firebase Authentication — managed identity service supporting email/password sign-in for three roles: student, staff, and admin.
- **Push Notifications:** Firebase Cloud Messaging — managed notification delivery service for order-ready and pre-order confirmation/cancellation alerts.
- **Firebase Plan:** Spark (free) plan. Cloud Functions are not used; all server-side scheduling logic runs on Supabase Edge Functions instead, which are available on the Supabase free tier without a billing account.
- **Edge Functions:** Supabase — free plan provides serverless TypeScript/Deno edge functions. Two functions are deployed: process-cutoff (pre-order cut-off deduction and cancellation logic, called by cron-job.org) and notify-order-ready (FCM notification dispatch, called directly by the Android app after a staff status update).
- **Scheduled Jobs:** cron-job.org — free HTTP cron service. Two jobs are configured to call the process-cutoff edge function: 10:00 AM EAT (lunch cut-off) and 5:00 PM EAT (dinner cut-off).
- **Admin Panel Hosting:** Vercel — free hosting for the Next.js admin web panel, with automatic deployment on every GitHub push to the main branch.
- **Scalability:** Firestore scales automatically with demand. Supabase Edge Functions and Vercel hosting scale within their respective free tier limits, which comfortably cover prototype-scale usage at USIU-Africa.
