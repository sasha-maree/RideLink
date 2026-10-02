# 01 – System Overview

**Document type:** Assignment requirement + Project scope definition  
**Source of truth for:** What we are building, what is out of scope, and assignment constraints  
**Status:** Draft – Pending team review

---

## 1. Project Name

**RideLink** – Backend Microservices for a Ride-Sharing Platform

---

## 2. Project Purpose

RideLink is a university group-assignment backend system that simulates the server-side of a ride-sharing platform. Its purpose is to demonstrate applied knowledge of microservices architecture, RESTful API design, interservice communication, authentication, data ownership, testing, and software engineering practices (SOLID principles, CI, Git workflow) within the context of a controlled academic project.

The system uses **fictional/simulated data**. It is not connected to real passengers, real drivers, real maps, real GPS, or real payment gateways.

---

## 3. Assignment Context

| Item | Detail |
|---|---|
| Module | IT3130 – Application Development |
| Assignment type | Group assignment |
| Group size | 4 members |
| Deliverable | Backend microservices system |
| Assessment method | Technical report, Swagger/Postman demonstration, individual viva |

Each of the four group members primarily owns one microservice.

---

## 4. Scope

### In scope

- Four independently executable backend microservices (Java, Spring Boot).
- RESTful JSON APIs for all externally exercised operations.
- Independent data persistence for each service (MongoDB, strictly no shared database).
- Authentication (users must authenticate before accessing protected resources).
- Role-based authorization (at minimum: `PASSENGER`, `DRIVER`, `ADMIN`).
- End-to-end ride lifecycle: `REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED`.
- Cancellation support where valid.
- Fare estimation and simulated payment processing.
- Interservice communication (at least two meaningful interactions).
- Input validation and consistent error handling.
- Unit tests for every service.
- Integrated successful and negative workflow testing.
- Swagger/OpenAPI documentation for every service.
- A shared Postman collection and environment.
- Git branching strategy, meaningful commits, pull requests, and peer review.
- CI pipeline that builds and tests all four services.
- Architecture diagram and at least one sequence diagram.
- Technical report and README.
- Individual viva demonstration.

### Out of scope

| Item | Reason |
|---|---|
| Web frontend (browser UI) | Not required by assignment |
| Mobile frontend | Not required by assignment |
| Real GPS / live location tracking | Not required; fictional data only |
| Real maps or routing engines | Not required |
| Real payment gateway integration | Not required; payment is simulated |
| Real passenger / driver accounts | Fictional data only |
| Production-grade cloud infrastructure | Out of scope for the assignment |
| Service mesh / distributed tracing | Not required; adds unnecessary complexity |
| API Gateway (unless it provides clear benefit) | Optional only – must not replace a core service |
| Service registry / discovery (unless it provides clear benefit) | Optional only |
| Message broker (unless justified by assignment need) | Optional only |
| Additional core microservices beyond the required four | Prohibited by assignment constraints |

---

## 5. Required Four Services

The assignment mandates exactly these four core business microservices. No more, no fewer.

| # | Service | Primary Responsibility |
|---|---|---|
| 1 | **Account Service** | User identity, authentication, roles, account management |
| 2 | **Driver & Vehicle Service** | Driver profiles, vehicle registration, availability management |
| 3 | **Ride Management Service** | Ride lifecycle, status management, driver assignment |
| 4 | **Fare & Payment Service** | Fare estimation, payment simulation, receipts |

> **Important:** An API Gateway or infrastructure component, if used, does not count as one of the four required services.

---

## 6. Main Users / Roles

| Role | Description |
|---|---|
| `PASSENGER` | A registered user who requests and takes rides |
| `DRIVER` | A registered user who accepts and completes rides |
| `ADMIN` | A privileged user who manages accounts and oversees the system |

> Additional roles may be introduced if justified. See Open Decisions.

---

## 7. Main System Capabilities

| Capability | Description |
|---|---|
| Account registration | Passengers and drivers can register and manage their accounts |
| Authentication | Users log in and receive a token for subsequent requests |
| Driver/vehicle management | Drivers register vehicles and manage their availability |
| Fare estimation | Passengers can estimate the fare before requesting a ride |
| Ride requesting | A passenger can request a ride; the system assigns an available driver |
| Ride lifecycle management | Ride progresses through defined status transitions |
| Simulated payment | Payment is simulated on ride completion |
| Receipt retrieval | Passengers and drivers can retrieve fare and payment receipts |
| Cancellation | Rides can be cancelled where the status allows it |

---

## 8. High-Level End-to-End Flow

