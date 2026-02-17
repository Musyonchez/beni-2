# 6.4 Integration

The integration phase of the USIU Campus Navigator aimed at establishing seamless connectivity between the various Android components—Activities, Fragments, and the local Data Manager. The objective was to ensure smooth navigation flows and instant data synchronization across the user interface, allowing students and faculty to switch between the Map, Facilities Directory, and Profile screens without losing state or experiencing lag. This phase was crucial in validating key system workflows such as dynamic marker placement, smart status updates, and external communication via system intents. Through consistent testing and configuration, the system achieved an efficient, interactive, and reliable native user experience.

## Essential Integration Tasks

**1. Fragment and Activity Integration** — Integrated the MapFragment, FacilitiesFragment, and ProfileFragment within the central MainActivity using the Android Navigation component. This connection ensured that the bottom navigation bar acted as a persistent controller, allowing users to switch contexts instantly. Unlike web pages that reload, this integration utilized Fragment transactions (hide/show) to preserve the map's zoom level and the directory's scroll position when switching tabs.

> [Figure 37: Fragment Transaction and Navigation Logic]

**Lines 37:** Initializes the BottomNavigationView by finding the view ID defined in the layout XML. This establishes the link between the backend logic and the visible navigation bar.

**Lines 39-58:** Implements the setOnItemSelectedListener. This critical block listens for user taps on the bottom bar icons. An if-else structure checks the unique ID of the clicked item (e.g., R.id.navigation_map) and instantiates the corresponding Fragment class. This logic acts as the central router for the application.

**Lines 53-55:** Checks if a valid fragment was selected and calls the helper method loadFragment(). This ensures that the UI only updates if a valid navigation target is identified, preventing potential null pointer exceptions.

> [Figure 38: Main Navigation Interface]

The main navigation interface highlights the persistent bottom navigation bar. This component serves as the central navigation hub, allowing users to switch contexts seamlessly between the Map, Facilities, Favorites, and Profile screens without losing their current state. The active tab is visually distinguished by the USIU Gold accent color, providing immediate feedback on the user's location within the application hierarchy.

**2. Map and Local Data Integration** — Connected the GoogleMap instance in the MapFragment directly to the DataManager class. Instead of hardcoding markers, the system iterates through the centralized list of Facility objects to dynamically populate the map. This ensures that if a facility's details (like name or coordinates) are updated in the data model, the visual marker on the map reflects this change immediately upon the next app launch.

> [Figure 39: Map Initialization and Local Data Binding]

**Lines 74–78:** Initializes the GoogleMap object and sets the POI click listener. These lines store the GoogleMap reference in mMap and register this as the OnPoiClickListener. This lets the fragment react when the user taps a campus building or POI.

**Lines 81–83:** Restricts camera to campus bounds and sets zoom limits. These lines call setLatLngBoundsForCameraTarget(USIU_BOUNDS) and set setMinZoomPreference and setMaxZoomPreference. The result keeps the camera focused on USIU and prevents zooming out or in beyond the intended view.

**Lines 123–139:** Search handling — load data, find match, move camera, show details. This block shows performSearch(...) scanning CampusData.getFacilities() for a name match. When a match is found the code animates the camera to the facility's coordinates and opens FacilityDetailsBottomSheet with that facility's name. If nothing matches the app shows a toast. This connects the map UI directly to the local data source so updates in CampusData reflect on the map.

> [Figure 40: Map and Local Data Integration Interface]

**3. System Intent Integration** — Configured Android Implicit Intents to integrate the app with the device's core capabilities. Specifically, the ACTION_DIAL intent was integrated into the Profile section to bridge the app with the Phone Dialer for security calls, and ACTION_SENDTO was linked to the Email client for administrative inquiries. This ensured timely communication between students and university services without requiring a backend messaging server.

> [Figure 41: System Intent Integration Logic]

**Lines 22–25:** Inflates the Profile screen layout and calls setupClickListeners(). This prepares the UI elements so each card can trigger the correct system action when tapped.

**Lines 31–38:** Handles the Campus Security card. Tapping this card launches the phone dialer using Intent.ACTION_DIAL and loads the security office number (+254730116111) into the dialer. This lets students start a call without typing the number.

**Lines 40–47:** Handles the Health Services card. The same dial intent pattern is used here, but with the Health Services number (+254730116080). This ensures quick access to medical support.

**Lines 49–56:** Handles the Main Office card. A dial intent opens the phone app with the main office number (+254730116000), providing a simple way to reach administrative staff.

**Lines 58–64:** Connects the "Open Map" button to the Map tab by calling navigateToMap() in MainActivity. This keeps navigation consistent by using the same fragment-based flow as the rest of the app.

**Lines 66–78:** Implements the email integration using Intent.ACTION_SENDTO with a mailto: URI. Tapping the Email button opens the device's mail app with the address info@usiu.ac.ke and a pre-filled subject line. A try–catch block handles cases where no email app is installed, showing a toast message instead of crashing.

> [Figure 42: System Intent Integration Interface]
