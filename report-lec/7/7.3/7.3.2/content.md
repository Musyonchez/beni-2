# 7.3.2 Smart Facility Status Validation

This objective focuses on validating the accuracy and reliability of the "Smart Status" system, which serves as a critical decision-support tool for students and faculty. The feature is designed to automatically determine whether a facility is "Open" or "Closed" by cross-referencing the device's current system time against a predefined schedule of operating hours. During UAT, testers simulated various time-based scenarios—such as checking academic buildings late at night or sports facilities on weekends—to verify that the status indicators updated dynamically and correctly.

The evaluation prioritized user trust; incorrect status information could lead to wasted trips and user frustration. Therefore, testing involved checking edge cases, such as the transition minute when a facility closes (e.g., 5:00 PM), to ensure the UI reflects the change instantly. Users confirmed that the visual cues—Gold for "Open" and Red for "Closed"—were intuitive and helpful for planning their campus movements. This validation ensures that the application provides actionable, real-time intelligence that aligns with the actual operational schedule of USIU Africa.

Figure 64 demonstrates how the app verifies the accuracy of facility operating status using unit tests. The code simulates different times and days for various facilities—academic, administrative, and sports—to ensure the system correctly identifies whether each facility is "Open" or "Closed." This testing confirms that the time-based and day-of-week logic is functioning properly, preventing users from seeing incorrect facility availability in real-world use.

**Line 1:** Annotates the method with @Test, identifying it as a unit test case to be executed by the JUnit runner.

**Lines 4–6:** Sets up the first test scenario for an "Academic" facility. It creates a mock Calendar object and manually sets the time to 10:00 AM, simulating a standard school day morning.

**Line 9:** Executes the assertion assertEquals("Open", ...). This line critically verifies that the logic correctly identifies 10 AM as an operating hour for the library. If the logic returns "Closed," the test fails immediately.

**Lines 12–14:** Sets up a specific edge case for "Admin" offices. It creates a mock time object set to Calendar.SUNDAY. This tests the day-of-week logic, ensuring offices are marked closed even if the user checks at 10 AM on a weekend.

**Line 17:** Validates that the registrar.calculateStatus method returns "Closed," confirming the weekend restriction logic is functioning.

**Lines 20–22:** Sets up a time-based boundary test for "Sports" facilities. It sets the time to 18:00 (6 PM), which is one hour after the standard 5 PM closing time.

**Line 25:** Asserts that the status is "Closed," proving that the logic correctly handles the end-of-day transition.

## Table 5: Smart Facility Status Validation Test Cases

| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC14 | Standard Operating Hours | 1. Set system time to Tuesday, 10:00 AM. 2. Open the Facilities Directory. 3. Locate "Registrar Office". | The status indicator should display "Open" in Gold text. | Status displayed "Open" correctly. | Passed |
| TC15 | Weekend Closure Logic | 1. Set system time to Sunday, 2:00 PM. 2. Open the Facilities Directory. 3. Locate "Registrar Office". | The status indicator should display "Closed" in Red text (as offices are closed on weekends). | Status displayed "Closed" correctly. | Passed |
| TC16 | After-Hours Transition | 1. Set system time to Wednesday, 5:01 PM. 2. Refresh the Facilities list. 3. Locate "Finance Office" (Closes at 5 PM). | The status should immediately switch from "Open" to "Closed". | Status updated to "Closed" instantly. | Passed |

**TC14** confirmed the standard positive case. By simulating a typical weekday morning, the test verified that the system correctly interprets the "Open" condition for administrative offices, providing users with accurate information for planning their visits.

**TC15** tested the specific day-of-week constraint. Since administrative offices are closed on weekends, this test ensured that the logic correctly overrides the time-of-day check (2:00 PM is usually open) with the day-of-week check (Sunday is closed). The successful result prevents users from making wasted trips to campus on weekends.

**TC16** validated the boundary condition at the exact minute of closing. By checking the status at 5:01 PM, just one minute after the standard closing time, the test confirmed that the system is precise and responsive. This accuracy is crucial for students rushing to submit documents or make inquiries at the end of the day.

Figure 65 captures the Facilities Directory interface during a live test conducted at 6:30 PM on a weekday. The screenshot visually validates the logic tested in Table 7.5. The "Library" card (Academic) correctly displays an "Open" status in Gold text, reflecting its extended hours until 11:00 PM. In contrast, the "Registrar" card (Administrative) directly below it displays a "Closed" status in Red text, confirming that the system successfully recognized the 5:00 PM cutoff time. This clear visual distinction proves that the backend time calculation logic is accurately translating system time into actionable user information.
