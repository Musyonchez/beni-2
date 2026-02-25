# 6.6 Application Deployment

The deployment phase of the USIU Cafeteria Ordering System packaged and published all components of the system into their respective production environments: the Android APK onto physical student and staff devices, the Supabase Edge Functions to the Supabase cloud platform, the scheduling jobs to cron-job.org, the Firestore security rules and indexes to the Firebase project, and the Next.js admin panel to Vercel. A key objective throughout this phase was to achieve full production deployment without incurring any infrastructure costs, utilising the free tiers of all platforms involved.

## 1. Gradle Build Configuration and Android APK Generation

The Android application's build configuration is defined in the module-level build.gradle file. The minSdkVersion is set to API Level 24 (Android 7.0), ensuring compatibility with the vast majority of student and staff devices on campus. The targetSdkVersion is set to the latest stable API level to leverage current Android security and performance features. The applicationId is set to com.usiu.cafeteria and the app is versioned for production release.

Dependencies declared in build.gradle include the Firebase BOM (Bill of Materials) for consistent Firebase SDK versioning, with individual Firebase modules for Firestore, Authentication, and Cloud Messaging. Additional dependencies include the Material Design 3 library, Glide for image loading with its annotation processor, and the AndroidX ViewModel and LiveData libraries for MVVM support.

The FUNCTIONS_SECRET value used to authenticate calls to the Supabase Edge Functions is stored in the module-level gradle.properties file, which is excluded from version control via .gitignore. It is exposed to the app at build time as a BuildConfig field, accessed in code as BuildConfig.FUNCTIONS_SECRET. This approach keeps the secret out of source control while making it available at runtime.

A signed release APK was generated using Android Studio's "Generate Signed Bundle/APK" wizard. A release keystore was created and used to sign the APK, a mandatory step for distribution and future update compatibility. The signed APK was then installed directly onto physical devices for testing and deployment.

> [Figure 43: Android build.gradle — Firebase BOM, Material Design 3, Glide, ViewModel/LiveData dependencies, and BuildConfig field for FUNCTIONS_SECRET]

## 2. Firestore Security Rules Deployment

Firestore security rules enforce role-based access control at the database level, ensuring that students can only read their own data, staff can access order and menu data, and admin operations are blocked at the client SDK level (forcing them through the Admin SDK in Next.js API routes).

The rules define four helper functions: isStudent() checks that the authenticated user's role field in Firestore equals "student"; isStaff() checks for "staff"; isAdmin() checks for "admin"; and isPrivileged() returns true if the user is either staff or admin. These helpers are used throughout the rules to gate collection access. For example, the walletTransactions collection allows reads only to the owning student or privileged users, and write operations that modify walletBalance are blocked for any client with the student role, preventing self-top-up.

The rules were deployed to the Firebase project using the Firebase CLI's firebase deploy --only firestore:rules command, making them active immediately across all client connections.

> [Figure 44: Firestore security rules — isStudent(), isStaff(), isAdmin(), isPrivileged() helper functions and representative collection-level access rules]

## 3. Firestore Composite Indexes Deployment

Five composite indexes were created in the Firebase Console's Firestore Indexes section to support the compound queries used by the application. Firestore requires explicit index definitions for any query that combines a field filter with an ordering on a different field, or that applies multiple equality or range filters simultaneously.

The deployed indexes are: userId ASC + createdAt DESC on the orders collection (for student order history); status ASC + createdAt ASC on orders (for the staff order queue); userId ASC + createdAt DESC on preOrders (for student pre-order listing); mealSlot ASC + scheduledDate ASC + status ASC on preOrders (for the cut-off function query); and userId ASC + createdAt DESC on walletTransactions (for wallet transaction history). Without these indexes, the corresponding Firestore queries would fail at runtime with a "requires an index" error.

## 4. Supabase Edge Functions Deployment

The two Supabase Edge Functions — process-cutoff and notify-order-ready — were deployed to the Supabase project using the Supabase CLI (supabase functions deploy). Each function resides in its own directory under supabase/functions/ and is deployed independently. The CLI packages the TypeScript source, uploads it to the Supabase platform, and makes it immediately available at its assigned HTTPS endpoint.

The FUNCTIONS_SECRET and the Firebase service account credentials (a JSON key file) are stored in the Supabase project as secrets using the supabase secrets set command. The secrets are injected into the edge function's Deno runtime as environment variables, accessible via Deno.env.get(). This keeps all sensitive credentials off the codebase and out of version control entirely.

> [Figure 45: Supabase CLI deployment — supabase functions deploy process-cutoff and supabase secrets set for FUNCTIONS_SECRET and Firebase service account credentials]

## 5. cron-job.org Scheduling Configuration

Two cron jobs were created in cron-job.org to trigger the process-cutoff Edge Function on the required schedule. The first job is configured to run at 07:00 UTC daily, corresponding to 10:00 AM East Africa Time, and sends an HTTP POST to the process-cutoff URL with the request body containing the mealSlot field set to "lunch". The second job runs at 14:00 UTC daily (5:00 PM EAT) with mealSlot set to "dinner". Both jobs include the Authorization header with the Bearer token set to the FUNCTIONS_SECRET value.

cron-job.org provides a web dashboard showing the execution history, response code, and response body for each job run. This serves as the primary monitoring tool for the pre-order cut-off automation, allowing the administrator to verify that each day's processing completed successfully and to inspect the summary of confirmed and cancelled pre-orders returned by the edge function.

> [Figure 46: cron-job.org scheduling configuration — two HTTP POST jobs at 07:00 UTC (lunch) and 14:00 UTC (dinner) targeting the process-cutoff Supabase Edge Function]

## 6. Next.js Admin Panel Deployment (Vercel)

The Next.js admin panel was deployed to Vercel by connecting the GitHub repository to a Vercel project. Vercel detects the Next.js framework automatically and configures the build pipeline accordingly. The admin panel is deployed from the cafeteria-app/admin/ subdirectory of the repository using Vercel's root directory configuration setting.

The Firebase service account credentials required by the Admin SDK in the Next.js API routes are stored as Vercel environment variables. These variables are injected into the Next.js server runtime at build and request time, and are never exposed to the browser. The client-side Firebase configuration (API key, project ID, etc.) is stored in public environment variables (prefixed NEXT_PUBLIC_) and is safe to include in the client bundle, as it grants access only within the bounds of the Firestore security rules.

The Vercel deployment is triggered automatically on each push to the main branch, making it straightforward to roll out updates to the admin panel. The admin panel is served over HTTPS by default on Vercel's infrastructure, ensuring encrypted communication between the administrator's browser and the Next.js server.

> [Figure 47: Vercel deployment dashboard — Next.js admin panel build log, environment variable configuration, and production deployment URL]

