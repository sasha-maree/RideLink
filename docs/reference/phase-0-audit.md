# RideLink – Phase 0 Audit

## 1. Audit Objective
Finalize and consistency-check the project's architectural reference documents and authoritative OpenAPI contracts so that the project is ready for implementation, updating confirmed technology stack decisions and identifying remaining blockers.

## 2. Confirmed Technology Stack

| Decision | Final Value |
|---|---|
| Language | Java |
| Framework | Spring Boot |
| Database | MongoDB |
| API Style | REST/JSON |
| API Version | /api/v1 |
| ID Format | UUID |
| Account Port | 8081 |
| Driver/Vehicle Port | 8082 |
| Ride Port | 8083 |
| Fare/Payment Port | 8084 |

## 3. Resolved Decisions
- **OD-01**: Language and framework confirmed as Java and Spring Boot.
- **OD-02 (and OD-A)**: Database technology confirmed as MongoDB. Each service must maintain its own independent persistence boundary (using Spring Data MongoDB).
- **OD-04**: Local development ports confirmed (Account: 8081, Driver: 8082, Ride: 8083, Fare: 8084).
- **OD-B**: ID format confirmed as UUID across all externally exchanged identifiers.
- **OS-01**: Token type confirmed as JWT for authentication (stateless validation by non-account services).
- **OS-02**: JWT signing algorithm confirmed as HS256; Secret management via `JWT_SECRET` environment variable.
- **OS-04**: Logout is stateless (no token blacklist maintained).
- **OS-05**: Internal service-to-service calls will forward the original user's JWT.
- **OA-04 / OB-01 / OP-05**: Member-to-service assignment confirmed (Member 1: Account, Member 2: Driver, Member 3: Ride, Member 4: Fare).
- **OW-01**: Driver selection algorithm is FIRST AVAILABLE DRIVER.
- **OW-02 / OB-04**: If an assigned driver cancels, the ride becomes CANCELLED (no automatic reassignment).
- **OW-03 / OB-03**: Driver rating update is NOT part of Phase 1 implementation.
- **OW-04 / OB-02**: Fare calculation is triggered synchronously via REST call from Ride Management Service.
- **OW-05**: Fare formula parameter values set to LKR (baseFare: 300.00, ratePerKm: 100.00).
- **OD-C**: Receipt entity stores a snapshot of passenger and driver names.
- **OW-06**: Selected negative scenarios are N-02 (Invalid ride status transition) and N-05 (Simulated payment failure).
- **OD-E**: Naming convention for database documents and fields is `camelCase`.

## 4. Remaining Team Decisions (Non-Blocking)
| ID | Decision | Affected Services / Docs |
|---|---|---|
| OS-03 | Token expiry duration | Account Service, Auth |
| OS-07 | Password hashing library (bcrypt, argon2, etc.) | Account Service |
| OS-08 | Whether HTTPS is required for local development | Dev Env |
| OA-02 | Whether to use an API Gateway | Deployment |
| OA-03 | Whether to containerize with Docker | Deployment |
| OP-03 | CI platform selection | DevOps |
| OP-04 | Git branching strategy | DevOps |
| OP-06 | Whether Docker/Docker Compose will be used for local dev | Dev Env |

*(All critical blockers for implementation logic and interservice communication are now resolved.)*

## 5. OpenAPI Contract Audit

| Contract | Status | Issues |
|---|---|---|
| account-service.yaml | ✅ Consistent | Resolved ports, JWT token type, and internal auth. |
| driver-vehicle-service.yaml | ✅ Consistent | Resolved ports, JWT token type, and internal auth. |
| ride-service.yaml | ✅ Consistent | Resolved ports, JWT token type, internal auth, and fare trigger mechanism. |
| fare-payment-service.yaml | ✅ Consistent | Resolved ports, JWT token type, internal auth, and fare formula configuration parameters (LKR). |

## 6. Cross-Document Contradictions Corrected
- *Conflict*: `04-api-contracts.md` contained duplicated endpoint definitions from YAML files.
  - *Correction*: Rewritten to act strictly as a conventions/rules guide, delegating exact endpoints to `contracts/*.yaml` to preserve single source of truth.
- *Conflict*: Several paths in `02-architecture.md` and `03-service-boundaries.md` omitted the `/api/v1/` prefix.
  - *Correction*: All documented API paths updated to include `/api/v1/` prefix to match the contracts.
- *Conflict*: Older fare formula examples used ZAR.
  - *Correction*: Updated to reflect the LKR configuration decision across documents and contracts.

## 7. Security Audit
- **Authentication**: JWT Bearer token confirmed. Account Service acts as the issuer.
- **Authorization**: Role-based (PASSENGER, DRIVER, ADMIN) confirmed. Handled at each endpoint.
- **JWT Validation**: Services will validate tokens independently using the shared `JWT_SECRET` environment variable and HS256 algorithm.
- **Internal Service Authentication**: Internal endpoints will validate the original user's JWT forwarded by the upstream service.
- **Logout**: Handled statelessly (no token blacklist).
- **Secrets**: Must not be hardcoded in source control. Must use environment variables.

## 8. Data Ownership Audit
- Each service owns its own MongoDB persistence boundary.
- Cross-service database access is strictly forbidden.
- UUID is consistently used as the primary identifier format for external cross-service references. No internal ObjectIds should leak if UUID is used in the contracts.
- Stable IDs (`accountId`, `rideId`, etc.) are exchanged correctly via API calls.

## 9. Business Workflow Audit
- The core happy path (Passenger request -> Driver accept -> Ride start -> Ride complete -> Fare calculated -> Payment simulated -> Receipt retrieved) is fully documented and supported by the API contracts.
- Fare calculation happens synchronously from Ride Management Service to Fare & Payment Service.
- Driver selection happens via FIRST AVAILABLE DRIVER logic.
- Negative scenarios formally selected: N-02 (Invalid ride status transition) and N-05 (Simulated payment failure).

## 10. Phase 0 Readiness

**READY FOR PHASE 1**

*Explanation*: The technology stack, interservice communication rules, database ownership, authentication, and core business workflow rules have been fully finalized. Accountability is assigned via Member mappings. The OpenAPI contracts accurately reflect the resolved architecture. Phase 1 backend implementation can safely begin.
