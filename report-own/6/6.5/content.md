# 6.5 APIs

The USIU Cafeteria Ordering System integrates a set of Firebase APIs, the Supabase Edge Functions HTTP interface, and the Firebase Admin SDK to enable authentication, real-time data synchronisation, push notifications, and server-side account management. Each API plays a distinct role in the system's operation, and together they form the complete communication layer between the two client applications and the cloud backend.

## 1. Firebase Authentication API

The Firebase Authentication API handles user identity across both client applications. In the Android app, LoginActivity uses the signInWithEmailAndPassword method from the FirebaseAuth class to authenticate students and staff. RegisterActivity uses createUserWithEmailAndPassword to create new student accounts, followed by a Firestore write to create the accompanying users document. ChangePasswordActivity uses the updatePassword method on the currently authenticated FirebaseUser object to enforce the mandatory first-login password change for staff accounts.

In the Next.js admin panel, the Firebase Client SDK's signInWithEmailAndPassword is used on the login page. The admin panel's API routes use the Firebase Admin SDK's auth().createUser() method to provision new staff accounts server-side, avoiding the need to expose administrative credentials in the client browser.

> [Figure 38: Firebase Authentication API — RegisterActivity createUserWithEmailAndPassword flow, including Firestore users document creation with role and walletBalance fields]

Figure 38 illustrates the student registration sequence. After Firebase Auth creates the user, the app calls FirestoreRepository to write the users document synchronously before redirecting to MainActivity. This ensures the role field is available immediately on the user's first login.

## 2. Firebase Firestore SDK

The Firebase Firestore SDK is the most extensively used API in the system, underpinning all data reads, writes, and real-time synchronisation in the Android app. The key Firestore SDK methods used are:

addSnapshotListener attaches a persistent, real-time listener to a collection or query result. The listener fires immediately with the current data and then again whenever the underlying documents change. This is the mechanism behind the live order status updates in the student's OrdersFragment and the live order queue in StaffOrdersFragment.

runTransaction executes a server-side atomic operation. All wallet balance modifications — top-ups, order deductions, staff-initiated debits, and pre-order deductions at cut-off — use runTransaction to guarantee that no concurrent operation can produce an inconsistent balance. The transaction reads the current balance, computes the new value, and writes it back in a single atomic step.

set and update write documents and individual fields to Firestore. The repository uses set for new documents (orders, preOrders, walletTransactions) and update for field-level changes such as order status transitions and availability toggles on menu items.

Composite indexes deployed to Firestore enable the compound queries required by the system, such as querying orders by userId and sorting by createdAt in a single operation, or querying preOrders by mealSlot, scheduledDate, and status simultaneously for the cut-off function.

> [Figure 39: Firebase Firestore SDK — addSnapshotListener on the orders collection in FirestoreRepository.listenToStudentOrders(), returning a LiveData-backed real-time order list]

Figure 39 shows the addSnapshotListener call within listenToStudentOrders(). The listener is attached once when the ViewModel initialises and remains active for the lifetime of the student's session. Each QuerySnapshot delivered by Firestore is mapped to a list of Order objects using toObject(Order.class) and posted to the MutableLiveData, which OrdersFragment observes to update its RecyclerView without any manual refresh.

## 3. Firebase Cloud Messaging (FCM) SDK

The Firebase Cloud Messaging SDK handles push notification delivery from the Supabase Edge Functions to student devices. On the Android client side, a FirebaseMessagingService subclass overrides the onNewToken callback to capture the device's FCM registration token whenever it is refreshed. The new token is immediately written to the fcmToken field of the authenticated user's Firestore document, ensuring the edge functions always have access to an up-to-date delivery address.

Push notifications are sent from the Supabase Edge Functions using the Firebase Admin SDK's messaging().send() method, which accepts a notification payload and a target FCM token. Notifications are received by the Android OS and displayed as system notifications even when the app is in the background. When the app is in the foreground, the onMessageReceived callback allows the app to handle the notification programmatically — for example, by refreshing the Orders tab to immediately show the updated status.

> [Figure 40: FCM SDK integration — onNewToken writes fcmToken to Firestore; edge function reads token and sends notification via Firebase Admin messaging().send()]

Figure 40 illustrates the two-phase FCM integration. The onNewToken callback runs automatically whenever Firebase rotates the device's registration token; the immediate Firestore write ensures edge functions always hold a valid delivery address. The notification dispatch path is entirely server-side, keeping the student device's role passive — it only receives notifications, never initiates them.

## 4. Supabase Edge Functions HTTP API

The Supabase Edge Functions are exposed as standard HTTPS endpoints and are consumed via plain HTTP POST requests. There is no client SDK — both the Android app and cron-job.org interact with the functions using HTTP directly.

The Android app's FirestoreRepository.notifyOrderReady() method constructs an HTTP POST request using Java's HttpURLConnection or OkHttp, setting the Content-Type header to application/json and the Authorization header to Bearer followed by the FUNCTIONS_SECRET value retrieved from BuildConfig. The request body is a JSON object containing the orderId and userId. The call is made on a background thread to avoid blocking the main UI thread.

The cron-job.org service sends HTTP POST requests to the process-cutoff endpoint with a JSON body specifying the mealSlot. These requests are authenticated with the same FUNCTIONS_SECRET Bearer token. The edge functions respond with an HTTP 200 status and a JSON summary of the processing result (e.g., number of confirmed and cancelled pre-orders), which cron-job.org logs for monitoring purposes.

> [Figure 41: Supabase Edge Functions HTTP API — Android HTTP POST to notify-order-ready with FUNCTIONS_SECRET Bearer token and JSON payload containing orderId and userId]

Figure 41 shows the HTTP API call from the Android app to the notify-order-ready edge function. The call is dispatched on a background thread immediately after the Firestore status write, using the FUNCTIONS_SECRET stored in BuildConfig to authenticate the request. The response is ignored by the app; if the network call fails, the student's in-app status chip already reflects the updated state through the active Firestore snapshot listener.

## 5. Firebase Admin SDK (Next.js API Routes)

The Firebase Admin SDK is used exclusively in the Next.js admin panel's server-side API routes. Unlike the client-side Firebase SDK, the Admin SDK bypasses Firestore security rules and Firebase Authentication role restrictions, allowing it to create Auth users with any role and read or write any Firestore document. This elevated privilege is why the Admin SDK is restricted to server-side code only; its service account credentials are stored as environment variables in Vercel and are never exposed to the browser.

The create-staff API route initialises the Admin SDK with the service account credentials, calls auth().createUser() to provision the Firebase Authentication account with the specified email and a temporary password, then calls firestore().collection("users").doc(newUid).set() to create the Firestore users document with role: "staff" and firstLogin: true. The route returns the new user's UID to the admin panel client on success, or a structured error message on failure.

> [Figure 42: Firebase Admin SDK in Next.js API route — auth().createUser() followed by Firestore doc creation with role: "staff" and firstLogin: true]

Figure 42 illustrates the staff account creation API route. The server-side execution context ensures that the Admin SDK credentials are never accessible to browser clients, maintaining the security boundary between the admin panel's user interface and its privileged backend operations.

