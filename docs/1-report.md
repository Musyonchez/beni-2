# Report Summary — USIU Campus Navigator
**APT 3065 | Applied Computer Technology | USIU-Africa | Supervisor: Prof. Paul Okanda**

---

## What is it?

A native Android app that helps students, staff, and visitors navigate the USIU-Africa campus. It shows an interactive map locked to campus boundaries, a searchable directory of 40+ facilities with real-time open/closed status, a favorites list, and one-tap emergency contacts. Works fully offline.

---

## The Problem

USIU-A has dozens of academic blocks, offices, and service facilities. New students and visitors consistently struggle to find their way around. Google Maps gets you to the gate but stops there — no building names, no services detail, no campus-specific context. Physical signage is static and easily missed. The result: wasted time, missed appointments, and unnecessary frustration.

---

## Literature Review (Ch 2)

Reviewed six existing systems at different scales:

| System | Key Insight |
|--------|-------------|
| MIT WhereIs (USA) | Gold standard — interactive maps, floor plans, web-based. Too complex/costly for a mid-size institution. |
| NUS NextBus (Singapore) | Combines navigation + live shuttle tracking. Great model for dynamic features. |
| UCT Campus Maps (South Africa) | Hybrid digital + physical maps. Shows offline access matters in African contexts. |
| UNILAG Navigator (Nigeria) | Semi-digital, geotagged. Proves even basic digitization adds real value. |
| UoN Wayfinding Maps (Kenya) | Static but well-structured. Good baseline for local context. |
| Strathmore Smart Campus Map (Kenya) | Most advanced locally — integrated into student systems, but no real-time GPS. |

**Gap:** No existing local/regional system offers real-time GPS + offline capability + campus-specific status info. USIU system fills that gap.

---

## Objectives (Ch 3)

1. Survey existing systems to identify strengths and weaknesses.
2. Design and develop a mobile navigation app for USIU-A with real-time guidance and a secure, scalable architecture.
3. Test and evaluate the prototype against the identified problems.

---

## How It Was Built (Ch 4–6)

**3 Phases:** Research & Design → Development → Testing & Documentation (13-week timeline, Sep–Dec 2025)

**Tech Stack:**
- Android (Java + XML), Material Design 3
- Google Maps SDK for Android
- SharedPreferences + Gson (local-first, no backend server needed)
- Android Implicit Intents (telephony, email)

**Key design decisions:**
- **Local-first:** All data lives on the device. No login, no cloud dependency, full offline support.
- **Camera boundary lock:** Map view is restricted to USIU campus coordinates — users can't accidentally scroll away.
- **Smart Status:** Real-time open/closed calculation using the device clock against predefined schedules for each facility type (offices, dining, sports, academic).
- **Fragment hide/show:** Switching tabs preserves map state and scroll position instead of reloading.

**4 main screens:**
1. **Map** — Interactive campus map with search, GPS dot, quick-access carousel for popular spots.
2. **Facilities** — Scrollable list of all campus locations, searchable, with Gold/Red open/closed indicators.
3. **Favorites** — Persisted across sessions via Gson serialization into SharedPreferences.
4. **Profile / Emergency** — One-tap dial to Security, Health Services, and Main Office; email integration.

---

## Testing Results (Ch 7)

All 16 test cases passed.

| Area | Key Result |
|------|-----------|
| Data persistence | Favorites survive force-close and device restart ✓ |
| Map init speed | Renders in **1.8s** (threshold: 3–5s) ✓ |
| Quick access navigation | Camera animates in **1.2s** ✓ |
| Rapid input stress test | Handles interrupts gracefully, no crashes ✓ |
| 500-item list scroll | Smooth at 60fps, no dropped frames ✓ |
| Search filter at scale | Results in **300ms** (threshold: 500ms) ✓ |
| Memory under load | Stable at **~45MB** (flat, not linear) ✓ |
| Smart Status — weekday | Correctly shows "Open" ✓ |
| Smart Status — weekend | Correctly shows "Closed" ✓ |
| Smart Status — boundary (5:01 PM) | Switches instantly to "Closed" ✓ |

---

## Challenges (Ch 8.3)

- **Map state reset on tab switch** → Solved by replacing fragment transactions with hide/show logic.
- **Campus boundary coordinates** → Required multiple iterations of field testing to get SW/NE corners right.
- **Smart Status weekend bug** → Saturday was showing "Open" for offices. Fixed by adding explicit day-of-week checks before hour-of-day checks.
- **GPS permission crash** → Implemented "Manual Mode" fallback when location access is denied.
- **Small screen layout overlap** → Replaced fixed pixel sizes with ConstraintLayout flexible dimensions.

---

## What Was Achieved (Ch 8.2)

- Boundary-locked Google Maps integration, institution-specific
- 40+ facilities with real-time Smart Status
- Fully offline, no user account needed
- Single-tap emergency dialing
- Material Design 3 UI with USIU branding throughout

---

## What's Next (Ch 8.4)

- Indoor navigation (floor-by-floor for Library, Science Center)
- AR "Live View" — directional overlays on camera feed
- Real-time crowd monitoring (anonymous)
- Academic calendar integration — auto-show next lecture building
- Multi-language support and dark theme
- Events layer on map for graduations, orientations, club activities

---

## References (Ch 9)

16 sources — APA format. Key works: Pressman & Maxim (2020), Sommerville (2016), Dennis et al. (2020), Tan & Chong (2023), Firebase docs, Google Maps Platform docs, Android Developers docs.

Full references in [report/9/content.md](../report/9/content.md).
