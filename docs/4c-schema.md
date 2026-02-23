# Change 3 — Data Model Changes vs Original Plan

Reference: original schema was defined in `docs/3-decisions.md` Ch 5 (ERD section)
and `docs/2-project.md` (Data Model table).

---

## `users` collection

### Original plan

| Field | Type | Notes |
|---|---|---|
| uid | string | PK |
| name | string | |
| studentId | string | |
| email | string | |
| role | string | "student" or "staff" |
| walletBalance | number | all users |
| deviceToken | string | FCM token field name |
| createdAt | timestamp | |

### Actual build

| Field | Type | Notes |
|---|---|---|
| uid | string | PK |
| name | string | |
| studentId | string | students only (null/absent for staff/admin) |
| email | string | |
| role | string | "student", "staff", or "admin" |
| walletBalance | number | **students only** — absent for staff/admin |
| fcmToken | string | renamed from `deviceToken` |
| firstLogin | boolean | **staff/admin only** — true on creation, cleared after password change |
| createdAt | timestamp | |

### Delta

| Field | Change | Reason |
|---|---|---|
| `deviceToken` → `fcmToken` | Renamed | Matches Firebase SDK field name; more descriptive |
| `walletBalance` | Only on students | Staff/admin do not hold a wallet balance |
| `firstLogin` | New field | Forces password change on first login for staff/admin accounts |
| `role` | Now 3 values | Added "admin" role (was only "student"/"staff") |

---

## `walletTransactions` collection

### Original plan

| Field | Type | Notes |
|---|---|---|
| id | string | PK |
| userId | string | FK → users |
| type | string | "credit" or "debit" |
| amount | number | |
| description | string | |
| createdAt | timestamp | |

### Actual build

| Field | Type | Notes |
|---|---|---|
| id | string | PK |
| userId | string | FK → student being topped up or charged |
| type | string | "credit" or "debit" |
| amount | number | |
| description | string | |
| staffId | string | **New** — UID of staff who performed the transaction |
| createdAt | timestamp | |

### Delta

| Field | Change | Reason |
|---|---|---|
| `staffId` | New field | Audit trail — records which staff member did the top-up or deduction |

---

## `menuItems` collection

No changes. `imageUrl` was already in the plan and is present in the actual build.

| Field | Type |
|---|---|
| id | string PK |
| name | string |
| category | string |
| price | number |
| available | boolean |
| imageUrl | string |

---

## `orders` collection

No structural changes from the plan.

---

## `preOrders` collection

No structural changes from the plan.

---

## `orderItems` (embedded array in orders and preOrders)

No structural changes from the plan.

---

## Firestore security rules — added `isAdmin()` function

Original plan only had `isStaff()`. Actual rules now have:

```
function isStaff()      { return isAuth() && role() == 'staff'; }
function isAdmin()      { return isAuth() && role() == 'admin'; }
function isPrivileged() { return isStaff() || isAdmin(); }
```

All staff-level permissions now use `isPrivileged()` so both staff and admin can perform them.

---

## New feature: wallet deduction by staff

Original plan only described staff **crediting** (topping up) student wallets.
The actual build also allows staff to **deduct** (charge) a wallet — for example, if a student
owes for a cash-paid order that was adjusted.

- Type: `"debit"` in `walletTransactions`
- Uses same Firestore transaction (atomic) as top-up, but subtracts instead of adds
- Same `staffId` audit field recorded

Commit: `2036acc feat: add manual wallet deduction for staff`

---

## Commits that captured these changes

- `2a69e66 chore(app): step 2 — model classes` — initial model classes (fcmToken naming)
- `48572f4 chore: add google-services.json` — confirms Firestore schema in use
- `5698460 feat: record staffId on wallet top-up and deduction transactions`
- `2036acc feat: add manual wallet deduction for staff`
- `db708b9 fix: remove walletBalance from staff user documents`
- `c524c2b fix: allow admin role to read/write all Firestore collections`
- `69854b4 feat: force password change on first login for staff and admin`

---

## Impact on report chapters

| Chapter | Section | What to change |
|---|---|---|
| Ch 5 | ERD / Data model table | Update `users` (rename `deviceToken`→`fcmToken`, add `firstLogin`, note walletBalance students only) |
| Ch 5 | ERD / Data model table | Update `walletTransactions` (add `staffId`) |
| Ch 5 | Firestore rules description | Mention `isAdmin()` and `isPrivileged()` helper functions |
| Ch 5 | FR 14 | Change "Staff shall credit a student's wallet" → "Staff shall credit or deduct a student's wallet" |
| Ch 5 | FR (new) | "Each wallet transaction shall record the ID of the staff member who performed it" |
