# 4.2.2 Phase Two: System Development / Implementation

## i. System Frontend

The system frontend will be developed as a mobile and web application using React for modularity, responsiveness, and cross-platform compatibility. The frontend will provide students, staff, and visitors with a clean and intuitive user interface that emphasizes simplicity and ease of use. Key features of the frontend include the ability to search for buildings, lecture halls, offices, or facilities, view interactive maps, and generate step-by-step walking routes across campus. The design will follow a structured layout with accessible navigation menus, clear icons, and interactive map features to ensure users can engage with the system effectively without requiring technical expertise. Special attention will be placed on responsive design so that the system functions seamlessly on mobile phones, tablets, and desktops.

## ii. System Backend

The system backend will be implemented using Node.js with Express, providing the core business logic and ensuring smooth communication between the frontend and the database. The backend will handle functions such as user authentication, map data management, search queries, and route generation. It will also provide RESTful APIs to enable real-time communication with the frontend, ensuring that user actions, such as searching for a location or requesting a route, are executed reliably and securely. Scalability will be prioritized in the backend architecture so that the system can support future growth, including potential integrations with shuttle bus schedules, campus event data, or third-party services.

## iii. Database

The database will be developed using PostgreSQL, a relational database management system (RDBMS) suited for handling structured and spatial data. PostgreSQL will manage core entities such as buildings, facilities, classrooms, offices, user accounts, and navigation routes. The database design will follow an entity-relationship model (ERD) to ensure data integrity and minimize redundancy. Since the project involves location data, PostgreSQL's PostGIS extension may be leveraged to handle geospatial queries and mapping functionality. In addition, indexing and query optimization will be implemented to improve performance, particularly during complex searches and when generating shortest-path routes across campus.

## iv. System Integration

The system integration process will ensure that the frontend, backend, and database operate together seamlessly. RESTful APIs will act as the communication bridge, allowing the frontend to retrieve map data, building details, and navigation routes from the backend. Integration testing will be conducted to verify that each system component interacts correctly, especially during critical operations such as searching for a building, displaying its location, and generating a walking path. Continuous integration practices will be applied to ensure that updates and changes to the system do not disrupt its overall functionality, maintaining stability throughout development.

## v. Core Functionality Implementation

This phase will focus on implementing the core functionalities that define the USIU Campus Navigation System. These include interactive campus maps, building search functionality, real-time route generation, and facility details (e.g., offices, lecture halls, and student services). Additional features such as user-friendly map markers, accessibility routes for individuals with mobility needs, and administrative dashboards for updating building or facility information will also be implemented. By prioritizing these features, the project ensures that the Minimum Viable Product (MVP) directly addresses USIU's navigation challenges, making it easier for students, staff, and visitors to orient themselves on campus. This foundation will also allow for future expansions, such as integration with shuttle services, event locations, and emergency safety features.
