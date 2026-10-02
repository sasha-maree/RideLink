# 03 – Service Boundaries

**Document type:** Service responsibility definition  
**Source of truth for:** What each service owns, what each service must NOT do  
**Status:** Draft – Pending team review

> **API contracts are in [`contracts/`](../../contracts/).** This document defines service responsibilities and boundary rules. The exact endpoint schemas, status codes, and validation rules are in the OpenAPI YAML files.

---

## Purpose

This document defines the exact boundary of each of the four microservices.

It exists to prevent:
- Functionality placed in the wrong service.
- One service reaching into another service's data.
- Overlapping responsibilities between services.

When in doubt about where a feature belongs, this document is the reference.

---

## General Rules (All Services)

1. A service is responsible for **all** business logic related to its domain.
2. A service must **never** directly read from or write to another service's database.
3. A service may **only** obtain another service's data by calling that service's API.
4. A service stores only **foreign IDs** to reference entities owned by other services.
5. A service validates all inputs it receives.
6. A service issues consistent, structured error responses.

---

## Service 1 – Account Service

### Purpose

The Account Service is the **identity and authentication authority** of the system. It is the only service that manages user credentials and issues authentication tokens.

### Responsibilities

- User registration (passenger and driver accounts).
- Credential storage and verification (hashed passwords).
- Login and token issuance.
- Logout / token invalidation (if stateful session tracking is implemented).
- Account profile management (name, email, phone number).
- Account status management (ACTIVE, SUSPENDED, DEACTIVATED).
- Role assignment and enforcement at the identity layer (`PASSENGER`, `DRIVER`, `ADMIN`).
- Admin operations related to accounts (view all accounts, suspend an account).

### NOT Responsible For

| Responsibility | Belongs To |
|---|---|
| Driver profile (bio, rating) | Driver & Vehicle Service |
| Vehicle registration | Driver & Vehicle Service |
| Driver availability | Driver & Vehicle Service |
| Ride creation or management | Ride Management Service |
| Fare calculation | Fare & Payment Service |
| Payment processing | Fare & Payment Service |
| Receipts | Fare & Payment Service |

### Main Entities (Owned)

| Entity | Key Fields |
|---|---|
| `Account` | accountId, email, passwordHash, role, status, createdAt |
| `Profile` | accountId, firstName, lastName, phoneNumber, updatedAt |

### Owned Data

- All account and profile records.
- Credentials (hashed password only – plaintext never stored).
- Roles.
- Account status.

### External Dependencies

None. The Account Service has no dependencies on other RideLink services.

### APIs Exposed

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Register a new account | Public |
| POST | `/api/v1/auth/login` | Login and receive token | Public |
| POST | `/api/v1/auth/logout` | Invalidate token | Authenticated |
| GET | `/api/v1/accounts/me` | Get own profile | Authenticated |
| PATCH | `/api/v1/accounts/me` | Update own profile | Authenticated |
| GET | `/api/v1/accounts/{accountId}` | Get account by ID | Internal / Admin |
| GET | `/api/v1/accounts` | List all accounts | Admin |
| PATCH | `/api/v1/accounts/{accountId}/status` | Suspend or deactivate account | Admin |

> **Authoritative contract:** [`contracts/account-service.yaml`](../../contracts/account-service.yaml)

### APIs Consumed

None. The Account Service does not call other RideLink services.

### Important Business Rules

1. Email must be unique across all accounts.
2. Passwords must be stored hashed – never in plaintext.
3. Only `ADMIN` may list all accounts or change account status.
4. A `SUSPENDED` or `DEACTIVATED` account cannot log in.
5. The Account Service issues the token; other services validate it independently.
6. Role cannot be changed by the account holder (only Admin).

### Primary Group Member

**[RESOLVED]** – Member 1

---

## Service 2 – Driver & Vehicle Service

### Purpose

The Driver & Vehicle Service manages **driver-specific profiles**, **vehicle registrations**, and **driver availability**. It is the source of truth for whether a driver is available for assignment.

### Responsibilities

