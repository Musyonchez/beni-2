# 8 — Deployment Notes & Lessons Learned

Covers what actually happened when deploying for the first time, problems hit,
and the exact fixes. Use this as a runbook for future deployments.

---

## Environment setup

### Node.js was not installed

The machine had no Node.js/npm. Installed via **winget**:

```
winget install OpenJS.NodeJS.LTS --accept-source-agreements --accept-package-agreements
```

Installs to `C:\Program Files\nodejs\`. After installation the PATH in the
current shell session is NOT updated — you must open a new terminal or prepend
manually:

```powershell
$env:PATH = 'C:\Program Files\nodejs;' + $env:PATH
```

### firebase-tools installed globally

```
npm install -g firebase-tools
```

Installs the `firebase` CLI to `C:\Users\Admin\AppData\Roaming\npm\`.

---

## firebase login

### Problem: non-interactive shell

Running `firebase login` from Claude Code's bash tool fails:

```
Error: Cannot run login in non-interactive mode.
```

### Fix

Run it from a real terminal (Windows Terminal / PowerShell window), not from
the Claude Code shell. The command opens a browser for Google OAuth, asks two
yes/no questions (Gemini feature + analytics), then prints:

```
✔  Success! Logged in as <your-email>
```

Login is stored in `~/.config/configstore/firebase-tools.json` and persists
across sessions — you only need to do this once per machine.

---

## firebase deploy

### Problem: wrong project ID in .firebaserc

`.firebaserc` was initialised with `"usiu-cafeteria"` (the display name), but
the actual Firebase project ID is `usiu-cafeteria-fc36c`. Deploy returned:

```
Error: HTTP 403 — Caller does not have required permission to use project usiu-cafeteria.
```

### Fix

List your projects to get the real ID:

```
firebase projects:list
```

Then update `.firebaserc`:

```json
{
  "projects": {
    "default": "usiu-cafeteria-fc36c"
  }
}
```

Always use the **Project ID** column from `projects:list`, not the display name.

---

### Problem: Cloud Functions require Blaze plan

Deploying `--only functions` failed with:

```
Error: Your project must be on the Blaze (pay-as-you-go) plan to complete this command.
Required API artifactregistry.googleapis.com can't be enabled until the upgrade is complete.
```

Cloud Functions (2nd gen) need Artifact Registry, Cloud Build, and Cloud
Functions APIs, all of which require Blaze.

### Fix

1. Go to:
   `https://console.firebase.google.com/project/usiu-cafeteria-fc36c/usage/details`
2. Click **Upgrade to Blaze**. Add a billing account and set a budget alert.
3. After upgrade run:
   ```
   firebase deploy --only functions
   ```

**Note:** Typical student usage stays well within the free-tier quotas even on
Blaze. Blaze is pay-as-you-go — you are only billed if you exceed free limits.

---

## What was successfully deployed (without Blaze)

Firestore rules and indexes deploy on the free Spark plan:

```
firebase deploy --only firestore:rules,firestore:indexes
```

Output:
```
+ cloud.firestore: rules file firestore.rules compiled successfully
+ firestore: deployed indexes in firestore.indexes.json successfully
+ firestore: released rules firestore.rules to cloud.firestore
+ Deploy complete!
```

**5 composite indexes created:**

| Collection          | Fields                                     |
|---------------------|--------------------------------------------|
| `orders`            | `userId` ASC + `createdAt` DESC            |
| `orders`            | `status` ASC + `createdAt` ASC             |
| `preOrders`         | `userId` ASC + `createdAt` DESC            |
| `preOrders`         | `mealSlot` + `scheduledDate` + `status` ASC|
| `walletTransactions`| `userId` ASC + `createdAt` DESC            |

---

## Full deployment checklist (future reference)

```bash
# 1. From cafeteria-app/app/
cd cafeteria-app/app

# 2. Install CLI (once per machine)
npm install -g firebase-tools

# 3. Log in (once per machine, opens browser)
firebase login

# 4. Confirm the correct project is set
firebase projects:list
# update .firebaserc "default" to match the Project ID column if wrong

# 5a. Deploy Firestore rules + indexes (Spark plan OK)
firebase deploy --only firestore:rules,firestore:indexes

# 5b. Deploy Cloud Functions (Blaze plan required)
firebase deploy --only functions

# 5c. Or deploy everything at once (Blaze)
firebase deploy --only functions,firestore:rules,firestore:indexes
```

---

## App behaviour while Functions are not yet deployed

The app is fully usable without Cloud Functions:

- Menu browsing, cart, wallet orders, cash orders — **all work**
- Real-time order status updates (staff → student) — **works** (Firestore listener)
- Wallet top-up by staff — **works**
- Pre-orders — **created and stored**, `status = "scheduled"` persists until Functions run
- Cut-off processing (deduct wallet, confirm/cancel) — **blocked** until Functions deployed
- FCM push notification "Order Ready" — **blocked** until Functions deployed

---

## Recurring pitfalls

| Pitfall | Symptom | Fix |
|---------|---------|-----|
| Wrong project ID in `.firebaserc` | HTTP 403 on deploy | Run `firebase projects:list`, use the **Project ID** column |
| Node not in PATH after install | `npm: command not found` | Open a new terminal or add `C:\Program Files\nodejs` to PATH |
| `firebase login` in non-interactive shell | "Cannot run login in non-interactive mode" | Run login from a real terminal window |
| Blaze plan not enabled | 403 on `artifactregistry.googleapis.com` | Upgrade at the Firebase console usage page |
| Functions deploy but not triggered | Scheduled functions miss today's pre-orders | Functions run in UTC — `scheduledDate` in Firestore must use `YYYY-MM-DD` in UTC+3 (EAT) |
| Stale FCM token | Notifications not received | `onNewToken` in `MyFirebaseMessagingService` handles this automatically |
