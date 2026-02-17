# 7.3.1 Real-Time Navigation & Search

This objective ensures users can instantly locate campus facilities through the platform's interface. The navigation feature serves as the primary tool for spatial orientation, allowing users to search for specific buildings by name or category, view their precise location on the interactive map, and access detailed information such as operating hours and descriptions. By integrating a responsive search bar with the underlying Google Maps logic, the system aims to eliminate the confusion often faced by new students when navigating complex campus environments.

UAT focused on verifying that the search mechanism is accessible, functional, and responsive across various Android devices. Testers evaluated the accuracy of the auto-complete suggestions and the speed at which the map camera updated to reflect the selected destination. Additionally, the tests confirmed that tapping a result correctly triggered the associated bottom sheet details, ensuring a seamless transition from search to information retrieval without visual lag or data errors. This rigorous validation ensures that the navigation workflow remains fluid and reliable under real-world usage.

Figure 62 demonstrates how the app's real-time navigation and search functionality is tested using Android's JUnit framework. The test simulates a user clicking the search icon, typing "Library," selecting the top result from the facilities list, and verifying that the correct details appear in the bottom sheet. This ensures that the search and navigation logic works correctly and updates the UI as expected, providing confidence in the system's real-world behavior.

**Line 1:** Annotates the method with @Test, instructing the Android JUnit runner to execute this specific case during the UAT phase.

**Line 4:** Uses the onView matcher to locate the search icon by its ID (R.id.action_search) and performs a click() action to expand the search interface.

**Lines 7–8:** Locates the search input field and simulates user typing by entering the string "Library," then automatically closes the soft keyboard to ensure the screen is not obscured.

**Line 11:** Performs a verification check (matches(isDisplayed())) to ensure that the text "Library" is visible on the screen, confirming that the search filter logic updated the list correctly.

**Lines 14–15:** Targets the facilities list (recycler_view_facilities) and uses RecyclerViewActions to perform a click on the first item (position 0), simulating the user selecting the top search result.

**Lines 18–19:** Validates the final outcome by checking that the Bottom Sheet title (tvSheetTitle) now displays "Library," confirming that the navigation to the selected facility was successful.

## Table 4: User Acceptance Test Cases

| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC11 | Search Functionality | 1. Open the App. 2. Tap the Search Icon. 3. Type "Library". | The list should filter instantly to show the "Library" card. | List filtered correctly; "Library" appeared. | Passed |
| TC12 | Navigation Routing | 1. Tap the "Library" result. 2. Verify map movement. | The Map camera should zoom in and center on the Library coordinates. | Camera successfully animated to the target building. | Passed |
| TC13 | Category Filtering | 1. Type "Food". 2. Observe results. | All dining facilities (Cafeteria) should appear. | System correctly displayed all "Dining" category items. | Passed |

**TC11** confirmed the responsiveness and accuracy of the search functionality. When the user typed "Library" into the search bar, the system instantly filtered the extensive facility list to display the relevant card. The successful execution of this test verifies that the local search algorithm is efficient and provides immediate feedback, eliminating the need for users to manually scroll through long lists to find a specific destination.

**TC12** verified the core "wayfinding" promise of the application. By tapping the search result, the user was successfully oriented to the correct physical location on the map. The test confirmed that the intent triggered by the selection correctly passed the coordinate data to the map camera, centering the view on the target building. This seamless transition from list to map is critical for ensuring users can quickly bridge the gap between identifying a facility and knowing where it is located.

**TC13** tested the flexibility of the search logic, specifically regarding category filtering. The system successfully displayed all dining-related facilities (e.g., "Cafeteria," "Pizza Inn") when the generic term "Food" was entered. This result confirms that the search mechanism is robust enough to handle categorical queries, ensuring that users can discover groups of facilities even if they do not know the specific name of a building.

Figure 63 captures the system's response immediately after a user searches for "Library" and selects the result. The screenshot demonstrates two critical outcomes: first, the search bar successfully filtered the facility list to show the correct target, and second, the map camera automatically updated to center on the Library building, displayed at an optimal zoom level. The presence of the bottom sheet displaying "Library" confirms that the navigation intent was successfully executed, providing the user with immediate context and access to further details. This visual evidence validates the effectiveness of the real-time navigation feature in guiding users to their desired destinations.
