# 7.2.1 Reliability & Data Consistency Testing

Reliability testing for the USIU Campus Navigator focused on validating the system's ability to maintain data integrity without a remote server, adhering to NFR4: Reliability Requirements. Since the application operates without user accounts, ensuring that locally saved data—specifically the "Favorite" facilities—persists across sessions is critical for user trust. This testing phase evaluated the stability of the SharedPreferences implementation and the serialization logic handled by the Gson library.

The primary objective was to verify that data writes are committed successfully to the device's internal storage and that data reads retrieve the exact state left by the user. Test scenarios simulated common real-world interruptions, such as force-closing the application or restarting the device, to confirm that the "Favorites" list remained consistent and was not lost. This ensures the application meets the 95% availability and data consistency standard defined in the non-functional requirements.

> [Figure 56: JSON Serialization and Persistence Logic using Gson and SharedPreferences]

**Lines 136–137:** The loadFavorites method attempts to retrieve a string from the private SharedPreferences file using the key "favorite_facilities". If the app is being launched for the first time, this returns null.

**Lines 138–140:** If data exists, the system uses TypeToken to define the complex list structure (ArrayList<Facility>). The gson.fromJson method then parses the stored JSON string back into usable Java objects, restoring the user's data.

**Lines 146–147:** The saveFavorites method captures the current state of the favoriteFacilities list and converts it into a JSON string using gson.toJson.

**Line 148:** The sharedPreferences.edit() method is called to open a transaction. The string is saved, and apply() is used to commit the changes asynchronously to the disk, ensuring the data is safe even if the app crashes immediately after.

## Test Cases — Table 2: Reliability & Data Consistency

| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC01 | Save Favorite Item | 1. Open Facilities Directory. 2. Tap the "Heart" icon on "Library". 3. Icon turns Red. | Item is added to local list and saved to storage. | Icon updated; Write operation confirmed. | **Passed** |
| TC02 | Data Persistence on Restart | 1. Add "Cafeteria" to favorites. 2. Force close the app. 3. Relaunch app. 4. Open Favorites tab. | "Cafeteria" should appear in the Favorites list. | Data persisted; "Cafeteria" was listed. | **Passed** |
| TC03 | Remove Favorite Item | 1. Tap "Heart" icon on an existing favorite. 2. Navigate away and back. | Item should be removed from the list permanently. | Item removed successfully from storage. | **Passed** |

The reliability test cases presented in Table 2 were meticulously designed to validate the robustness of the local data storage system under various conditions. TC01 confirmed the basic functionality of the write operation, verifying that user interactions in the UI correctly trigger the backend logic to update the list in memory. TC02 served as the critical stress test for NFR4. By force-closing the application, the test simulated a crash or a system restart, which clears the device's Random Access Memory (RAM). The successful retrieval of the "Cafeteria" item upon relaunch proved that the Gson serialization and SharedPreferences commit logic function correctly, writing data to the persistent storage (disk) rather than just temporary memory. Finally, TC03 verified that data updates, specifically deletions, are also persistent. This ensures that the database maintains an accurate reflection of the user's current preferences and does not retain stale or unwanted data. Collectively, these results confirm that the system meets the high reliability requirements for data consistency.

> [Figure 57: Favorites fragment UI]

The Favorites fragment populated with facility cards, captured immediately after a simulated application restart, serves as definitive proof that the deserialization logic within the loadFavorites() method correctly retrieved the JSON string from the device's internal storage and reconstructed the Facility java objects without data loss. The presence of facility cards in the RecyclerView, rather than the default "No Favorites Yet" empty state view, confirms that the SharedPreferences read operation was successful. Furthermore, the specific state of the UI elements—such as the filled red "Heart" icon and the accurate retention of facility name and category details—validates that the integrity of the complex object data was maintained through the serialization process.

From a reliability perspective (NFR4), this interface demonstrates the system's ability to handle session interruptions gracefully. The successful rendering of this view confirms that the backend write operation (saveFavorites) committed the data to the disk permanently, rather than holding it in volatile memory (RAM) which would have been cleared upon closing the app. This behavior ensures that students can rely on the application to retain their personalized schedules and preferred locations even if their device reboots or the operating system kills the background process to save battery. The seamless restoration of this state meets the requirement for 95% system availability and consistent data retention, providing a trustworthy and personalized user experience.
