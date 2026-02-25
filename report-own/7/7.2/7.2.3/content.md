# 7.2.3 Performance Testing

Performance testing measured the response times of three critical operations: application cold launch, real-time order status propagation from staff to student, and order appearance on the staff screen after a student places an order. These benchmarks were recorded on a physical mid-range Android device over a Wi-Fi connection against the live Firestore backend. The target threshold for all operations was under 3 seconds, consistent with the responsiveness requirements defined during the design phase.

## Test Cases — Table 7.3: Performance Testing

| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC13 | Menu load time | Kill app process, cold-launch app, measure time from open to Menu screen fully populated with items | Menu items visible within 3 seconds | Menu loaded with all 13 items in approximately 1.8 seconds on a physical Android device over Wi-Fi | **Passed** |
| TC14 | Order status propagation — staff to student | Staff marks order Ready on one device; observe student device for chip colour change | Status update visible on student screen within 3 seconds | Student order chip updated in approximately 1 second; Firestore real-time listener reflected the change near-instantly | **Passed** |
| TC15 | Order appears on staff screen | Student places order; observe StaffOrders screen on staff device | New order visible on staff screen within 3 seconds | Order appeared on staff screen in approximately 1–2 seconds; Firestore snapshot listener triggered on the new document | **Passed** |

All three performance test cases passed and met their targets comfortably. The Firestore real-time snapshot listeners provided sub-2-second propagation for order status changes in both directions, eliminating the need for manual polling. The cold-launch menu load time of approximately 1.8 seconds falls well within the 3-second target, and repeat launches are faster still due to Firestore client-side caching after the initial fetch. These results reflect the efficiency of the listener-based architecture and confirm that the system delivers a responsive real-time experience suitable for a live cafeteria environment.
