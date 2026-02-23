# Change 1 — Cloud Functions Replaced by Supabase Edge Functions

## What the original plan said (docs/3-decisions.md, Ch 4 & Ch 5)

- Scheduled pre-order cut-off jobs run as **Firebase Cloud Functions** with cron triggers:
  - `lunchCutoff` — `0 10 * * *` (10:00 AM daily)
  - `dinnerCutoff` — `0 17 * * *` (5:00 PM daily)
- FCM notification on order-ready also triggered by a Cloud Function (Firestore trigger)
- Firebase project on **Blaze (pay-as-you-go) plan** — required for Cloud Functions
- Chapter 4 requirements table listed: "Firebase Blaze plan required for Cloud Functions"
- Chapter 5 DFD listed: "Cut-off job: Cloud Function scans preOrders at 10 AM / 5 PM"

## What was actually built

Firebase Cloud Functions require the Blaze plan (credit card required). The project stays on the
free **Spark plan**, so Cloud Functions are not available. The equivalent logic was deployed as
**Supabase Edge Functions** (free tier, no credit card).

### Supabase Edge Functions deployed

| Function | URL | Trigger |
|---|---|---|
| `process-cutoff` | `https://mjkowkfdneyegqtodxur.supabase.co/functions/v1/process-cutoff` | HTTP POST by cron-job.org |
| `notify-order-ready` | `https://mjkowkfdneyegqtodxur.supabase.co/functions/v1/notify-order-ready` | HTTP POST by Android app after status update |

### Scheduling: cron-job.org (free)
Two cron jobs set up at cron-job.org:
- 10:00 AM EAT (07:00 UTC) — calls `process-cutoff` with body `{ "mealSlot": "lunch" }`
- 5:00 PM EAT (14:00 UTC) — calls `process-cutoff` with body `{ "mealSlot": "dinner" }`

### Notification trigger: Android → Supabase (not Firestore trigger)
The original plan used a Firestore trigger (Cloud Function fires when order status changes to
"ready"). Since Firestore triggers require Cloud Functions / Blaze plan, the Android approach
changed: when a staff member marks an order "Ready", the Android app fires a background HTTP POST
to `notify-order-ready` directly after the Firestore write succeeds. Fail-silent.

### Runtime
- Supabase Edge Functions run on **Deno** (TypeScript)
- Uses `npm:firebase-admin` to read Firestore (user FCM token) and send FCM messages
- Critical setting: `db.settings({ preferRest: true })` — gRPC does not work in Deno runtime

### Security
- All edge function calls require `Authorization: Bearer <FUNCTIONS_SECRET>` header
- Secret stored in Supabase and in Android `gradle.properties` (gitignored)

## Commits that captured this change

- `92f3a4d feat: add Supabase Edge Functions replacing Firebase Cloud Functions`
- `1604b5d feat: wire Android app to Supabase notify-order-ready edge function`
- `2750336 fix: use preferRest + lazy Firebase init in Supabase edge functions`
- `5241464 docs: add doc 9 — Supabase Edge Functions setup and deployment guide`
- `30d4a8d docs: add doc 10 — Supabase deployment log and lessons learned`

## Impact on report chapters

| Chapter | Section | What to change |
|---|---|---|
| Ch 4 | Requirements table (server row) | Remove "Firebase Blaze plan". Add "Supabase free plan (Edge Functions)" and "cron-job.org (free cron)" |
| Ch 4 | Phase 2 description | Change "Cloud Functions" to "Supabase Edge Functions" |
| Ch 4 | Software/tools list | Add: Supabase CLI, cron-job.org |
| Ch 5 | Architecture diagram / description | Replace "Cloud Functions" layer with "Supabase Edge Functions" |
| Ch 5 | DFD level 1, process 9 | "Cut-off job: **Supabase Edge Function** called by cron-job.org at 10 AM / 5 PM" |
| Ch 5 | DFD level 1, process 7 | "FCM notification sent via **Supabase Edge Function** triggered by Android after status update" |
| Ch 5 | NFR 3 | "Pre-order cut-off **edge function** shall execute within 60 seconds of the scheduled time" |
