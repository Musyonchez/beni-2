# 4.4 Gantt Chart

A Gantt chart is a visual project management tool used to plan, schedule, and monitor the progress of tasks within a project. The Gantt chart for the USIU Cafeteria Ordering System outlines every stage of the project lifecycle — from research and system design through development and testing to final submission — across a 13-week timeline from September to December 2025.

> [Figure 11: Gantt chart — USIU Cafeteria Ordering System Project Timeline (Sep 2025 – Dec 2025)]
>
> Phase 1: Research & Design (Weeks 1–4)
> - Week 1: Research & Analysis
> - Week 2: Requirements Gathering
> - Week 3: Proposal Development
> - Week 4: System Architecture & Design
>
> Phase 2: Development (Weeks 5–10)
> - Week 5: Firebase Project Setup
> - Weeks 6–8: Student-Side Fragments (Menu, Cart, Orders, Pre-orders)
> - Week 9: Profile/Wallet and Staff Screens
> - Week 10: Cloud Functions & End-to-End Integration
>
> Phase 3: Testing & Documentation (Weeks 11–13)
> - Weeks 11–12: Testing & Evaluation (20 test cases + UAT)
> - Week 12: Project Write-up
> - Week 13: Final Presentation & Submission

Figure 11 above shows the Gantt chart which also highlights task dependencies and overlaps, providing a visual understanding of how activities are interrelated. This helps with effective project management by identifying critical paths and ensuring that development work does not begin before the design is complete, and that testing does not begin before the full system is integrated.

## Project Phases and Tasks

### 1. Phase One: Research and Requirements Gathering

This initial phase established the foundation of the USIU Cafeteria Ordering System by focusing on research, requirements gathering, proposal development, and system design. Research involved reviewing six existing systems and observing the USIU cafeteria during peak hours. Requirements gathering translated findings into 20 documented test cases and a complete specification of functional and non-functional requirements. Proposal development formalised the project's scope and objectives. System design produced all required diagrams — use case, ERD, DFDs, flowcharts, class diagram, and wireframes — providing a complete blueprint before any code was written. The successful completion of these tasks marked **Milestone 1: Research and System Design Completed**.

### 2. Phase Two: System Implementation

This phase concentrated on building all components of the cafeteria ordering system. Firebase setup established the backend infrastructure before application development began. Student-side fragments delivered the core user experience across Menu, Cart, Orders, and Pre-orders screens. The Profile/Wallet screen and staff screens completed the role-based access model. Cloud Functions integrated the server-side scheduled logic for pre-order cut-offs and push notifications. End-to-end testing confirmed that the full order lifecycle — from student placement to staff update to student notification — functioned correctly. Completion marked **Milestone 2: System Implementation Completed**.

### 3. Phase Three: Testing, Documentation, and Presentation

The final phase validated system reliability and prepared deliverables. All 20 test cases were executed and results documented. UAT with students and cafeteria staff provided real-world feedback. Project documentation covered all development stages, challenges encountered, and how they were resolved. The final presentation demonstrated the system live. This marked **Milestone 3: Testing, Documentation, and Presentation Completed**.

## Dependencies

The project followed a logical flow of dependencies. System design could only begin after requirements gathering was finalised. Firebase setup had to be completed before fragment development. Cloud Functions development required both Firestore collections and fragment code to be in place before end-to-end integration could be verified. Testing depended on the full system being deployed. These dependencies ensured a smooth progression between activities and minimised rework.

## Milestones

Three milestones structured the project's progress. Milestone 1 marked the completion of research and design, providing a validated blueprint. Milestone 2 represented the completion of all system implementation, with every feature functional and integrated. Milestone 3 confirmed the system was fully tested, documented, and ready for submission.
