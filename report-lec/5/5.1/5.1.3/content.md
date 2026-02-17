# 5.1.3 Entity Relationship Diagram (ERD)

## Introduction

An ERD illustrates the logical structure of the database by showing entities, their attributes, and the relationships between them. For the USIU Campus Navigation System, the ERD is essential because the system relies heavily on a database to store and retrieve information about buildings, facilities, users, and navigation data.

This ensures that the system can quickly return accurate results when students, staff, or visitors search for a building, request directions, or access facility details.

> [Figure 14: Entity Relationship Diagram (ERD)]

Figure 14 above shows the Entity-Relationship Diagram (ERD) for the Campus Navigation System, it illustrates the key entities within the system and how they relate to each other. It defines the structure of the database by showing entities such as Users, Facilities, Locations, and Navigation Routes, along with their attributes and connections.

For instance, a *User* (which could be a student, staff, or visitor) can search or access multiple *Facilities* on campus, while each *Facility* is linked to a specific *Location*. The *Routes* entity helps store path data between locations to enable navigation and direction features.

This diagram ensures efficient data organization and supports the interaction between the mobile application and backend database, enabling smooth retrieval, storage, and management of campus-related information.
