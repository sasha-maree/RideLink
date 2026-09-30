# Driver & Vehicle Service

## Responsibility
Driver & Vehicle Service is responsible for managing its designated domain area for the RideLink platform.

## Owner
**Assigned to**: Member 2

## Technical Details
- **Technology**: Java 21, Spring Boot 3.4.x, Maven
- **Port**: 8082
- **Database Boundary**: MongoDB ($(System.Collections.Hashtable.db))

## How to Run Locally
1. Ensure the root .env file is configured.
2. In this service directory, execute:
   `ash
   ./mvnw spring-boot:run
   `

## Required Environment Variables
- PORT_
- MONGODB_URI_
- JWT_SECRET

## Phase 1 Status
- [x] Project Skeleton Created
- [x] Database Isolation Configured
- [x] Global Error Handling Initialized
- [x] Security/JWT Foundation Ready
- [x] Independent Startup Verified
