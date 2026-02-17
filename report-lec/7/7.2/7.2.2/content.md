# 7.2.2 Performance Testing

Performance testing was conducted to rigorously evaluate the responsiveness of the USIU Campus Navigator, ensuring it aligns with NFR1: Performance Requirements. The primary metric for this evaluation was the system's ability to execute critical navigation commands—specifically map rendering, route calculation, and camera movements—within the stipulated 3–5 second threshold defined in the requirements phase. Since the application relies heavily on the Google Maps SDK for Android, testing focused on measuring the latency between a user's input (such as tapping a facility card) and the completion of the corresponding visual response (the camera zooming to the specific coordinates).

Unlike server-dependent applications, where performance is often dictated by internet bandwidth, the performance of this application is largely dependent on efficient local resource management and the rendering speed of the underlying vector tiles. The tests measured the execution time of the animateCamera method and the initialization speed of the SupportMapFragment. This phase was essential to confirm that the application remains fluid and responsive, preventing "Application Not Responding" (ANR) errors even when the user rapidly switches between different campus locations or zooms in and out of complex building clusters. By validating these metrics, the testing ensured that the system delivers a seamless navigation experience suitable for students moving quickly between classes.

> [Figure 58: Programmatic Camera Animation Triggered by Quick Access Card Interaction]

**Line 108:** Attaches a click listener to the specific UI card element identified by R.id.cardLibrary. This is the starting point for the performance test measurement; the timer starts the moment the user's finger lifts from the screen.

**Line 109:** Instantiates a new LatLng object with the specific hardcoded GPS coordinates for the Library (-1.2138, 36.8792). Efficient memory allocation here is crucial to prevent garbage collection pauses that could cause UI stutter during the animation start.

**Line 110:** Performs a null check on the mMap object. This defensive programming step prevents potential crashes (NullPointerExceptions) if the user taps the card before the map tiles have finished loading.

**Line 111:** Executes the core navigation command: mMap.animateCamera. This method instructs the Google Maps engine to calculate a smooth flight path from the current camera position to the target library coordinates, adjusting the zoom level to 17f. The speed at which this animation completes and the map tiles refresh is the direct measure of the system's performance against NFR1.

## Test Cases — Table 7.2: Performance Testing

| Test Case ID | Test Scenario | Test Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC04 | Map Initialization Speed | 1. Launch the application from the home screen. 2. Measure the time taken from the splash screen until the map tiles are fully rendered. | The Google Map should initialize and render within 3 seconds. | Map rendered in 1.8 seconds on average. | **Passed** |
| TC05 | Quick Access Navigation | 1. Locate the "Library" card in the bottom carousel. 2. Tap the card. 3. Observe the camera animation. | The camera should smoothly animate to the library coordinates in under 2 seconds without lag. | Animation completed in 1.2 seconds; no dropped frames observed. | **Passed** |
| TC06 | Rapid Switching Stress Test | 1. Tap the "Sports" card. 2. Immediately tap the "Cafeteria" card before the first animation finishes. 3. Immediately tap the "Library" card. | The app should handle the input queue gracefully, cancelling previous animations and moving to the final target without crashing. | The system successfully interrupted the previous animations and settled on the "Library" target instantly. | **Passed** |

The performance test cases were specifically designed to benchmark the system against the latency requirements of NFR1. TC04 focused on the "cold start" time, validating that the heavy Google Maps SDK initializes quickly enough to not frustrate users upon launch. The result of 1.8 seconds confirms that the asynchronous map loading in onMapReady is functioning efficiently. TC05 tested the primary interaction loop—navigating to a facility. The 1.2-second response time is well within the 3–5 second acceptable window, ensuring a fluid user experience. Finally, TC06 simulated an impatient user rapidly switching targets. The system's ability to handle these interrupt requests without crashing or freezing confirms the robustness of the animation logic and memory management.

> [Figure 59: Quick access cards]

The screenshot captures the moment immediately following a user interaction with the "Library" Quick Access card. The map camera has successfully centered on the Library building and zoomed to the optimal level of 17.0f, clearly displaying the building's footprint and surrounding pathways. The fluidity of this transition—occurring in approximately 1.2 seconds—visually confirms the system's adherence to the performance requirement (NFR1). The lack of rendering artifacts or blank tiles further validates the efficiency of the map initialization logic.