- Driver profile creation and management (bio, rating, availability status).
- Vehicle registration and management (make, model, plate number, capacity).
- Linking vehicles to drivers.
- Tracking and updating driver availability (`AVAILABLE`, `OFFLINE`, `ON_TRIP`).
- Exposing available drivers to the Ride Management Service.
- Exposing driver/vehicle summaries to other services by ID.
- Admin operations related to drivers (view all, approve, suspend).

### NOT Responsible For

| Responsibility | Belongs To |
|---|---|
| User authentication | Account Service |
| Credential management | Account Service |
| Ride creation | Ride Management Service |
| Ride status transitions | Ride Management Service |
| Fare calculation | Fare & Payment Service |
| Payments | Fare & Payment Service |

### Main Entities (Owned)

| Entity | Key Fields |
|---|---|
| `DriverProfile` | driverId (= accountId from Account Service), licenseNumber, rating, availabilityStatus, createdAt |
| `Vehicle` | vehicleId, driverId, make, model, year, plateNumber, capacity, isActive |

### Owned Data

- Driver profiles (linked to an accountId from Account Service, but the profile record is owned here).
- Vehicle records.
- Driver availability status.
- Driver rating (updated on ride completion – mechanism **[RESOLVED]** - NOT part of Phase 1 implementation).

### External Dependencies

| Service | Why |
|---|---|
| Account Service | The driverId is the accountId issued by Account Service. The Driver & Vehicle Service does not duplicate account/credential data. |

### APIs Exposed

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| POST | `/api/v1/drivers` | Create driver profile (after account creation) | Authenticated (DRIVER) |
| GET | `/api/v1/drivers/me` | Get own driver profile | Authenticated (DRIVER) |
| PATCH | `/api/v1/drivers/me` | Update own driver profile | Authenticated (DRIVER) |
| PATCH | `/api/v1/drivers/me/availability` | Set availability status (AVAILABLE or OFFLINE) | Authenticated (DRIVER) |
| GET | `/api/v1/drivers/available` | List available drivers | Internal (Ride Management Service) |
| GET | `/api/v1/drivers/{driverId}` | Get driver profile by ID | Internal / Admin |
| GET | `/api/v1/drivers` | List all drivers | Admin |
| POST | `/api/v1/vehicles` | Register a vehicle | Authenticated (DRIVER) |
| GET | `/api/v1/vehicles/me` | List own vehicles | Authenticated (DRIVER) |
| GET | `/api/v1/vehicles/{vehicleId}` | Get vehicle by ID | Internal / Admin |
| PATCH | `/api/v1/drivers/{driverId}/availability` | Update driver availability (system-set: ON_TRIP) | Internal (Ride Management Service) |

> **Authoritative contract:** [`contracts/driver-vehicle-service.yaml`](../../contracts/driver-vehicle-service.yaml)

### APIs Consumed

| Service | Endpoint | Why |
|---|---|---|
| Account Service | `GET /accounts/{id}` | Optional: validate that accountId exists before creating a driver profile |

### Important Business Rules

1. A driver profile must be linked to an existing account with role `DRIVER`.
2. A driver must have at least one active vehicle to be eligible for ride assignment.
3. A driver cannot be `AVAILABLE` without at least one active vehicle.
4. Driver availability is the single source of truth for assignment eligibility.
5. Only the Ride Management Service may update a driver's availability to `ON_TRIP`.
6. Rating updates require a completed ride – mechanism to be defined (see `07-communication-and-security.md`).

### Primary Group Member

**[RESOLVED]** – Member 2

---

## Service 3 – Ride Management Service

### Purpose

The Ride Management Service is responsible for the **entire ride lifecycle**. It is the orchestrator of the ride from initial request through to completion or cancellation.

### Responsibilities

- Accepting ride requests from passengers.
- Querying Driver & Vehicle Service for available drivers.
- Assigning a driver to a requested ride.
- Managing ride status transitions (`REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED`).
- Enforcing valid status transition rules.
- Handling ride cancellation (where status permits).
- Storing ride history.
- Triggering fare calculation on ride completion.
- Exposing ride details to other services (e.g., Fare & Payment Service).

