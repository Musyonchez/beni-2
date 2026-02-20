# 4.2.1 Phase 1: Research, Requirements Gathering and System Design

## i. Research

The first activity in this phase is research, which lays the foundation for the USIU Cafeteria Ordering System. It involves reviewing both global and regional cafeteria and food ordering platforms to identify best practices, strengths, and existing limitations. Global systems such as the MIT Dining App and NUS uNivUS provide insights into mobile ordering, digital wallets, and real-time order tracking. Regional systems such as UCT Hungry Rhino and the UNILAG Cafeteria reveal the challenges of limited interactivity and the absence of mobile-first approaches in the African university context. Primary research will also be conducted through observations and informal interviews with USIU students and cafeteria staff to capture the specific operational challenges on campus — peak-hour queue lengths, payment friction, and the absence of advance ordering.

## ii. Requirements Gathering

The second activity is requirements gathering, which transforms user needs into actionable system specifications. Functional requirements include browsing and searching the menu by category, placing orders with wallet or cash payment, tracking order status in real time, scheduling pre-orders with recurring options, automated wallet deduction at cut-off times, staff order management, and wallet top-up. Non-functional requirements will focus on real-time synchronisation speed, wallet transaction atomicity, Cloud Function execution timing, security rules for user data isolation, and performance under concurrent load. These requirements will be documented to serve as a guide for development and a benchmark for testing.

## iii. Proposal

The third activity is the preparation of the project proposal, which consolidates the research findings and requirements into a structured plan. The proposal clearly defines the system objectives, scope, methodology, and expected outcomes. It also justifies the project by highlighting the gaps in existing solutions — particularly the complete absence of mobile ordering, pre-scheduling, and cashless wallet functionality in Kenyan university cafeterias — and presents how the proposed system will address these gaps comprehensively.

## iv. System Architecture and Design

The final activity in this phase is system architecture and design, which defines the structural and logical blueprint of the USIU Cafeteria Ordering System. This includes creating use case diagrams to represent student and staff interactions, data flow diagrams (DFDs) to illustrate the flow of orders and wallet transactions, entity-relationship diagrams (ERDs) to design the Firestore collections structure, flowcharts for both the regular order and pre-order cut-off flows, and wireframes for all five main screens. The architecture will specify the three-tier structure: Android client, Firebase SDK layer, and Firebase Firestore backend, with Firebase Cloud Functions handling scheduled cut-off jobs.
