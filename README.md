# RideLink

## Overview
RideLink is a microservices-based ride-hailing backend platform developed for IT3130 Group Assignment.

## Architecture
The system consists of four independent Spring Boot microservices:
- **Account Service**: Manages users and authentication.
- **Driver & Vehicle Service**: Manages driver profiles, vehicles, and driver availability.
- **Ride Management Service**: Manages the core ride state machine.
- **Fare & Payment Service**: Handles fare calculation and simulated payments.

## Technology Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.4.x
- **Build Tool**: Maven (Maven Wrapper included)
- **Database**: MongoDB (Independent database per service)
- **Security**: JWT Bearer Authentication (HS256)

## Ports
- Account Service: 8081
- Driver & Vehicle Service: 8082
- Ride Management Service: 8083
- Fare & Payment Service: 8084

## Setup Instructions

### Environment Configuration
1. Copy .env.example to .env.
2. Configure the required environment variables (MongoDB connections, JWT Secret, etc.).

### Running Locally
To start the services independently, navigate to their respective directories in services/ and run:

`ash
cd services/account-service
./mvnw spring-boot:run
`

Repeat for all other services. Each service will run on its designated port and connect to its independent MongoDB instance.

## Development Phase Status
**Current Phase**: Phase 1 Complete (Backend Foundation)
- [x] Service skeletons created.
- [x] Environment-based configuration established.
- [x] Error handling and Validation foundation implemented.
- [x] Security (JWT) foundation prepared.
- [x] Swagger OpenAPI initialized.
