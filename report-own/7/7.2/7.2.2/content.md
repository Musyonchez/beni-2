# 7.2.2 Security Testing

Security testing verified that the Firestore security rules correctly enforce role-based access boundaries across all user types. Testing was conducted using the Firebase Console Rules Playground, which allows simulated read and write operations to be authenticated with specific user tokens and roles without modifying live data. The three scenarios targeted the most critical security boundaries: student wallet self-modification, unauthenticated collection access, and staff elevated read permissions.

## Test Cases — Table 7.2: Security Testing

| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC10 | Student cannot self-modify wallet | In Firebase Console Rules Playground, simulate a write to `users/{studentUid}` with `walletBalance: 99999` using a student auth token | Write denied by Firestore rules | Rules Playground returned DENIED; the student rule only permits writing to the orders and preOrders collections, not to the users document | **Passed** |
| TC11 | Unauthenticated read denied | In Rules Playground, simulate a GET on the orders collection with no auth token | Read denied | Rules Playground returned DENIED; the `isAuth()` guard blocks all unauthenticated access across every collection | **Passed** |
| TC12 | Staff can read all orders | In Rules Playground, simulate a GET on the orders collection using a staff auth token (role = "staff") | Read allowed | Rules Playground returned ALLOWED; the `isPrivileged()` rule function (which includes `isStaff()` and `isAdmin()`) grants read access to all orders | **Passed** |

All three security test cases passed. The Firestore security rules correctly enforce all role boundaries tested. Unauthenticated requests are rejected at the outermost `isAuth()` guard before any collection-level rule is evaluated. Students are confined to their own data partitions and cannot modify sensitive fields such as `walletBalance`, which can only be written by privileged roles (staff and admin) via the StaffWallet interface. Staff and admin roles receive the correct elevated permissions through the shared `isPrivileged()` helper function. This rule architecture ensures that wallet integrity relies entirely on server-controlled staff operations rather than client-side writes.
