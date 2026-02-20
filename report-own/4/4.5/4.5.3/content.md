# 4.5.3 Server Requirements

The USIU Cafeteria Ordering System uses Firebase as its entire server infrastructure, eliminating the need for a self-managed web or database server.

- **Database:** Firebase Firestore — fully managed NoSQL cloud database with real-time sync and offline SDKs. No server configuration required.
- **Authentication:** Firebase Authentication — managed identity service supporting email/password sign-in.
- **Push Notifications:** Firebase Cloud Messaging — managed notification delivery service.
- **Scheduled Jobs:** Firebase Cloud Functions — serverless Node.js runtime for the lunchCutoff and dinnerCutoff cron jobs and the onOrderStatusChanged trigger.
- **Firebase Plan:** Blaze (pay-as-you-go) plan required to deploy Cloud Functions. At prototype scale (hundreds of students), usage will remain within the free tier allowances included in the Blaze plan.
- **Scalability:** Firestore and Cloud Functions scale automatically with demand — no manual provisioning required.
