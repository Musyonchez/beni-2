# APPENDIX B: TECHNICAL DEPLOYMENT AND MAINTENANCE GUIDE

This appendix provides a technical reference for deploying, configuring, and maintaining the cloud infrastructure of the USIU Cafeteria Ordering System. It assumes familiarity with Firebase, Supabase, and Git.

---

## B.1 Deployment Prerequisites

The following accounts and tools must be in place before deployment begins. All services operate on free tiers — no credit card or billing account is required.

**Accounts required:**
- Firebase (Spark free tier) — Firestore, Authentication, Cloud Messaging
- Supabase (free tier) — Edge Functions hosting
- Vercel (free tier) — Next.js admin panel hosting
- cron-job.org (free tier) — scheduled HTTP trigger jobs
- GitHub — source code repository

**Tools required:**
- Android Studio (Ladybug 2024.2.1 or newer) — for building and signing the Android APK
- Node.js v18 or newer — for Next.js admin panel development
- Git — version control
- Supabase CLI — for deploying and managing Edge Functions
- Firebase CLI — for deploying Firestore security rules and composite indexes

---

## B.2 Environment Variables and Secrets

Sensitive credentials are never stored in source code. Each platform has its own secure storage mechanism. The table below lists every secret used by the system and where it is stored.

| Platform | Variable Name | Purpose | Where to Store |
|---|---|---|---|
| Android App | `FUNCTIONS_SECRET` | Shared secret used to authenticate HTTP calls from the app to the Supabase Edge Functions. | `gradle.properties` — excluded from version control via `.gitignore`. Exposed to app code at build time as `BuildConfig.FUNCTIONS_SECRET`. |
| Supabase | `FUNCTIONS_SECRET` | Same shared secret — validated by Edge Functions on every incoming request via the `Authorization: Bearer` header. | `supabase secrets set FUNCTIONS_SECRET=...` |
| Supabase | `FIREBASE_SERVICE_ACCOUNT` | Firebase service account JSON key — used by the Firebase Admin SDK inside the Edge Functions to read Firestore and send FCM notifications. | `supabase secrets set FIREBASE_SERVICE_ACCOUNT="$(cat key.json)"` |
| Vercel | `FIREBASE_ADMIN_*` | Firebase Admin SDK service account credentials — used by Next.js server-side API routes to create staff accounts and write Firestore documents. | Vercel Project Settings → Environment Variables (server-side only, not exposed to the browser). |
| Vercel | `NEXT_PUBLIC_FIREBASE_*` | Client-side Firebase configuration (API key, project ID, etc.) — used by the Next.js browser client to authenticate and read Firestore within the bounds of security rules. | Vercel Project Settings → Environment Variables (prefixed `NEXT_PUBLIC_`, safe to expose to the client bundle). |

---

## B.3 Deployment Steps

### Step 1 — Firebase Rules and Indexes

Deploy the Firestore security rules and composite indexes to the Firebase project using the Firebase CLI:

```bash
firebase deploy --only firestore:rules
firebase deploy --only firestore:indexes
```

Both commands read from `firestore.rules` and `firestore.indexes.json` in the project root. Run these commands once on initial setup and again whenever either file is modified.

### Step 2 — Supabase Edge Functions

Navigate to the `supabase/functions/` directory and deploy each function independently:

```bash
supabase functions deploy process-cutoff
supabase functions deploy notify-order-ready
```

Set the required secrets once using the Supabase CLI:

```bash
supabase secrets set FUNCTIONS_SECRET=your-shared-secret-here
supabase secrets set FIREBASE_SERVICE_ACCOUNT="$(cat service-account-key.json)"
```

Secrets are injected into the Deno runtime as environment variables at invocation time and are never stored in the function source code.

> **Critical implementation note:** Both Edge Functions must include the line `db.settings({ preferRest: true })` immediately after initialising the Firebase Admin SDK Firestore client. The Deno runtime used by Supabase does not support gRPC, which is the default transport protocol for the Firebase Admin SDK. Without this setting, all Firestore calls from the Edge Functions will fail silently. Setting `preferRest: true` forces the SDK to use the Firestore REST API, which is fully compatible with the Deno HTTP stack.

### Step 3 — cron-job.org Scheduling

Create two HTTP POST jobs in cron-job.org with the following configuration:

| Job Name | Endpoint URL | Schedule (UTC) | Request Body |
|---|---|---|---|
| `cafeteria-lunch-cutoff` | `https://[project-ref].supabase.co/functions/v1/process-cutoff` | `0 7 * * *` (07:00 UTC = 10:00 AM EAT) | `{"mealSlot":"lunch"}` |
| `cafeteria-dinner-cutoff` | `https://[project-ref].supabase.co/functions/v1/process-cutoff` | `0 14 * * *` (14:00 UTC = 5:00 PM EAT) | `{"mealSlot":"dinner"}` |

For both jobs, add the following HTTP header to authenticate the request:

```
Authorization: Bearer YOUR_FUNCTIONS_SECRET
```

