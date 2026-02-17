# 5.3 Non-Functional Requirements

Non-functional requirements describe the qualities and constraints of the system rather than the specific features.

- **NFR1: Performance Requirements**
  The system should be able to handle multiple students and staff navigating simultaneously without crashing. Routes should generate within 3–5 seconds on a stable internet connection.

- **NFR2: Security Requirements**
  All users (students, staff, administrators) must log in using a username/email and password or single sign-on (SSO) with university credentials. Passwords must be stored securely (hashed, not in plain text). Only administrators can update the database.

- **NFR3: Usability Requirements**
  The system must have a clean and simple interface that users can understand without training. Navigation should be intuitive with clear menus and search functionality. The system must be mobile-friendly since most users will access it on smartphones.

- **NFR4: Reliability Requirements**
  The system should be available at least 95% of the time during normal use. Saved campus data should never be lost and should remain consistent in the database even after crashes or restarts.

- **NFR5: Scalability**
  The system should support an increasing number of users, buildings, and routes as the campus grows. The database should be expandable to accommodate future updates (e.g., new departments, hostels, or facilities).
