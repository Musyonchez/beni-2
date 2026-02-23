# 4.5.2 Software Requirements

The following software tools and platforms will be used in the development of the USIU Cafeteria Ordering System:

- **IDE:** Android Studio (latest stable release) — primary development environment for Java + XML Android development; VS Code for Next.js admin panel and Supabase Edge Function development
- **Languages:** Java (Android application); TypeScript (Next.js admin panel and Supabase Edge Functions)
- **UI Frameworks:** Material Design 3 components via AndroidX libraries (Android app); Tailwind CSS (admin panel)
- **Web Framework:** Next.js 14 (App Router) — web-based admin panel
- **Version Control:** Git & GitHub for source code management
- **Firebase Services:** Firestore (real-time NoSQL database), Firebase Authentication (user management for three roles: student, staff, admin), Firebase Cloud Messaging (push notifications); Firebase Spark free plan — no Cloud Functions required
- **Edge Functions:** Supabase CLI — deploys TypeScript/Deno edge functions (process-cutoff, notify-order-ready) on the Supabase free plan
- **Scheduled Jobs:** cron-job.org — free HTTP cron service that triggers the Supabase Edge Functions at 10:00 AM EAT (lunch cut-off) and 5:00 PM EAT (dinner cut-off)
- **Hosting:** Vercel — free hosting for the Next.js admin panel with CI/CD from GitHub
- **Design Tools:** Figma or draw.io for wireframes and system diagrams
- **Testing:** Android Emulator (functional testing), physical Android device (performance and FCM testing), browser (admin panel testing)
- **Project Management:** GitHub Projects or similar for task tracking
