# 10 — Supabase Functions Deployment Log

This doc covers everything that happened after Doc 9 was written — the actual deployment
process, every error hit, and how each was resolved. Useful as a runbook for next time.

---

## Why we switched from Firebase Cloud Functions

Firebase Cloud Functions require the **Blaze (pay-as-you-go) plan**, which needs a linked
credit card. The project was already on the Spark (free) plan and all other Firebase services
(Auth, Firestore, FCM) work fine on it. Rather than upgrade billing, we replaced only the
Cloud Functions with **Supabase Edge Functions** — free, no credit card, no spending risk.

What was replaced:

| Was (Firebase) | Now (Supabase) |
|---|---|
| `lunchCutoff` / `dinnerCutoff` scheduled functions | `process-cutoff` edge function, called by cron-job.org |
| `onOrderReady` Firestore trigger | `notify-order-ready` edge function, called by Android app |

Everything else stayed on Firebase.

---

## Step-by-step: what happened

### 1. Tried to install Supabase CLI via npm — failed

```bash
npm install -g supabase
```

**Error:** `Installing Supabase CLI as a global module is not supported.`

Supabase CLI cannot be installed via npm. Their docs recommend Scoop, Homebrew, or a direct
binary download.

**Fix:** Downloaded the Windows binary directly from GitHub releases:

```bash
# Find latest release URL
node -e "
const https = require('https');
https.get('https://api.github.com/repos/supabase/cli/releases/latest',
  {headers: {'User-Agent': 'node'}}, r => {
    let d = ''; r.on('data', c => d += c);
    r.on('end', () => {
      const asset = JSON.parse(d).assets.find(a => a.name.includes('windows_amd64'));
      console.log(asset.browser_download_url);
    });
  });
"

# Download and extract (v2.75.0 as of Feb 2026)
powershell.exe -Command "Invoke-WebRequest -Uri '<url>' -OutFile 'C:\Users\Admin\supabase.tar.gz' -UseBasicParsing"
mkdir -p /c/supabase-cli
tar -xzf /c/Users/Admin/supabase.tar.gz -C /c/supabase-cli

# Add to PATH for the session
export PATH="$PATH:/c/supabase-cli"
supabase --version   # 2.75.0
```

---

### 2. Supabase login — non-interactive shell issue

Same problem as Firebase login: the bash shell tool has no TTY, so interactive login fails.

**Fix:** User ran `C:\supabase-cli\supabase.exe login` in their own Windows terminal. The
access token is stored system-wide, so subsequent CLI calls in the bash shell pick it up.

---

### 3. Tried `winget install Supabase.CLI` — not found

Winget does not have a Supabase CLI package (as of Feb 2026). Use the direct binary download
approach described above.

---

### 4. Set secrets

```bash
# FUNCTIONS_SECRET — shared bearer token
supabase secrets set FUNCTIONS_SECRET=<hex string> --project-ref mjkowkfdneyegqtodxur

# FIREBASE_SERVICE_ACCOUNT — Firebase Admin credentials
# Downloaded from Firebase console → Project settings → Service accounts → Generate new private key
# Placed in cafeteria-app/ (excluded from git via cafeteria-app/.gitignore)
node -e "
const fs = require('fs');
console.log(JSON.stringify(JSON.parse(
  fs.readFileSync('C:/Users/Admin/Code/beni-2/cafeteria-app/<filename>.json', 'utf8')
)));
"
# Paste the output as the secret value
supabase secrets set FIREBASE_SERVICE_ACCOUNT='<minified json>' --project-ref mjkowkfdneyegqtodxur
```

**Important:** The service account JSON must be a single-line minified string.
`JSON.stringify(JSON.parse(...))` handles this automatically.

---

### 5. First deploy — got 401 "Invalid JWT"

```bash
supabase functions deploy process-cutoff --project-ref mjkowkfdneyegqtodxur
```

Test returned: `{"code":401,"message":"Invalid JWT"}`

**Cause:** Supabase Edge Functions enforce their own JWT check **before** function code runs.
Our custom `Authorization: Bearer <secret>` header is not a Supabase JWT.

**Fix:** Redeploy with `--no-verify-jwt`. Our own auth check inside the function handles
security instead:

```bash
supabase functions deploy process-cutoff      --project-ref mjkowkfdneyegqtodxur --no-verify-jwt
supabase functions deploy notify-order-ready  --project-ref mjkowkfdneyegqtodxur --no-verify-jwt
```

