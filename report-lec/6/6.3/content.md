# 6.3 Backend Implementation

The backend architecture of the USIU Campus Navigator is designed as a "local-first" system, ensuring that the application remains fully functional without an active internet connection. Unlike traditional client-server models that rely on remote APIs and cloud databases, this project utilizes a robust internal logic layer to manage data persistence, facility status calculations, and user preferences directly on the device. This approach significantly reduces latency, eliminates data costs for the user, and ensures that critical navigation information is always available, regardless of network coverage on campus.

The core of the backend implementation relies on Android's SharedPreferences framework combined with the Gson library for object serialization. SharedPreferences provides a lightweight, key-value storage mechanism ideal for saving primitive data types. To handle complex data structures, specifically the list of Facility objects that users mark as "Favorites," the system implements a serialization layer. This layer converts Java objects into JSON strings for storage and deserializes them back into objects for runtime use, effectively creating a local NoSQL-like database within the application's private storage sandbox.

Furthermore, the backend logic encapsulates the "Smart Status" algorithms and navigational intents. Rather than fetching operating hours from a server, the application calculates the Open/Closed status of facilities in real-time using the device's system clock and a set of predefined business rules. This logic is tightly integrated into the data adapters, ensuring that the visual interface reflects the current reality of the campus services instantly.

## i. Data Persistence Implementation (SharedPreferences & Gson)

The Data Persistence module is responsible for retaining user preferences across app sessions. Implemented primarily within the FacilitiesAdapter class, this logic handles the "Favorites" feature. When a user taps the heart icon on a facility card, the system does not merely update the UI; it triggers a transaction that updates a locally stored list. This list is managed using the Google Gson library, which acts as a bridge between the application's memory (Java Objects) and the persistent storage (JSON Strings).

The implementation ensures data integrity by checking for existing records before adding new ones, preventing duplicates. It also handles the retrieval process safely; when the app launches, it attempts to read the stored JSON string. If the string is null (indicating a fresh install), it gracefully initializes an empty list, preventing NullPointerExceptions. This robust handling ensures that the user's curated list of important locations—such as their specific lecture halls or the cafeteria—is preserved even if the phone is restarted or the app is closed.

The loadFavorites() method retrieves the JSON string from SharedPreferences and converts it into a List<Facility> using Gson's fromJson method. Conversely, the saveFavorites() method performs the reverse operation, converting the list into a JSON string using toJson and committing it to storage asynchronously via the apply() method to prevent blocking the main UI thread.

> [Figure 34: Implementation of the RecyclerView Adapter and View Holder Binding Logic]

**Lines 31–37:** The constructor for the FacilitiesAdapter initializes the class. Crucially, it creates a new Gson instance and immediately calls loadFavorites(). This ensures that the user's saved preferences are retrieved from storage the moment the list is created, preventing any delay in showing the correct "favorite" status.

**Lines 41–45:** The onCreateViewHolder method handles the visual creation of list items. It uses the LayoutInflater to parse the item_facility.xml layout file. The parameter attachToRoot: false is specific and necessary for RecyclerViews, as the view checks its own dimensions before being added to the parent layout.

**Lines 48–53:** This block represents the onBindViewHolder method, which connects data to the UI. For every item in the list, it retrieves the specific Facility object and programmatically sets the text for the name, category, and description TextViews, effectively recycling the layout for multiple data entries.

> [Figure 35: JSON Serialization and Persistence Logic using Gson and SharedPreferences]

**Lines 132–134:** The removeFavorites method utilizes Java 8 functional programming. The removeIf function uses a lambda expression to iterate through the list and identify the specific facility by name. Once found and removed, it immediately triggers saveFavorites() to ensure the local storage is updated.

**Lines 137–142:** This method handles Data Deserialization. It retrieves a raw JSON string from SharedPreferences. If data exists, it uses Gson with a TypeToken to convert that string back into a complex ArrayList<Facility> object. The TypeToken is essential here to preserve the list's data type during conversion.

**Lines 147–150:** This method handles Data Serialization. It converts the list of objects into a single JSON string using gson.toJson. It then accesses the SharedPreferences editor and calls .apply() to write the data to the device's storage asynchronously, ensuring the UI thread is not blocked during the save operation.

> [Figure 36: "Favorites" UI]

The "Favorites" UI serves as the visual confirmation of the backend persistence logic. When a user successfully toggles the heart icon in the directory, the facility is added to the internal list and saved. This screen, implemented in FavoritesFragment, retrieves the persisted list and displays it. If the list is empty, a placeholder text ("No Favorites Yet") is shown. This interface allows users to quickly access their most-visited locations without searching through the full directory, demonstrating the practical utility of the local data storage implementation.