### NOT Responsible For

| Responsibility | Belongs To |
|---|---|
| User authentication | Account Service |
| Driver profile management | Driver & Vehicle Service |
| Vehicle management | Driver & Vehicle Service |
| Driver availability tracking | Driver & Vehicle Service |
| Fare calculation | Fare & Payment Service |
| Payment processing | Fare & Payment Service |
| Receipts | Fare & Payment Service |

### Main Entities (Owned)

| Entity | Key Fields |
|---|---|
| `Ride` | rideId, passengerId, driverId, vehicleId, pickupLocation, dropoffLocation, status, requestedAt, assignedAt, acceptedAt, startedAt, completedAt, cancelledAt, distanceKm, durationMinutes |
| `RideStatusHistory` | historyId, rideId, fromStatus, toStatus, changedAt, changedBy |

### Owned Data

- All ride records and their full lifecycle history.
- Pickup and dropoff location data (simulated – strings or coordinates).
- Distance and duration (simulated values for fare calculation).

### External Dependencies

| Service | Why | When |
|---|---|---|
| Driver & Vehicle Service | Fetch available drivers for assignment | On ride request |
| Driver & Vehicle Service | Update driver availability to ON_TRIP | On driver assignment |
| Driver & Vehicle Service | Update driver availability to AVAILABLE | On ride cancellation or completion |
| Fare & Payment Service | Notify / trigger fare calculation | On ride completion (mechanism **[RESOLVED]** - Synchronous REST call to FPS) |

### APIs Exposed

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| POST | `/api/v1/rides` | Request a new ride | Authenticated (PASSENGER) |
| GET | `/api/v1/rides/{rideId}` | Get ride details | Authenticated (owner or DRIVER assigned or ADMIN) |
| GET | `/api/v1/rides` | List rides | Authenticated (own rides; ADMIN sees all) |
| PATCH | `/api/v1/rides/{rideId}/status` | Transition ride status | Authenticated (DRIVER or ADMIN) |
| DELETE | `/api/v1/rides/{rideId}` | Cancel a ride | Authenticated (PASSENGER or DRIVER – subject to status rules) |
| GET | `/api/v1/rides/{rideId}/history` | Get status history of a ride | Authenticated |

> **Authoritative contract:** [`contracts/ride-service.yaml`](../../contracts/ride-service.yaml)

### APIs Consumed

| Service | Endpoint | Why |
|---|---|---|
| Driver & Vehicle Service | `GET /api/v1/drivers/available` | Find available driver for assignment |
| Driver & Vehicle Service | `PATCH /api/v1/drivers/{driverId}/availability` | Set driver to ON_TRIP after assignment |
| Driver & Vehicle Service | `PATCH /api/v1/drivers/{driverId}/availability` | Set driver back to AVAILABLE after completion/cancel |

### Important Business Rules

1. Only a `PASSENGER` may request a ride.
2. Only the assigned `DRIVER` may accept, start, or complete a ride.
3. A `PASSENGER` may cancel a ride only when status is `REQUESTED` or `ASSIGNED`.
4. A `DRIVER` may cancel when status is `ACCEPTED` (with implications).
5. The system may cancel if no driver is found.
6. Ride status transitions must follow the defined state machine exactly (see `06-business-workflows.md`).
7. A ride must store the `driverId` from the assignment, not a full driver record.
8. Distance and duration are simulated values – they do not require real GPS or maps.

### Primary Group Member

**[RESOLVED]** – Member 3

---

## Service 4 – Fare & Payment Service

### Purpose

The Fare & Payment Service is responsible for **fare estimation**, **fare calculation** on completed rides, **simulated payment processing**, and **receipt generation**.

### Responsibilities

- Providing fare estimates before ride request (using simulated fare rules).
- Calculating the final fare after a ride is completed.
- Recording payment attempts and outcomes (simulated).
- Generating and storing receipts.
- Exposing receipt details to passengers and drivers.

### NOT Responsible For

