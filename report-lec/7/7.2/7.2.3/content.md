# 7.2.3 Scalability Testing

Scalability testing was conducted to assess the system's capacity to handle growth, specifically the addition of new campus infrastructure, as outlined in NFR5: Scalability Requirements. While the current version of the USIU Campus Navigator includes over 40 facilities, the requirement dictates that the system must accommodate an increasing number of buildings, departments, and routes as the university expands. Therefore, this testing phase focused on the adaptability of the local database architecture and the rendering efficiency of the UI components when handling significantly larger datasets.

The evaluation centered on the FacilitiesAdapter and its underlying RecyclerView implementation. Unlike static layouts (such as ListView or ScrollView) which degrade in performance and consume excessive memory as content is added, the RecyclerView is architected to recycle views, ensuring that memory usage remains constant regardless of whether the list contains 50 or 500 items. Test scenarios involved stress-testing the adapter by verifying that scrolling remains smooth (maintaining 60 frames per second) and that the search filtering logic continues to function instantly without lag, even if the facility list doubles in size. This confirms that the application is future-proof and ready to scale alongside USIU's physical growth.

> [Figure 60: Logic for Binding Data to UI Elements in the Recycler View Adapter]

**Line 49:** The onBindViewHolder method is called every time a new item scrolls onto the screen. It receives a recycled holder object rather than creating a new one. This is the key to scalability: even if the list has 1,000 items, this method reuses the same ~8 view objects already in memory.

**Line 50:** Retrieves the specific Facility data object for the current list position (O(1) operation), ensuring instant data access regardless of list size.

**Lines 52-53:** Binds the data strings to the UI elements. Because the text views are already cached in the holder, there is no expensive layout inflation occurring here, keeping the scroll frame rate high (60fps).

**Lines 56-60:** Executes the conditional logic for the status indicator. Instead of inflating different layouts for "Open" vs "Closed," it simply modifies attributes of the existing text view. This efficiency prevents UI stutter during rapid scrolling.

> [Figure 61: Structure of the ViewHolder Class for Optimized View Lookup]

**Lines 152-153:** The getItemCount method returns the total size of the dataset. By returning facilityList.size(), the app ensures accurate scrolling dimensions whether the list contains 10 items or 10,000 items.

**Lines 156-166:** The FacilityViewHolder static class implements the "View Holder Pattern."
- **Line 160:** The constructor calls super(itemView), holding the reference to the root view layout.
- **Lines 162-164:** It calls findViewById exactly once when the item is first created. These references (tvFacilityName, etc.) are stored in memory. This prevents the system from having to search the XML tree every time the user scrolls a pixel, which is the number one cause of lag in non-scalable apps.

## Test Cases — Table 3: Scalability Testing

| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC07 | Load Large Dataset | 1. Inject 500 dummy facility items into the database. 2. Launch the Facilities Directory. 3. Scroll rapidly from top to bottom. | The list should scroll smoothly (60fps) without stuttering or crashing due to memory overflow. | Scrolling remained fluid; no frame drops observed. | **Passed** |
| TC08 | Search Filter Performance | 1. With 500 items loaded, type "Cafeteria" in the search bar. 2. Measure time to update the list. | The list should update instantly (< 500ms) to show only relevant items. | Search results filtered in approx. 300ms. | **Passed** |
| TC09 | View Recycling Validation | 1. Monitor memory usage in Android Profiler. 2. Scroll through the list continuously for 30 seconds. | Memory usage should remain flat and stable (not increasing linearly with scroll). | Memory graph remained stable at ~45MB. | **Passed** |

The scalability tests were designed to stress-test the application's architecture against future growth scenarios.

**TC07** verified the efficiency of the RecyclerView. By injecting 500 items—far more than the current 40—the test confirmed that the recycling logic prevents the "Out of Memory" errors common in older list implementations. The scrolling remained fluid, proving that the memory footprint does not grow linearly with the dataset size.

**TC08** tested the algorithmic efficiency of the search filter. The near-instant result (300ms) proves that the local filtering logic is optimized enough to handle a much larger database without needing a backend server search. This responsiveness ensures that as the campus grows and more facilities are added, students can still find their destination instantly.

**TC09** provided technical validation of the ViewHolder pattern. The stable memory graph confirms that the app is not leaking memory as it renders new rows, ensuring it remains stable even on older devices with limited RAM. This stability is critical for scalability, as it guarantees that adding new buildings in the future will not degrade the user experience for students with entry-level smartphones.
