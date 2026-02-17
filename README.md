# APT 3065 — USIU Campus Navigator

Final year project for **APT 3065** at United States International University Africa (USIU-Africa),
School of Science and Technology. Supervised by **Prof. Paul Okanda**.

The project is a native Android application that solves a real campus problem: students and visitors
at USIU-A have no dedicated tool to navigate the campus, locate facilities, or check operating hours.
The app provides an interactive boundary-locked map, a searchable facilities directory with real-time
open/closed status, a persistent favourites list, and one-tap emergency contacts — all working fully
offline.

---

## Repository Structure

```
beni-2/
├── report-lec/         # Lecturer's original report (read-only reference)
├── report-own/         # Our report — being written from scratch
├── docs/               # Quick-read summaries and supporting documents
├── pdf-source/         # Original PDF extracts from the lecturer's report
└── conversations/      # Session transcripts (Claude Code working sessions)
```

### Report folders

Both report folders share the same chapter structure mirrored from the original PDF.
Each chapter lives in a numbered folder (`1/`, `2/`, ...) and each section in a
sub-folder (`1.1/`, `1.2/`, ...). Content lives in `content.md` files.

| Folder | Purpose |
|--------|---------|
| `report-lec/` | Verbatim extraction of the lecturer's submitted report — source of truth for structure, tone, and content |
| `report-own/` | Our rewritten version — same structure, empty `content.md` files ready to fill |

---

## The Application

| Area | Detail |
|------|--------|
| Platform | Android (Java + XML) |
| Maps | Google Maps SDK — boundary-locked to USIU campus |
| Storage | SharedPreferences + Gson — local-first, no login, no cloud |
| UI | Material Design 3 with USIU branding (navy #002147, gold #CFB991) |
| Screens | Map, Facilities Directory, Favourites, Profile / Emergency Contacts |

**Key features:**
- Camera locked to USIU campus boundaries — users cannot scroll away
- Smart Status — real-time open/closed per facility using the device clock
- Favourites persisted across sessions via Gson serialisation
- One-tap dial to Campus Security (+254730116111) and Health Services (+254730116080)
- Fully offline — no internet required after install

---

## Chapters

| # | Chapter |
|---|---------|
| 1 | Introduction |
| 2 | Literature Review |
| 3 | Aims and Objectives |
| 4 | Proposed Project |
| 5 | System Analysis and Design |
| 6 | Implementation |
| 7 | Testing and Evaluation |
| 8 | Conclusion |
| 9 | References |

For a fast overview of the full report see [docs/1-report.md](docs/1-report.md).