| Responsibility | Belongs To |
|---|---|
| User authentication | Account Service |
| Driver profiles | Driver & Vehicle Service |
| Vehicle management | Driver & Vehicle Service |
| Ride status management | Ride Management Service |
| Ride assignment | Ride Management Service |

### Main Entities (Owned)

| Entity | Key Fields |
|---|---|
| `Fare` | fareId, rideId, baseFare, distanceFare, surcharge, totalAmount, currency, calculatedAt |
| `Payment` | paymentId, rideId, fareId, passengerId, paymentMethod, status, processedAt |
| `Receipt` | receiptId, rideId, fareId, paymentId, passengerId, driverId, issuedAt |

### Owned Data

- Fare records (with breakdown of base fare, distance component, surcharges).
- Payment records (simulated – status is `COMPLETED`, `FAILED`, or `PENDING`).
- Receipts.

### External Dependencies

| Service | Why |
|---|---|
| Ride Management Service | Reads ride details (distance, duration, passengerId, driverId) for fare calculation |
| Account Service | Reads passenger/driver name and email to enrich receipts |

### APIs Exposed

| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| POST | `/api/v1/fares/estimate` | Estimate fare for given distance | Authenticated (PASSENGER) |
| POST | `/api/v1/fares/calculate` | Calculate final fare for a completed ride | Internal / system trigger |
| GET | `/api/v1/fares/{rideId}` | Get fare details for a ride | Authenticated (owner or DRIVER or ADMIN) |
| POST | `/api/v1/payments` | Initiate simulated payment | Authenticated (PASSENGER) |
| GET | `/api/v1/payments/{paymentId}` | Get payment details | Authenticated (owner or ADMIN) |
| GET | `/api/v1/receipts/{rideId}` | Get receipt for a ride | Authenticated (PASSENGER or DRIVER assigned to ride) |

> **Authoritative contract:** [`contracts/fare-payment-service.yaml`](../../contracts/fare-payment-service.yaml)

### APIs Consumed

| Service | Endpoint | Why |
|---|---|---|
| Ride Management Service | `GET /api/v1/rides/{rideId}` | Read ride details for fare calculation |
| Account Service | `GET /api/v1/accounts/{accountId}` | Read passenger/driver name for receipt |

### Important Business Rules

1. Fare calculation may only be triggered for rides with status `COMPLETED`.
2. A passenger may not pay until a fare has been calculated.
3. Payment is simulated – the service does not connect to a real payment gateway.
4. A receipt is generated only after a successful payment.
5. Fare amounts are calculated using a defined formula: `baseFare + (distanceKm × ratePerKm)`.
6. The fare rate and base fare are configurable values (not hardcoded magic numbers).
7. Currency is simulated (e.g., a placeholder unit).

### Primary Group Member

**[RESOLVED]** – Member 4

---

## Cross-Service ID Reference Table

This table shows which IDs each service stores as foreign references.

| Service | Stores | From Service |
|---|---|---|
| Driver & Vehicle Service | `accountId` (as driverId) | Account Service |
| Ride Management Service | `passengerId` (= accountId) | Account Service |
| Ride Management Service | `driverId` (= accountId) | Account Service (via Driver & Vehicle Service) |
| Ride Management Service | `vehicleId` | Driver & Vehicle Service |
| Fare & Payment Service | `rideId` | Ride Management Service |
| Fare & Payment Service | `passengerId` | Account Service (read from ride) |
| Fare & Payment Service | `driverId` | Account Service (read from ride) |

---

## Open Boundary Decisions

| ID | Decision Required |
|---|---|
| OB-01 | **[RESOLVED]** Member-to-service assignment (Member 1: Account, Member 2: Driver, Member 3: Ride, Member 4: Fare) |
| OB-02 | **[RESOLVED]** How fare calculation is triggered (Synchronous REST call from RMS to FPS) |
| OB-03 | **[RESOLVED]** Driver ratings updates (Not part of Phase 1) |
| OB-04 | **[RESOLVED]** Driver cancellation results in ride CANCELLED state (No reassignment) |
