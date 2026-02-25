# 11 — Chapter 7 Testing Brief

## What this doc is for

This session's job is to manually test the cafeteria app and produce
`cafeteria-app/docs/12-ch7-test-results.md`. That file will be handed
to the report-writing AI to write Chapter 7 (Testing & Evaluation).

You do NOT need to write JUnit tests or Espresso tests.
Just run the app on a device/emulator, perform the scenarios below,
observe and record what happens, and write the output MD.

---

## Structure of the report chapter (so you know what's needed)

- 7.1 Introduction
- 7.2 Unit Testing — CartViewModel logic tested manually via code inspection + running
- 7.3 System Testing
  - 7.3.1 Functional Testing — test case table (TC01–TC09)
  - 7.3.2 Security Testing — test case table (TC10–TC12, Firestore rules)
  - 7.3.3 Performance Testing — test case table (TC13–TC15, response times)
- 7.4 User Acceptance Testing (UAT) — narrative, did real users try it?
- 7.5 Key Findings — what worked, what didn't
- 7.6 Impact Analysis — who benefits and how
- 7.7 Conclusion

---

## Test scenarios to perform and record

### Functional (TC01–TC09)

Run each scenario on a physical device or emulator. Note exactly what happened.

| ID | Scenario |
|----|----------|
| TC01 | Student registers (name, studentId, email, password) → lands on Menu screen |
| TC02 | Student adds 2 items to cart → changes quantity → removes one → total updates |
| TC03 | Student places wallet order (sufficient balance) → order appears in Orders tab with status "Pending" |
| TC04 | Student places wallet order with insufficient balance → app blocks and shows warning |
| TC05 | Student places cash order → order appears in Orders tab |
| TC06 | Staff logs in → sees StaffOrders tab → taps "Start Preparing" → student order chip turns blue |
| TC07 | Staff taps "Mark Ready" → student receives notification → chip turns green |
| TC08 | Student schedules a pre-order (future date, Lunch) → appears in Pre-orders tab as "Scheduled" |
| TC09 | Staff opens StaffWallet → looks up student by studentId → credits wallet → student balance updates |

For each TC record:
- Test Steps (brief)
- Expected Result
- Actual Result (what actually happened on screen)
- Status: **Passed** or **Failed**

---

### Security (TC10–TC12)

Test the Firestore rules. You can do this directly in the Firebase Console
(Firestore → Rules → Rules Playground) or by trying restricted operations in the app.

| ID | Scenario |
|----|----------|
| TC10 | Student user tries to directly write to walletBalance field → should be denied |
| TC11 | Unauthenticated request tries to read orders collection → should be denied |
| TC12 | Staff user reads all orders (all students) → should be allowed |

---

### Performance (TC13–TC15)

Time these operations approximately (use a stopwatch or visual observation).

| ID | Scenario |
|----|----------|
| TC13 | App launch → time until Menu screen is fully loaded with items |
| TC14 | Staff marks order Ready → time until student's order chip updates on their screen |
| TC15 | Student places order → time until it appears in StaffOrders list |

---

## Unit Testing section (7.2)

No JUnit required. Instead:
- Describe the CartViewModel logic: addItem(), removeItem(), updateQuantity(), clearCart()
- Describe the wallet check logic in CartFragment (balance >= subtotal check)
- Describe the estimated wait time formula (activeOrderCount × 5, min 5)
- Note that these were verified by running the app through TC02–TC04 above

---

## UAT section (7.4)

Did any real users (classmates, lecturers, friends) try the app?
- If yes: note who (role: student/staff), what they tried, and their feedback
- If no: describe a simulated UAT session where you played each role yourself and noted observations

---

## Output format: `cafeteria-app/docs/12-ch7-test-results.md`

Write this file with the following sections.
Be specific — the report writer needs real observed values (e.g. "loaded in ~2 seconds",
"notification arrived in ~3 seconds"). Do not invent values; record what you actually saw.

```
# Chapter 7 — Test Results

## 7.2 Unit Testing
[Description of CartViewModel logic and the three formulas/checks, verified via app run]

## 7.3.1 Functional Testing

### Table: Functional Test Cases
| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC01 | ... | ... | ... | ... | Passed/Failed |
...

[Brief paragraph explaining the results]

## 7.3.2 Security Testing

### Table: Security Test Cases
| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
...

[Brief paragraph]

## 7.3.3 Performance Testing

### Table: Performance Test Cases
| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
...

[Brief paragraph]

## 7.4 UAT
[Narrative: who tested, what they did, feedback received, any issues noted]

## 7.5 Key Findings
### a) Functionality
### b) Performance
### c) Security
### d) Usability

## 7.6 Impact Analysis
### i. Students
### ii. Cafeteria Staff
### iii. Administrators
### iv. Platform Maintainability
### v. SDGs (SDG 4: Quality Education, SDG 9: Innovation, SDG 11: Sustainable Communities)

## 7.7 Conclusion
[Short wrap-up]
```

---

## Commit when done

```
git add cafeteria-app/docs/12-ch7-test-results.md
git commit -m "docs(ch7): add test results for Chapter 7"
```