```
Passenger registers → Passenger logs in (receives token)
Driver registers → Driver registers vehicle → Driver sets availability → Driver logs in (receives token)

Passenger requests fare estimate (optional)
Passenger requests ride → Ride Management assigns available driver → Ride status: REQUESTED → ASSIGNED
Driver accepts ride → Ride status: ACCEPTED
Driver starts ride → Ride status: IN_PROGRESS
Driver completes ride → Ride status: COMPLETED
Fare & Payment Service calculates final fare and processes simulated payment
Passenger/Driver retrieves receipt
```

---

## 9. Major Functional Requirements

These are **confirmed assignment requirements**, not team decisions.

| ID | Requirement |
|---|---|
| FR-01 | The system must expose four independently executable backend microservices |
| FR-02 | Each service must have independent data persistence |
| FR-03 | No service may directly access another service's database |
| FR-04 | The ride lifecycle must follow: REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED |
| FR-05 | Cancellation must be supported where the ride status permits it |
| FR-06 | There must be at least two meaningful interservice interactions |
| FR-07 | Users must authenticate before accessing protected endpoints |
| FR-08 | Role-based authorization must be enforced |
| FR-09 | Input validation must be applied to all inputs |
| FR-10 | Errors must be handled consistently across all services |
| FR-11 | All services must have meaningful unit tests |
| FR-12 | Swagger/OpenAPI documentation must be provided for every service |
| FR-13 | A shared Postman collection and environment must be provided |
| FR-14 | At least two negative/failure scenarios must be demonstrable |
| FR-15 | A CI pipeline must build and test all four services |

---

## 10. Non-Functional / Engineering Requirements

These are **confirmed assignment requirements**.

| ID | Requirement |
|---|---|
| NFR-01 | SOLID principles must be applied in implementation |
| NFR-02 | Consistent coding conventions must be followed across all services |
| NFR-03 | Git branching strategy, meaningful commits, pull requests, and peer review are required |
| NFR-04 | An architecture diagram must be produced |
| NFR-05 | At least one sequence diagram must be produced |
| NFR-06 | A technical report must be produced |
| NFR-07 | A README must be provided |
| NFR-08 | Each member must be able to demonstrate and explain their service in a viva |

---

## 11. Assignment Constraints

These are hard constraints from the assignment brief.

1. **Exactly four** core business microservices – no more, no fewer.
2. An API Gateway or infrastructure component must **not** replace or count as one of the four.
3. No frontend application of any kind.
4. No real payment gateway, real GPS, or live maps.
5. Optional technologies (Docker, messaging, service discovery) may only be introduced if they provide **clear assignment-related benefit** and do not add unnecessary complexity.
6. Fictional/simulated data only.

---

## 12. Required Negative / Failure Scenarios

At least two must be demonstrable. Suggested candidates (team confirms which two or more):

| # | Scenario |
|---|---|
| N-01 | No available driver – ride cannot be assigned |
| N-02 | Invalid ride status transition (e.g., trying to complete an ASSIGNED ride) |
| N-03 | Unauthorized access – token missing or invalid |
| N-04 | Invalid input – missing required field, wrong format |
| N-05 | Failed simulated payment |
| N-06 | Attempting to register an already-registered account |

---

## 13. Open Decisions

Items that have not yet been resolved by the team. These must be resolved before the relevant implementation phase begins.

| ID | Decision Required | Options | Impact |
|---|---|---|---|
| OD-01 | **[RESOLVED]** Programming language and backend framework | **Java / Spring Boot** | Affects all services |
| OD-02 | **[RESOLVED]** Database technology for each service | **MongoDB** (Spring Data MongoDB) | Affects data layer of all services |
| OD-03 | [TEAM DECISION REQUIRED] JWT library selection | Depends on chosen language/framework | Affects Account Service and auth middleware |
| OD-04 | **[RESOLVED]** Service port assignments | **8081–8084** | Affects local development setup |
| OD-05 | [TEAM DECISION REQUIRED] Whether to introduce Docker / Docker Compose | Yes or No – only if it provides clear benefit | Affects dev environment and CI |
| OD-06 | [TEAM DECISION REQUIRED] Which two (or more) negative scenarios to formally demonstrate | See §12 above | Affects testing plan |
| OD-07 | [TEAM DECISION REQUIRED] Whether an API Gateway will be used | Yes or No – must not replace a core service | Affects external interface |
| OD-08 | [TEAM DECISION REQUIRED] Git branching strategy | Gitflow, trunk-based, feature-branch | Affects team workflow |
| OD-09 | [TEAM DECISION REQUIRED] CI platform | GitHub Actions, GitLab CI, Bitbucket Pipelines, etc. | Affects CI pipeline setup |
| OD-10 | [TEAM DECISION REQUIRED] Database naming conventions | e.g., snake_case, PascalCase | Affects all data layer work |
