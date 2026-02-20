# Cafeteria Ordering System — Android App

## What this is
An Android (Java + XML) mobile app for ordering food at the USIU-Africa cafeteria.
Students browse the menu, add to cart, place orders, and track them in real-time.
Staff manage incoming orders, update the menu, and top up student wallets.

## Tech stack (non-negotiable)
- **Language:** Java only (no Kotlin)
- **Layouts:** XML (no Compose)
- **Backend:** Firebase — Firestore, Authentication, Cloud Messaging (FCM), Cloud Functions
- **UI:** Material Design 3
- **Build:** Gradle (standard Android Studio project)

## Read these docs first (in order)
1. `docs/1-overview.md` — architecture, project structure, coding conventions
2. `docs/2-screens.md` — every screen, fragment name, what it shows, interactions
3. `docs/3-data-model.md` — all Firestore collections and document schemas
4. `docs/4-features.md` — detailed specs for wallet, pre-order, estimated wait time
5. `docs/5-firebase.md` — Firebase services, Cloud Functions logic, FCM, security rules
6. `docs/6-ui.md` — colours, typography, component guidelines, branding

## Key rules
- One Activity (`MainActivity`) with a `BottomNavigationView` and a `FragmentContainerView`
- Use **hide/show** (not replace) for fragment switching — preserves scroll/state
- All Firestore calls go through `FirestoreRepository` — no direct Firestore calls in fragments
- Wallet deductions use `runTransaction()` — never a plain `set()` or `update()`
- Do not add features beyond what is in the docs
- Do not over-engineer — favour simplicity and working code over abstractions

## USIU branding
- Primary (navy): `#002147`
- Secondary (gold): `#CFB991`
- Apply via `colors.xml` and Material Design 3 theme