---

### 6. Second deploy — got 500 "Internal Server Error" (plain text body)

After fixing the 401, both functions returned a plain-text `Internal Server Error` with no
JSON body or detail. Plain text meant the error happened **at module load time**, before
`Deno.serve()` even ran.

**Root cause:** The original functions had top-level code:

```typescript
if (getApps().length === 0) {
  const sa = JSON.parse(Deno.env.get("FIREBASE_SERVICE_ACCOUNT")!);
  initializeApp({ credential: cert(sa) });
}
const db = getFirestore();  // crashes if initializeApp threw above
```

If `initializeApp` failed for any reason, `getFirestore()` also crashed and killed the module.

**Fix:** Moved all Firebase initialization inside the request handler with a try/catch so
errors are returned as JSON responses instead of crashing the module silently.

---

### 7. Third deploy — got 500 with gRPC connection error

With better error handling in place, the 500 now returned a useful message:

```json
{
  "error": "14 UNAVAILABLE: No connection established. Last error: Error: Client network
  socket disconnected before secure TLS connection was established."
}
```

**Root cause:** Firebase Admin SDK uses **gRPC** as the default transport for Firestore.
gRPC does not work in Deno edge runtime (Supabase Edge Functions run on Deno).

**Fix:** Add `preferRest: true` to Firestore settings to force HTTP/REST transport:

```typescript
const db = getFirestore(app);
db.settings({ preferRest: true });  // critical for Deno/edge environments
```

---

### 8. Functions working

After the `preferRest` fix both functions return 200:

```
process-cutoff:      HTTP 200  {"ok":true,"mealSlot":"lunch"}
notify-order-ready:  HTTP 200  {"ok":true,"notified":false}
```

`notified: false` on the test is expected — the test UID has no Firestore doc, so no FCM
token is found. With a real user UID it sends the push notification.

---

## Summary of all errors and fixes

| # | Error | Cause | Fix |
|---|---|---|---|
| 1 | `npm install -g supabase` fails | Supabase CLI not supported via npm | Download binary from GitHub releases |
| 2 | `winget install Supabase.CLI` not found | No winget package exists | Use GitHub binary download |
| 3 | Login fails in bash shell | Non-interactive shell (no TTY) | Run login in a real Windows terminal window |
| 4 | HTTP 401 "Invalid JWT" | Supabase JWT layer runs before function code | Deploy with `--no-verify-jwt` |
| 5 | HTTP 500 plain text (no JSON body) | Top-level module code crashed before handler ran | Move Firebase init inside handler with try/catch |
| 6 | HTTP 500 gRPC "UNAVAILABLE: No connection established" | gRPC not supported in Deno edge runtime | Add `db.settings({ preferRest: true })` |

---

## Key lessons

- **Supabase CLI on Windows:** Always use the GitHub binary download, not npm or winget.
- **`--no-verify-jwt`:** Required when your function uses its own auth instead of Supabase
  JWTs. Passed at deploy time.
- **Firebase Admin + Deno:** Always set `preferRest: true` on the Firestore instance.
  gRPC will never work in an edge runtime.
- **Top-level code in edge functions:** Any code outside `Deno.serve()` that can throw will
  kill the module with no useful error message. Keep initialization lazy (inside the handler).
- **Service account JSON:** Must be minified to one line before setting as a Supabase secret.
  Use `JSON.stringify(JSON.parse(rawJson))` in Node.js.
- **Testing from Windows:** Use Node.js `https.request()` instead of curl to avoid Windows
  shell quoting headaches with JSON bodies.

---

## Deployed function URLs

```
process-cutoff:      https://mjkowkfdneyegqtodxur.supabase.co/functions/v1/process-cutoff
notify-order-ready:  https://mjkowkfdneyegqtodxur.supabase.co/functions/v1/notify-order-ready
```

Both require: `Authorization: Bearer 5cc10310c1ceeb668ae07a66eceab29cf82d78e46ef89e2783f724e163b0af47`

---

## Still TODO

Set up cron-job.org with two jobs (see Doc 9 for full instructions):

| Job | Cron | Body |
|---|---|---|
| Lunch cutoff | `0 7 * * *` UTC | `{"mealSlot":"lunch"}` |
| Dinner cutoff | `0 14 * * *` UTC | `{"mealSlot":"dinner"}` |
