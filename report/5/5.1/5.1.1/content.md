# 5.1.1 System Architecture Diagram

A system architecture diagram is a visual representation that shows how different components of a system interact and work together. It illustrates the structure, data flow, and relationships between the user interface, application logic, and data storage layers. In the case of the Campus Navigation System, the diagram outlines how users, the mobile application, backend server, and external services like Google Maps connect to deliver location and navigation features efficiently.

> [Figure 12: System Architecture Diagram]

Figure 12 above shows the architecture of the Campus Navigation System follows a three-tier design consisting of the Presentation Layer, Application Layer, and Data Layer. Each layer performs specific roles that collectively enable the system to function efficiently, ensure modularity, and support real-time interactions between users and system services.

## 1. Presentation Layer

This layer represents the user interface and interaction point of the system. It consists of Students, Staff, Visitors, and Administrators, who access the system through a Mobile Application Frontend.

Students, Staff, and Visitors can search for campus facilities, view maps, and get directions to classrooms, offices, or other locations within the institution.

The Administrator manages facility data such as building names, locations, and categories, ensuring the information displayed to users is accurate and up to date.

The frontend communicates user requests (such as searching for a location or requesting directions) to the backend via API calls and receives processed results to display as interactive maps or location details.

This layer ensures that users interact with an intuitive, responsive, and user-friendly interface, providing a seamless navigation experience.

## 2. Application Layer

The Application Layer acts as the intermediary between the user interface and the underlying data resources. It is primarily powered by a Backend Server / API, which handles business logic, data processing, and communication between the frontend and database.

- The backend processes incoming API requests from the mobile application, such as location searches or facility management updates.
- It integrates with external services (like Google Maps API) to process geolocation data, route generation, and map visualization.
- Once data is processed, the backend sends the results back to the frontend for user display.

This layer ensures real-time functionality, secure data exchange, and smooth coordination between system components.

## 3. Data Layer

The Data Layer is responsible for managing and storing all essential information used by the system. It consists of:

- A Campus Database, which stores structured information such as building details, facility locations, user profiles, and navigation data. The backend queries and updates this database whenever users perform searches or administrators modify facility information.
- External Services such as Google Maps API and Firebase, which support advanced mapping, location tracking, authentication, and real-time data synchronization.

This layer ensures that the system operates with reliable data storage, accurate geolocation services, and secure access control.
