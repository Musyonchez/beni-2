# report-lec — Lecturer's Original Report

This folder contains a verbatim Markdown extraction of the lecturer's submitted APT 3065 report.
It was extracted from the original PDF page by page and mirrors the document's structure exactly.

**Do not edit any files in this folder.** It is the reference baseline — used to compare structure,
check wording, and understand what the original submission looked like.

---

## What's inside

Every chapter follows the same layout:

```
<chapter>/
├── _chapter.md       # Chapter title and section list (metadata)
├── content.md        # Full chapter intro text
├── <section>/
│   ├── content.md    # Section content
│   └── <subsection>/
│       └── content.md
```

Front matter lives in dedicated folders at the root level:

```
abstract/
acknowledgement/
cover/
declaration/
table-of-contents/
```

---

## Chapter Overview

| Folder | Content |
|--------|---------|
| `1/` | Introduction — history of navigation systems, problem statement |
| `2/` | Literature Review — MIT WhereIs, NUS NextBus, UCT, UNILAG, UoN, Strathmore |
| `3/` | Aims and Objectives — general aim, 3 specific objectives |
| `4/` | Proposed Project — 3 phases, 13-week Gantt chart, hardware/software requirements |
| `5/` | System Analysis and Design — architecture diagram, use case, ERD, DFDs, flowchart, class diagram, wireframes, FR/NFR |
| `6/` | Implementation — MapFragment, FacilitiesAdapter, Smart Status logic, SharedPreferences + Gson, Google Maps SDK, deployment |
| `7/` | Testing and Evaluation — reliability, performance, scalability, UAT, impact analysis, SDGs |
| `8/` | Conclusion — achievements, challenges, future work |
| `9/` | References — 16 APA sources |

---

## Quick reference

For a condensed summary of this report's content see [../docs/1-report.md](../docs/1-report.md).
