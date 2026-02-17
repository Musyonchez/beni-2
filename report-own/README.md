# report-own — Our Report

This is where the actual report gets written. The folder tree mirrors `report-lec/` exactly —
same chapters, same sections, same file names — but all `content.md` files start empty.

The goal is to write each section in our own words, informed by the lecturer's version in
`report-lec/` but not copied from it.

---

## How to work in here

Each section has two files:

| File | Purpose |
|------|---------|
| `_chapter.md` | Structure reference — chapter title, section list. Don't touch. |
| `content.md` | Where the actual writing goes. Fill this in. |

Work section by section. Open the matching file in `report-lec/` to understand what the section
covers, then write your own version into `content.md` here.

---

## Folder structure

```
report-own/
├── cover/
├── declaration/
├── acknowledgement/
├── abstract/
├── table-of-contents/
├── 1/                  # Chapter 1: Introduction
│   ├── _chapter.md
│   ├── content.md
│   ├── 1.1/            # 1.1 History
│   ├── 1.2/            # 1.2 Problem Statement
│   └── 1.3/            # 1.3 Conclusion
├── 2/                  # Chapter 2: Literature Review
├── 3/                  # Chapter 3: Aims and Objectives
├── 4/                  # Chapter 4: Proposed Project
├── 5/                  # Chapter 5: System Analysis and Design
├── 6/                  # Chapter 6: Implementation
├── 7/                  # Chapter 7: Testing and Evaluation
├── 8/                  # Chapter 8: Conclusion
└── 9/                  # Chapter 9: References
```

---

## Progress

Track which sections are done by checking if their `content.md` has content.

- Fill in `content.md` → commit with `chore(ch<n>): ...`
- Use `report-lec/` as reference, `docs/1-report.md` for a fast summary
