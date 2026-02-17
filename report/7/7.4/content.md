# 7.4 Key Findings

The evaluation of the USIU Campus Navigator highlighted its operational strengths, architectural efficiency, and user usability across its core modules. The rigorous testing process confirmed that the application successfully addresses the navigation challenges faced by the USIU community.

## a) Functionality

The system fully supports its intended use cases, including real-time campus navigation, facility discovery, and dynamic status tracking. Core modules such as the Google Maps integration, local search engine, and "Smart Status" logic are robust and functionally complete. The integration with Android Intents for direct dialing and emailing proved seamless, providing users with immediate access to institutional support without needing complex backend infrastructure.

## b) Performance

System endpoints demonstrated consistent response times and high availability during concurrent access. Performance testing confirmed that the local-first architecture (using SharedPreferences and Gson) efficiently handles data retrieval and storage without the latency associated with cloud-based calls. The map rendering and camera animation logic consistently executed within the 1.2–1.8 second range, well within the acceptable threshold for a responsive mobile application.

## c) Usability and User Satisfaction

Students, faculty, and visitors reported a high degree of ease of use due to the system's intuitive Material Design interface and persistent bottom navigation. The integration of visual elements such as color-coded status indicators (Gold/Red) and facility category icons improved transparency and reduced cognitive load. Users particularly appreciated the offline capability, which ensures navigation remains functional even in areas with poor network coverage.
