# 6.5 APIs

The USIU Campus Navigator integrates a set of essential Android and Google APIs to support seamless interaction between its frontend logic, local data, and device hardware. These APIs play a critical role in enabling location awareness, interactive mapping, and direct communication channels. The effective integration of these APIs ensures real-time system responsiveness, accurate geospatial rendering, and smooth end-user experiences across different tasks such as wayfinding and emergency dialing.

## Key APIs Used in the USIU Campus Navigator

**1. Google Maps SDK for Android:** Powers the core navigation interface. It renders the vector-based map tiles, handles camera movements (pan, zoom, tilt), and manages the coordinate system. It allows the application to restrict the view strictly to the USIU campus boundaries, preventing users from getting lost in the surrounding map data.

> [Figure 43: Google Maps SDK and Location Services Integration]

**Lines 74–78:** Initializes the GoogleMap instance (mMap) and sets a POI click listener (setOnPoiClickListener). This allows the fragment to detect taps on campus buildings and display detailed information in a bottom sheet.

**Lines 81–83:** Restricts the camera to the USIU_BOUNDS and sets minimum and maximum zoom preferences. This prevents users from navigating outside the campus area while still allowing close zoom for details.

**Lines 87–93:** Enables camera gestures including pan, zoom, tilt, and rotate while disabling zoom controls. This ensures smooth, interactive map manipulation while keeping the UI clean.

**Lines 96–111:** Calls enableMyLocation(), which uses Android Location Services to check permissions and display the blue "My Location" dot. If granted, checkUserLocation() retrieves the device's current latitude and longitude and animates the map to the user's position if it lies within campus bounds.

**Lines 115–130:** Implements the FusedLocationProviderClient task to get the last known location. This allows the map to reflect real-time device positioning without continuously polling the GPS, improving performance and battery usage.

> [Figure 44: Map APIs User Interface]

**2. Android Location Services API:** Facilitates real-time geospatial positioning. This API communicates with the device's GPS hardware to retrieve the user's current latitude and longitude. It enables the "My Location" blue dot feature, allowing students to see their exact position relative to campus buildings.

> [Figure 45: Android Location Services API Logic]

**Lines 102–111:** enableMyLocation() checks if the app has ACCESS_FINE_LOCATION permission. If granted, it enables the blue dot and My Location button on the map. If not, it requests permission from the user.

**Lines 114–133:** checkUserLocation() uses the FusedLocationProviderClient to get the last known location of the device. If a valid location is retrieved and it is within USIU_BOUNDS, the map camera animates to the user's position with zoom level 18. This provides instant feedback of the user's position relative to campus landmarks.

**Lines 135–141:** onRequestPermissionsResult() handles the permission callback. If the user grants location access, it calls enableMyLocation() to turn on the blue dot feature. This ensures a smooth permission workflow without crashing the app.

**Lines 60–67 (Map Setup):** Once the GoogleMap instance is ready, onMapReady() calls enableMyLocation() to immediately show the user's position when the map loads.

> [Figure 46: Location Services Interface]

**3. Android Telephony (Intent) API:** Handles automated dialing requests sent by users when contacting Campus Security or Health Services. This ensures clear and immediate access to support by passing specific telephone numbers (tel:) directly to the system's native dialer.

> [Figure 47: Implementation of Implicit Intents for Direct Dialing Functionality]

**Line 34:** Attaches an OnClickListener to the UI card with ID cardSecurity. This lambda function waits for the user's touch interaction.

**Line 35:** Instantiates a new Intent object with the action Intent.ACTION_DIAL. This specific action tells the Android system that the user wants to open the phone keypad, rather than placing the call immediately (which would require higher permissions).

**Line 36:** Sets the data for the intent using Uri.parse. The string "tel:+254730116111" is parsed into a URI that the phone dialer understands as a valid telephone number.

**Line 37:** Executes startActivity(intent), which pauses the current app and launches the device's default Phone app with the USIU Security number pre-filled and ready to call.

> [Figure 48: Emergency contacts UI]
> [Figure 49: Telephony API integration UI]
