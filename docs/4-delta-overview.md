# Delta Overview — What Changed After Chapters 1–5 Were Written

Chapters 1–5 of the report were written based on `docs/3-decisions.md` (the original plan).
During implementation several things changed. This folder documents those changes so the report
can be corrected.

---

## Summary of Changes

| # | What changed | Original plan | Actual build | Affects chapters |
|---|---|---|---|---|
| 1 | Scheduled job runtime | Firebase Cloud Functions (Blaze) | Supabase Edge Functions (free) | Ch 4, Ch 5 |
| 2 | Admin role + web panel | No admin role; staff use Android APK | 3rd role "admin" uses a Next.js web panel | Ch 3, Ch 4, Ch 5 |
| 3 | Staff account creation | Staff self-register via Android app | Admin creates staff accounts via web panel | Ch 4, Ch 5 |
| 4 | Wallet deduction by staff | Staff top-up (credit) only | Staff can also deduct (charge) wallet manually | Ch 5 |
| 5 | Data model additions | `deviceToken`, no `firstLogin`, no `staffId` | `fcmToken`, `firstLogin`, `staffId` on transactions | Ch 5 |

---

## Detailed Docs

- [4a-supabase.md](4a-supabase.md) — Cloud Functions replaced by Supabase Edge Functions
- [4b-three-roles.md](4b-three-roles.md) — Third role (admin) + Next.js web admin panel
- [4c-schema.md](4c-schema.md) — Exact data model changes vs the original plan
- [4d-report-edits.md](4d-report-edits.md) — Chapter-by-chapter edit guide for Ch 1–5

---

## Reading order

Read in order: 4a → 4b → 4c → then use 4d to edit the report.
The original plan is in `docs/3-decisions.md` for reference.