Replace `[project-ref]` with the Supabase project reference ID found in the Supabase project settings. Replace `YOUR_FUNCTIONS_SECRET` with the same value stored in the Supabase secrets.

### Step 4 — Android APK Generation

1. In Android Studio, open the project and navigate to **Build → Generate Signed Bundle / APK**.
2. Select **APK** and click Next.
3. Select your release keystore (create one on first release using the **Create new** option — store the keystore file and passwords securely).
4. Select the **release** build variant and click Finish.
5. The signed APK is generated at `app/build/outputs/apk/release/app-release.apk`.

Install the APK directly on student and staff devices via USB or a file transfer mechanism. The app requires Android 7.0 (API Level 24) or higher.

> **Note:** The `FUNCTIONS_SECRET` value must be present in `gradle.properties` before building. Confirm the line `FUNCTIONS_SECRET=your-secret` exists in that file and that `gradle.properties` is listed in `.gitignore`.

### Step 5 — Next.js Admin Panel (Vercel)

1. Push the repository to GitHub (the admin panel source resides in the `admin/` subdirectory).
2. In Vercel, click **Add New Project** and import the GitHub repository.
3. Set the **Root Directory** to `admin` in the Vercel project settings.
4. Add all environment variables listed in Section B.2 — both the server-side Admin SDK credentials and the `NEXT_PUBLIC_` client-side Firebase config.
5. Click **Deploy**. Vercel will build and deploy the Next.js application automatically.

All subsequent pushes to the `main` branch on GitHub will trigger an automatic redeployment, making admin panel updates straightforward to roll out.

---

## B.4 Firestore Composite Indexes

Firestore requires explicitly defined composite indexes for any query that combines a field filter with an ordering on a different field, or that applies multiple equality or range filters simultaneously. Without these indexes, the corresponding queries will throw a runtime error. The five indexes below must be deployed before the system is used.

Deploy via Firebase Console → Firestore Database → Indexes tab, or include them in `firestore.indexes.json` and deploy with `firebase deploy --only firestore:indexes`.

| Collection | Index Fields | Query Purpose |
|---|---|---|
| `orders` | `userId` (Ascending) + `createdAt` (Descending) | Student order history — retrieve a student's past orders sorted by most recent. |
| `orders` | `status` (Ascending) + `createdAt` (Ascending) | Staff active order queue — retrieve all non-collected orders in arrival order. |
| `preOrders` | `userId` (Ascending) + `createdAt` (Descending) | Student pre-order listing — retrieve a student's scheduled pre-orders. |
| `preOrders` | `mealSlot` (Ascending) + `scheduledDate` (Ascending) + `status` (Ascending) | Cut-off function batch query — retrieve all pre-orders for a given meal slot and date with status "scheduled". |
| `walletTransactions` | `userId` (Ascending) + `createdAt` (Descending) | Wallet transaction history — retrieve a student's wallet activity sorted by most recent. |

---

## B.5 Common Maintenance Tasks

The table below covers the most frequent operational and maintenance scenarios that system administrators and support staff will encounter.

| Task | Procedure |
|---|---|
| **Top up a student's wallet** | Staff member uses the StaffWallet tab in the Android app: enter the student's ID, enter the amount, tap Credit. No admin panel action is needed. |
| **Reset a staff member's password** | The admin panel does not support password resets directly. Delete the staff account from the Users Directory page, then re-create it on the Staff Account Creation page with a new temporary password. |
| **Remove a student account** | Firebase Console → Authentication → locate the user → Delete. Also delete the corresponding document from the `users` collection in Firestore. |
| **Add or update a menu item** | Admin Web Panel → Menu Management → Add Item, or click Edit on an existing row. Changes are reflected on all student devices within seconds via Firestore snapshot listeners. |
| **Check pre-order cut-off execution** | Login to cron-job.org → select the relevant job → open the Execution History tab. Each successful run returns HTTP 200 with a JSON summary of confirmed and cancelled pre-orders. |
| **Redeploy an Edge Function after a code change** | Run `supabase functions deploy process-cutoff` or `supabase functions deploy notify-order-ready` from the project root. The updated function is live immediately. |
| **Monitor Firestore quota usage** | Firebase Console → Firestore Database → Usage tab. The Spark free tier allows 50,000 reads, 20,000 writes, and 20,000 deletes per day. Usage at prototype scale is well within these limits. |
| **Rotate the FUNCTIONS_SECRET** | Generate a new secret string. Update it in `gradle.properties` and rebuild the APK. Update it in Supabase using `supabase secrets set FUNCTIONS_SECRET=new-value`. Update it in both cron-job.org job headers. All four locations must be updated before distributing the new APK, otherwise Edge Function calls from the old APK will be rejected. |
| **Add a new admin account** | Firebase Console → Authentication → Add User. Then manually create a Firestore document in the `users` collection with the new UID and `role: "admin"`. Set `firstLogin: true` to enforce a password change on first access to the admin panel. |
