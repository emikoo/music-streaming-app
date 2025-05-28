# Music Streaming Service - Database Course Project

![Database Schema](schema.svg)

This database models a music streaming analytics platform that focuses on data management, user behavior tracking, and content organization rather than actual music playback functionality. 
The system serves as a comprehensive backend for analyzing music consumption patterns and managing digital music catalogs.


## Entities

**users** : Platform members with secure login credentials, customizable profile settings, and registration timestamps for account lifecycle tracking.

**artists** : Musical performers and content creators featuring biographical details, geographic origin data, and visual branding assets.

**songs** : Digital audio tracks containing comprehensive metadata such as titles, playback duration, cover art, and creator associations.

**playlists** : Organized music collections supporting both community-generated and editorial curation with custom titles, visual covers, and creation monitoring.

**playlist_songs** : Relational mapping structure connecting individual tracks to collections while maintaining chronological addition records.

**plays** : Comprehensive listening event logs documenting user engagement patterns through detailed temporal interaction data.

**follows** : Social connection framework linking users to preferred playlists for personalized content discovery and recommendation systems.



## Scenarios

- Recording new user registrations and profile management including authentication and personalization.
- Tracking listening behavior including song plays, duration, and temporal patterns.
- Managing playlist creation, modification, and social sharing.
- Storing and analyzing user engagement patterns including follows, plays, and content discovery paths.
- Generating analytics on popular content, user preferences, and platform engagement metrics.

## Data Integrity & Performance
Data integrity is maintained through foreign key constraints, unique constraints on usernames and emails, and validation checks for duration and email format. Performance optimization relies on strategic indexing for core operations:

- **Analytics Queries** : Indexes on `plays(song_id)` , `plays(user_id)` , and `plays(played_at)` enable fast aggregation for listening statistics and temporal analysis
- **Social Network Operations** : Indexes on `follows(playlist_id)` and `follows(user_id)` support efficient playlist discovery and user relationship queries
- **Search and Discovery** : Indexes on `users(username)` , `users(email)` , and `songs(artist_id)` accelerate content lookup and user authentication
- **Content Management** : Indexes on `playlists(created_by)` and` playlist_songs(playlist_id)` optimize playlist operations and song collection queries
The database design supports efficient JOIN operations across related tables, with optimized query patterns for analytics workloads including `GROUP BY` aggregations for play counts, user statistics, and content popularity metrics. Constraint validation ensures data quality while strategic indexing maintains sub-second response times for complex analytical queries.

## Technical Implementation

**Backend**: Flask (Python) with PostgreSQL database hosted on Supabase.

**Frontend**: Native Android application built with Kotlin, implementing MVVM architecture with Repository pattern, Retrofit for API communication, and modern Jetpack Compose UI framework.

**Data Generation**: Fake data generation using Faker library.

**API Design**: RESTful endpoints supporting full CRUD operations for all entities, with specialized endpoints for analytics and content discovery optimized for mobile application requirements.
