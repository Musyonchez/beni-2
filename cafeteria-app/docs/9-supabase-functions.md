# 9 — Supabase Edge Functions (Cloud Functions replacement)

Firebase Cloud Functions require the **Blaze (pay-as-you-go) plan**, which needs a credit card.
This doc explains how we replaced them with **Supabase Edge Functions** (free, no credit card).

## What moved where

| Original (Firebase) | Replacement |
|---|---|
| `lunchCutoff` scheduled function | cron-job.org → `supabase/functions/process-cutoff` |
| `dinnerCutoff` scheduled function | cron-job.org → `supabase/functions/process-cutoff` |
| `onOrderReady` Firestore trigger | Android calls `supabase/functions/notify-order-ready` after status update |

Firebase Auth, Firestore, and FCM remain on Firebase Spark (free) — nothing changed there.

---

## One-time setup

### 1. Create a Supabase project (free)

Go to [supabase.com](https://supabase.com), sign up, create a new project.
Note your **project ref** (looks like `abcdefghijklm`) from the project URL.

### 2. Install Supabase CLI

```bash
npm install -g supabase
supabase login
```

### 3. Get Firebase service account JSON

1. Firebase console → Project settings → Service accounts
2. Click **Generate new private key** → download JSON
3. Open the JSON file and minify it to one line (no newlines inside the JSON)

### 4. Set Supabase secrets

```bash
# From the repo root
supabase secrets set --project-ref <your-project-ref> \
  FIREBASE_SERVICE_ACCOUNT='<paste minified service account JSON here>' \
  FUNCTIONS_SECRET='<any random string, e.g. openssl rand -hex 32>'
```

### 5. Deploy edge functions

```bash
# From the repo root
supabase functions deploy process-cutoff    --project-ref <your-project-ref>
supabase functions deploy notify-order-ready --project-ref <your-project-ref>
```

After deploy, note the function URLs:
```
https://<project-ref>.supabase.co/functions/v1/process-cutoff
https://<project-ref>.supabase.co/functions/v1/notify-order-ready
```

### 6. Configure cron-job.org (for scheduled cutoffs)

Go to [cron-job.org](https://cron-job.org), create a free account, then add two jobs:

| Job | URL | Cron | When |
|---|---|---|---|
| Lunch cutoff | `https://<ref>.supabase.co/functions/v1/process-cutoff` | `0 7 * * *` | 07:00 UTC = 10:00 EAT |
| Dinner cutoff | `https://<ref>.supabase.co/functions/v1/process-cutoff` | `0 14 * * *` | 14:00 UTC = 17:00 EAT |

For each job, set:
- **Method:** POST
- **Header:** `Authorization: Bearer <FUNCTIONS_SECRET>`
- **Body:** `{"mealSlot":"lunch"}` or `{"mealSlot":"dinner"}`

### 7. Add secrets to Android build

In `cafeteria-app/app/gradle.properties` (create if it doesn't exist, never commit this file):

```properties
SUPABASE_NOTIFY_URL=https://<project-ref>.supabase.co/functions/v1/notify-order-ready
FUNCTIONS_SECRET=<same secret as above>
```

Then build the app — the values are injected via `BuildConfig`.

---

## Testing

```bash
# Test process-cutoff manually
curl -X POST https://<ref>.supabase.co/functions/v1/process-cutoff \
  -H "Authorization: Bearer <secret>" \
  -H "Content-Type: application/json" \
  -d '{"mealSlot":"lunch"}'

# Test notify-order-ready manually
curl -X POST https://<ref>.supabase.co/functions/v1/notify-order-ready \
  -H "Authorization: Bearer <secret>" \
  -H "Content-Type: application/json" \
  -d '{"userId":"<firebase-uid>"}'
```

---

## Pitfalls

| Problem | Fix |
|---|---|
| `Unauthorized` from edge function | `FUNCTIONS_SECRET` in Supabase secrets doesn't match value in cron-job.org header or Android BuildConfig |
| `Error: FIREBASE_SERVICE_ACCOUNT not set` | Secret not set, or set on wrong project ref |
| Service account JSON parse error | Must be a single line (no newlines). Use `jq -c . service-account.json` to minify |
| Notification not received | Check `users/{uid}.fcmToken` exists in Firestore; FCM token is written by `MyFirebaseMessagingService` on first app launch |
| `gradle.properties` values not picked up | File must be in `cafeteria-app/app/` (same dir as `build.gradle`), not repo root |
