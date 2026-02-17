# 6.6 Application Deployment

The deployment phase of the USIU Campus Navigator was focused on packaging the application into a secure, installable format accessible to students, faculty, and staff. This stage involved configuring the build environment, generating signed binaries, and verifying the installation process on physical devices to ensure smooth system operations. The frontend logic was optimized using ProGuard to shrink code size and obfuscate proprietary logic, while the Gradle build system was configured to manage dependencies such as the Google Maps SDK and Gson serialization library efficiently. Special attention was given to the signing process, which ensures the integrity of the application and prevents unauthorized modifications. Post-deployment, the application was subjected to rigorous real-world testing to verify GPS accuracy and UI responsiveness across different Android versions and screen sizes.

## Deployment Tasks

**1. Gradle Build Configuration:** The project's build.gradle file was meticulously configured to define the application's versioning and dependency management. The minSdkVersion was set to API Level 24 (Android 7.0) to ensure compatibility with the majority of student devices, while the targetSdkVersion was updated to the latest API level to leverage modern security and performance features. Essential dependencies, including com.google.android.gms:play-services-maps and com.google.code.gson:gson, were implemented to support the core mapping and data persistence functionalities.

> [Figure 50: Gradle Build Configuration – Top-Level File]

**Line 1:** A comment explaining that this is the top-level build file, used to configure options common to all sub-projects or modules.

**Lines 2-4:** The plugins block defines plugins for the project. alias(libs.plugins.android.application) apply false registers the Android application plugin but does not apply it here. This allows separate modules to apply the plugin in their own build.gradle files.

**2. APK Signing and Generation:** To prepare the application for distribution, a signed Android Package Kit (APK) was generated using Android Studio's build tools. A secure release keystore was created to digitally sign the APK, a mandatory step that verifies the developer's identity and ensures the app's integrity. This cryptographic signature prevents the installation of tampered versions and allows for seamless future updates without data loss for the user.

> [Figure 51: APK Signing and Generation]

**Lines 1-3:** The plugins block applies the Android application plugin to this module, enabling Android-specific build features.

**Lines 5-7:** The android block sets the project namespace and the compile SDK version, determining which Android API the app is compiled against.

**Lines 9-16:** The defaultConfig block defines essential app properties, including the application ID, minSdk, targetSdk, version code, and version name. It also specifies the test instrumentation runner.

**Lines 18-26:** The buildTypes block defines the release build. isMinifyEnabled = false disables code shrinking, and proguardFiles specifies ProGuard rules to optimize and protect the code. A signing configuration can be added here to digitally sign the APK for distribution.

**Lines 27-30:** compileOptions ensures the app uses Java 11 features and sets compatibility with Java 11 source and target code.

**Lines 32-44:** The dependencies block lists all external libraries required by the app, including Google Maps, location services, Dexter for permissions, Gson for JSON handling, and standard Android libraries.

**3. Physical Device Testing:** The signed APK was side-loaded onto a variety of physical Android devices, ranging from entry-level smartphones to high-resolution tablets. This step verified that hardware-dependent features, specifically the GPS sensors and Compass, functioned correctly in the real-world campus environment. It also confirmed that the Material Design XML layouts adapted responsively to different screen densities and aspect ratios.

> [Figure 52: Physical Device Testing Logic]
> [Figure 53: Map Interface During Physical Device Testing]

**4. Maintenance and Support:** A structured plan for post-deployment maintenance was established to handle future updates, such as changes to facility operating hours or the addition of new campus buildings. The modular nature of the DataManager class allows for quick updates to the facility list in the source code, which can be rolled out as version updates. Additionally, a feedback loop was established via the Profile section's email integration, allowing users to report bugs or suggest features directly to the development team.

> [Figure 54: Maintenance and Support Logic]
> [Figure 55: Profile Section Feedback Interface]
