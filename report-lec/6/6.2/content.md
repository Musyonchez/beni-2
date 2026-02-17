# 6.2 Frontend Implementation

The frontend architecture of the USIU Campus Navigator was constructed using Java programming language within the Android native development framework, leveraging the full capabilities of the Android SDK to deliver a polished, responsive, and highly intuitive user interface. The application systematically implements Material Design 3 components throughout all interface elements to ensure visual consistency, accessibility compliance, and professional aesthetics that align with modern mobile application standards. The carefully structured interface facilitates effortless navigation through four logically organized main sections via a persistent bottom navigation bar, enabling seamless transitions between real-time interactive mapping, comprehensive facility exploration, personalized favorites management, and critical emergency contact access.

The design philosophy consistently emphasizes usability while maintaining strict adherence to USIU's official brand identity, incorporating the university's signature color palette of dark blue (#002147) and accent gold (#CFB991) throughout all visual elements. This strategic implementation ensures a cohesive and institutionally aligned user experience across all device configurations, from compact mobile phones to larger tablet displays, while maintaining optimal performance and responsiveness.

## i. Main Application Structure and Navigation Implementation

The Map Interface serves as the foundational component of the USIU Campus Navigator, acting as the primary entry point for users seeking spatial orientation. Implemented within the MapFragment class, this interface leverages the Google Maps SDK for Android to render a high-fidelity, vector-based map of the campus. The fragment implements the OnMapReadyCallback interface, ensuring that all map-related logic, such as setting camera angles and enabling location layers, is executed asynchronously only after the Google Play Services have fully rendered the map tiles. This approach prevents the main UI thread from hanging during initialization, resulting in a stutter-free user experience.

To ensure the application remains focused on its primary objective, campus navigation, a strict Camera Boundary Restriction was implemented. Without this constraint, users could accidentally scroll away from the campus into surrounding neighborhoods like Roysambu or Garden Estate, losing their orientation. The implementation defines a specific LatLngBounds object corresponding to the geographical corners of the USIU Africa campus. The camera logic is programmed to reject any scroll or pan gestures that would move the viewport outside these defined coordinates, effectively "locking" the user's view to the relevant educational environment.

A significant portion of the implementation logic is dedicated to Real-Time Facility Status. To solve the problem of students walking to closed facilities, the MapFragment initializes a status tracker upon creation. This logic utilizes the Java Calendar instance to retrieve the current day of the week and hour of the day. It then applies conditional logic—specific to USIU operations—to determine if facility types like "Office," "Restaurant," or "Pool" are currently open. For example, the logic dictates that the pool is only open on weekdays between 7 AM and 5 PM, while academic buildings remain accessible for longer durations.

The interface also integrates Quick Access Navigation through card interaction listeners. Instead of requiring users to manually pinch and zoom to find popular locations, the implementation attaches OnClickListener events to specific UI cards (Library, Sports, Cafeteria). When a user taps a card, the application programmatically triggers a CameraUpdateFactory.newLatLngZoom command. This smoothly animates the camera to the precise latitude and longitude of the selected facility, adjusting the zoom level to 17.0f to provide an optimal view of the building's footprint.

Finally, the MapFragment handles the critical Runtime Permission Logic required for the "My Location" layer. Before enabling the blue dot that represents the user's GPS position, the code checks if ACCESS_FINE_LOCATION has been granted by the user. If the permission is missing, the app requests it via ActivityCompat; if granted, the mMap.setMyLocationEnabled(true) method is called. This ensures the app complies with Android's privacy standards while providing the essential wayfinding feature that allows students to see their position relative to campus landmarks.

> [Figure 23: MapFragment Class Declaration and Geographical Constants]

**Lines 30–32:** Declares essential class-level variables. mMap holds the reference to the visual map, while fusedLocationClient is the entry point for the Google Play Services location APIs, used to retrieve the device's precise GPS coordinates.

**Lines 35–38:** Defines USIU_BOUNDS as a static constant using the LatLngBounds class. This block hardcodes the specific Southwest and Northeast geographical coordinates of the USIU campus, creating the mathematical rectangle used to constrain the user's view.

**Line 41:** Initializes the declaration for facilityStatus. This Map<String, String> data structure is designed to store the real-time operational state (e.g., "Open" or "Closed") of various campus facilities, decoupling the data storage from the UI logic.

> [Figure 24: Fragment Initialization and Real-Time Status Logic]

**Lines 48–62:** The onCreateView method orchestrates the fragment's startup. Rather than cluttering the main lifecycle method, it delegates tasks to helper methods: initializeFacilityStatus() prepares the data, initializeMap() loads the visual component, and setupButtonListeners() attaches click events to the UI.

**Lines 67–71:** The initializeFacilityStatus method begins the real-time logic. It creates a Java.util.Calendar instance to capture the current system time (Day of Week and Hour of Day), which is the foundation for deciding if a facility is currently open.

**Lines 74–75:** Implements the specific business logic for administrative offices. It evaluates a boolean expression checking if the current day is between Monday and Friday (Calendar.MONDAY to Calendar.FRIDAY) and if the current time is within standard working hours (08:00 to 17:00).

> [Figure 25: Asynchronous Map Setup and Runtime Permission Handling]

**Lines 134–136:** The onMapReady callback is triggered asynchronously once the Google Maps service is fully loaded. It initializes the global mMap variable, allowing the rest of the class to interact with the map interface.

**Lines 141–148:** Implements critical Android security logic. Before accessing the GPS, the code uses checkSelfPermission to verify if ACCESS_FINE_LOCATION has been granted. If the permission is missing, it pauses execution to request it from the user via requestPermissions, preventing the app from crashing due to security violations.

**Lines 157–160:** The setupMapRestrictions method acts as a "digital fence." It applies setLatLngBoundsForCameraTarget(USIU_BOUNDS) to strictly limit the scrollable area to the campus. It also sets a MinZoomPreference of 15.0f, which prevents users from zooming out too far and losing context of the campus details.

> [Figure 26: Map Interface showing the Google Map]

The Map Interface is designed as the primary navigation hub, prioritizing immediate spatial orientation and ease of access through a layered UI approach. Dominating the screen is the central map canvas, which renders high-fidelity vector tiles of the USIU campus. This dynamic layer supports standard gesture interactions, allowing users to pinch-to-zoom for granular views of building footprints or rotate the camera to align with their physical orientation. The visual style is customized to highlight campus footpaths and structures in high contrast, while the user's real-time position is tracked via a pulsating blue dot with a directional cone, providing immediate context regarding their location relative to their surroundings.

Situated at the top of the interface is a floating Search Bar, designed with rounded corners and elevation consistent with Material Design 3 card standards. This widget acts as the primary entry point for specific queries; tapping it expands the view to allow text input for building names or facility categories. To the right of the search field, a filter icon provides users with the ability to toggle the visibility of specific marker types, such as "Academic" or "Dining," effectively reducing visual clutter on the map and allowing for a more focused navigation experience.

Anchored at the bottom of the screen is a horizontal "Quick Access" carousel, implemented as a scrollable list of cards overlaying the map view. Each card displays a high-quality thumbnail and the name of a major facility, such as the Library or Sports Complex. This feature addresses the common user challenge of not knowing exactly what to search for upon first launch. Tapping any card in this carousel triggers a programmatic camera update, smoothly flying the viewport to the selected location. Just above this carousel sits the "My Location" Floating Action Button (FAB), which allows users to instantly re-center the map on their GPS coordinates if they have panned away to explore other areas of the campus.

## ii. Facilities Directory Implementation

The Facilities Directory serves as the central information hub of the application, providing users with a comprehensive, searchable list of all key locations within the USIU campus. Unlike the Map Interface, which offers spatial context, the Directory is optimized for quick data retrieval and status checking. Implemented within the FacilitiesFragment, this module utilizes a RecyclerView component to render a scrollable, high-performance list of facility objects. The architecture separates the data layer from the presentation layer using a custom FacilitiesAdapter, which binds raw data, such as building names, categories, and operating hours to the visual layout defined in the XML card files. This separation of concerns ensures that the list can scale efficiently to accommodate hundreds of entries without causing UI lag or memory leaks.

A core innovation within this module is the "Smart Status" logic, designed to address the common frustration of students walking to closed facilities. The adapter does not simply display static operating hours; instead, it performs a real-time calculation every time a card is bound to the view. By capturing the device's current system time and comparing it against the specific operating window of each facility type (e.g., Academic, Dining, or Administration), the system dynamically assigns a status of "Open" or "Closed." This status is visually reinforced through color-coded indicators — Gold for open and Red for closed — allowing users to assess availability at a glance while scrolling, significantly enhancing the utility of the directory.

To further improve usability, the Facilities Directory incorporates a robust search and filtering mechanism. A SearchView widget located at the top of the interface allows users to filter the list in real-time by typing keywords, such as "Library" or "Cafeteria." The implementation logic uses a filtering algorithm within the adapter to instantly update the data set, removing irrelevant items without reloading the entire fragment. This responsiveness is crucial for new students who may know the name of a building but not its category or location. Additionally, tapping on any facility card triggers a navigation intent, linking the directory directly back to the Map Interface to show the selected location.

> [Figure 27: Facilities directory implementation]

**Lines 48-50:** The onBindViewHolder method is the core logic block for the list. It retrieves the Facility object corresponding to the current scroll position from the facilityList.

**Lines 56-61:** Implements the visual logic for the status indicator. If the facility status matches "Open", the text color is set to usiu_status_open (Yellow) with a transparent background. Conversely, if closed, the text color shifts to usiu_warning_red (Red), providing immediate visual feedback to the user.

> [Figure 28: Facilities directory implementation]

**Lines 64-67:** Checks the local persistence layer (SharedPreferences) via the helper method isFacilityFavorite. If the facility is saved as a favorite, the heart icon button color changes to red and the background drawable is updated to button_pill_active, indicating the active state.

**Lines 93-98:** Sets up the "Directions" button listener. This code constructs a Uri with the navigation query for USIU Africa and launches an Intent specifically targeting the Google Maps application (com.google.android.apps.maps), allowing the user to get turn-by-turn navigation.

> [Figure 29: Facilities Directory UI]
> [Figure 30: Facilities Directory UI]

The Facilities Directory UI is designed as a vertical, scrollable list of information cards. Each card serves as a compact summary of a specific campus location, displaying the Facility Name (tvFacilityName) and Category (tvCategory) in bold, legible typography. The status of the building is prominently displayed on the right side of the card; as detailed in the code, this text appears in Yellow when the facility is open and Red when closed, allowing users to scan the list rapidly for available services without opening detailed views.

## iii. Profile and Support UI

The Profile and Support UI is a critical component designed to serve as the application's safety and communication hub. While the Map and Directory fragments focus on exploration, the Profile fragment is engineered for immediate access to institutional support services. Implemented within the ProfileFragment class, this interface aggregates essential contact points—such as Campus Security, Health Services, and the Main Office—into a clean, accessible layout. The logic behind this fragment prioritizes speed and reliability; rather than navigating through complex menus, users are presented with direct-action cards that trigger native system functions immediately upon interaction.

Technically, this module relies heavily on Android's Implicit Intents system to bridge the gap between the application and the device's core telephony and messaging capabilities. The implementation handles OnClickListener events for various UI cards, programmatically constructing Intent objects with specific actions like ACTION_DIAL for phone calls and ACTION_SENDTO for emails. This approach ensures that the application does not need to reinvent communication tools but instead leverages the user's preferred dialer and email clients, ensuring a familiar and reliable user experience.

Additionally, the Profile fragment serves as a secondary navigation anchor. It includes logic to communicate back to the central MainActivity, allowing users to jump directly from the contact screen back to the map view if they need to locate the office they just contacted. Error handling is also integrated into the communication logic; specifically for email intents, the code wraps the start activity call in a try-catch block. This defensive programming ensures that the app remains stable and provides helpful feedback (via Toasts) even if the user's device lacks a configured email client.

> [Figure 31: Implementation of Direct Dial Intents and Internal Navigation Logic]

**Lines 30–36:** The setupClickListeners method initializes the user interaction logic. Specifically, lines 32–36 attach an OnClickListener to the Security card. When tapped, it creates an Implicit Intent with the ACTION_DIAL action. By parsing the telephone string (tel:+254...), the app hands off the operation to the Android system's default dialer, pre-filling the emergency number without requiring the user to type it.

**Lines 53–57:** This block handles internal app navigation rather than external intents. When the "Open Map" button is clicked, the code checks if the hosting activity is an instance of MainActivity. If true, it casts the activity context and calls the public method MapsToMap(). This allows the Fragment to communicate upward to the Activity to switch tabs or fragments programmatically.

> [Figure 32: Configuration of Email Intent with Runtime Exception Handling]

**Lines 61–63:** This section configures the Email button using a highly specific intent action: ACTION_SENDTO. Unlike a generic "share" intent, ACTION_SENDTO combined with the mailto: URI scheme ensures that the Android system only presents email clients (like Gmail or Outlook) to the user, filtering out unrelated apps like SMS or social media.

**Line 64:** The implementation enhances user convenience by adding Intent.EXTRA_SUBJECT. This pre-fills the subject line of the email draft with "USIU Campus Navigator Inquiry," ensuring that inquiries sent by students are immediately recognizable by the recipient.

**Lines 65–69:** Critical error handling is implemented here using a try-catch block. If the user's device does not have an email client installed, the startActivity(intent) call would normally crash the application. This logic catches that specific exception and instead displays a Toast message ("No email app found"), ensuring a stable user experience.

> [Figure 33: The Profile and Support UI]

The Profile and Support UI is visually structured to separate general information from critical emergency services. The top section features the USIU institutional branding and a "Quick Actions" area. Dominating the center of the screen are high-contrast contact cards. The Campus Security card is often styled with a distinct icon or color (such as Red or USIU Blue) to visually differentiate it from standard administrative contacts like the Main Office. This visual hierarchy ensures that in high-stress situations, users can instinctively identify the correct button to press.

Below the emergency section, the interface provides secondary support options. The Email Us button allows for non-urgent administrative inquiries, utilizing the user's default email app for a seamless transition. Additionally, an Open Map button is strategically placed to allow users to quickly pivot from viewing contact information to finding the physical location of the offices on the map. The layout is padded and spaced according to Material Design standards to prevent accidental clicks, ensuring a reliable and frustration-free user experience.
