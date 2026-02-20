# 1 — Overview, Architecture & Conventions

## Project name
`CafeteriaApp` (Android package: `com.usiu.cafeteria`)

## Architecture — 3-tier
```
[Android App — Java/XML]
        ↕ (Firestore SDK, FirebaseAuth, FCM)
[Firebase Backend — Firestore + Auth + FCM + Cloud Functions]
        ↕ (Cloud Functions read/write Firestore)
[Scheduled Jobs — Cloud Functions cron (10:00 AM & 5:00 PM EAT)]
```

## Android project structure
```
app/src/main/
├── java/com/usiu/cafeteria/
│   ├── MainActivity.java           — single activity, bottom nav, fragment host
│   ├── auth/
│   │   ├── LoginActivity.java
│   │   └── RegisterActivity.java
│   ├── fragments/                  — student screens
│   │   ├── MenuFragment.java
│   │   ├── CartFragment.java
│   │   ├── OrdersFragment.java
│   │   ├── PreOrdersFragment.java
│   │   └── ProfileWalletFragment.java
│   ├── staff/                      — staff screens (separate activity or role-gated nav)
│   │   ├── StaffOrdersFragment.java
│   │   ├── StaffMenuFragment.java
│   │   └── StaffWalletFragment.java
│   ├── models/                     — plain Java model classes (match Firestore docs)
│   │   ├── User.java
│   │   ├── MenuItem.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── PreOrder.java
│   │   └── WalletTransaction.java
│   ├── viewmodels/
│   │   ├── CartViewModel.java       — shared, survives fragment switch
│   │   ├── WalletViewModel.java     — shared, live wallet balance
│   │   └── OrdersViewModel.java
│   ├── repository/
│   │   └── FirestoreRepository.java — ALL Firestore calls live here
│   └── adapters/
│       ├── MenuAdapter.java
│       ├── CartAdapter.java
│       ├── OrdersAdapter.java
│       ├── PreOrdersAdapter.java
│       └── WalletTransactionAdapter.java
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── activity_login.xml
    │   ├── activity_register.xml
    │   ├── fragment_menu.xml
    │   ├── fragment_cart.xml
    │   ├── fragment_orders.xml
    │   ├── fragment_preorders.xml
    │   ├── fragment_profile_wallet.xml
    │   ├── fragment_staff_orders.xml
    │   ├── fragment_staff_menu.xml
    │   ├── fragment_staff_wallet.xml
    │   ├── item_menu.xml
    │   ├── item_cart.xml
    │   ├── item_order.xml
    │   ├── item_preorder.xml
    │   └── item_wallet_tx.xml
    ├── menu/
    │   ├── bottom_nav_student.xml
    │   └── bottom_nav_staff.xml
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml
    │   └── themes.xml
    └── navigation/                 — (optional) Navigation component graph
```

## Key dependencies (build.gradle :app)
```groovy
// Firebase
implementation platform('com.google.firebase:firebase-bom:33.x.x')
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-messaging'

// Material Design 3
implementation 'com.google.android.material:material:1.12.x'

// Glide (for menu item images)
implementation 'com.github.bumptech.glide:glide:4.x.x'

// ViewModel + LiveData
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.x.x'
implementation 'androidx.lifecycle:lifecycle-livedata:2.x.x'
```

## Coding conventions
- All Firestore calls (reads, writes, listeners) → `FirestoreRepository` only
- Fragments observe `LiveData` from ViewModels — no direct repository calls from fragments
- Fragment transactions use **hide/show** (not replace) so list scroll positions are preserved
- Model classes use empty constructors + public getters/setters (required by Firestore SDK)
- Role check on login: if `users/{uid}.role == "staff"` → show staff bottom nav and staff fragments; else student
- Never call `finish()` after login without starting the correct activity first
- Use `@StringRes` string resources for all user-visible text

## Authentication flow
1. App opens → check `FirebaseAuth.getInstance().getCurrentUser()`
2. If null → `LoginActivity`
3. On successful login → read `users/{uid}` to get role
4. role == "staff" → launch `StaffMainActivity` (or swap bottom nav)
5. role == "student" → launch `MainActivity` with student bottom nav
6. Registration creates user doc in Firestore with role = "student" and walletBalance = 0
