# 06 – Business Workflows

**Document type:** Business workflow and state machine definition  
**Source of truth for:** End-to-end business flows, ride state machine, and interaction sequences  
**Status:** Draft – Pending team review

---

## Ride State Machine

This is the definitive definition of the ride lifecycle. All services must respect these transitions.

### Valid States

| State | Meaning |
|---|---|
| `REQUESTED` | Passenger has submitted a ride request; system is attempting to assign a driver |
| `ASSIGNED` | System has assigned an available driver; driver has not yet responded |
| `ACCEPTED` | Assigned driver has accepted the ride |
| `IN_PROGRESS` | Driver has started the ride; passenger is in the vehicle |
| `COMPLETED` | Driver has marked the ride as complete |
| `CANCELLED` | Ride was cancelled by passenger, driver, or system |

### Valid Transitions

```mermaid
stateDiagram-v2
    [*] --> REQUESTED : Passenger requests ride
    REQUESTED --> ASSIGNED : System assigns driver
    REQUESTED --> CANCELLED : No driver found / Passenger cancels
    ASSIGNED --> ACCEPTED : Driver accepts
    ASSIGNED --> CANCELLED : Driver declines / Passenger cancels
    ACCEPTED --> IN_PROGRESS : Driver starts ride
    ACCEPTED --> CANCELLED : Driver cancels (exceptional)
    IN_PROGRESS --> COMPLETED : Driver completes ride
    COMPLETED --> [*]
    CANCELLED --> [*]
```

### Transition Rules Table

| From | To | Triggered By | Conditions |
|---|---|---|---|
| *(new)* | `REQUESTED` | PASSENGER via POST /rides | Valid passenger, valid pickup/dropoff |
| `REQUESTED` | `ASSIGNED` | System (RMS) | Available driver found |
| `REQUESTED` | `CANCELLED` | System or PASSENGER | No driver found, or passenger cancels |
| `ASSIGNED` | `ACCEPTED` | DRIVER | Only the assigned driver |
| `ASSIGNED` | `CANCELLED` | PASSENGER or DRIVER | Passenger withdraws, or driver declines |
| `ACCEPTED` | `IN_PROGRESS` | DRIVER | Only the assigned driver |
| `ACCEPTED` | `CANCELLED` | DRIVER | Exceptional case – driver cancels before pickup |
| `IN_PROGRESS` | `COMPLETED` | DRIVER | Only the assigned driver |

### Invalid Transitions (Negative Scenarios)

Any transition not listed above is **invalid** and must return a `409 INVALID_STATUS_TRANSITION` error.

Examples of invalid transitions to test:
- `ASSIGNED → IN_PROGRESS` (skipped ACCEPTED)
- `COMPLETED → CANCELLED`
- `IN_PROGRESS → ACCEPTED`
- `CANCELLED → any state`
- `COMPLETED → any state`

---

## Workflow 1 – Account Registration and Login

**Actor:** New user (Passenger or Driver)  
**Services involved:** Account Service

```mermaid
sequenceDiagram
    actor User
    participant AS as Account Service

    User->>AS: POST /auth/register {firstName, lastName, email, password, role}
    AS->>AS: Validate input fields
    AS->>AS: Check email uniqueness
    AS->>AS: Hash password
    AS->>AS: Create account record
    AS-->>User: 201 Created {accountId, email, role, status: ACTIVE}

    User->>AS: POST /auth/login {email, password}
    AS->>AS: Verify credentials
    AS->>AS: Generate token
    AS-->>User: 200 OK {token, role, accountId}
```

**Failure scenarios:**
- Email already registered → `409 Conflict`
- Wrong password → `401 Unauthorized`
- Account suspended → `403 Forbidden`

---

## Workflow 2 – Driver Registration and Preparation

**Actor:** Driver (already has an account with role=DRIVER)  
**Services involved:** Account Service, Driver & Vehicle Service

```mermaid
sequenceDiagram
    actor Driver
    participant AS as Account Service
    participant DVS as Driver & Vehicle Service

    Driver->>AS: POST /auth/register {role: DRIVER, ...}
    AS-->>Driver: 201 Created {accountId}

    Driver->>AS: POST /auth/login
    AS-->>Driver: 200 OK {token}

    Driver->>DVS: POST /drivers {licenseNumber}
    DVS->>DVS: Validate input
    DVS->>DVS: Create driver profile (driverId = accountId)
    DVS-->>Driver: 201 Created {driverId, availabilityStatus: OFFLINE}

    Driver->>DVS: POST /vehicles {make, model, year, plateNumber, capacity}
    DVS->>DVS: Validate input, check plate uniqueness
    DVS->>DVS: Link vehicle to driver
    DVS-->>Driver: 201 Created {vehicleId}

    Driver->>DVS: PATCH /drivers/me/availability {status: AVAILABLE}
    DVS->>DVS: Check driver has active vehicle
    DVS-->>Driver: 200 OK {availabilityStatus: AVAILABLE}
```

**Failure scenarios:**
- Driver profile already exists → `409 Conflict`
- No active vehicle when going AVAILABLE → `403 Forbidden`
- Plate number already registered → `409 Conflict`

---

## Workflow 3 – Fare Estimation

**Actor:** Passenger  
**Services involved:** Fare & Payment Service

```mermaid
sequenceDiagram
    actor Passenger
    participant FPS as Fare & Payment Service

    Passenger->>FPS: POST /fares/estimate {estimatedDistanceKm: 12.5}
    FPS->>FPS: Apply fare formula: baseFare + (distance × rate)
    FPS-->>Passenger: 200 OK {estimatedFare: 85.50, currency, breakdown}
```

> This workflow has no interservice calls. Fare estimation uses only the Fare & Payment Service's own configured fare rules.

---

## Workflow 4 – Ride Request and Driver Assignment

**Actor:** Passenger  
**Services involved:** Ride Management Service, Driver & Vehicle Service

```mermaid
sequenceDiagram
    actor Passenger
    participant RMS as Ride Management Service
    participant DVS as Driver & Vehicle Service

    Passenger->>RMS: POST /rides {pickupLocation, dropoffLocation, estimatedDistanceKm}
    RMS->>RMS: Validate input, verify passenger role
    RMS->>DVS: GET /drivers/available
    DVS-->>RMS: 200 OK [{driverId, vehicleId, rating}]

    alt Driver(s) available
        RMS->>RMS: Select driver (e.g., first available or highest rated)
        RMS->>RMS: Create ride record {status: REQUESTED → ASSIGNED}
        RMS->>DVS: PATCH /drivers/{driverId}/availability {status: ON_TRIP}
        DVS-->>RMS: 200 OK
        RMS-->>Passenger: 201 Created {rideId, status: ASSIGNED, driverId}
    else No drivers available
        RMS->>RMS: Create ride record {status: REQUESTED}
        RMS-->>Passenger: 503 Service Unavailable / 409 {code: NO_DRIVER_AVAILABLE}
    end
```

**Interservice interaction:** RMS → DVS (ISI-01 and ISI-02)

---

## Workflow 5 – Ride Acceptance

**Actor:** Driver  
**Services involved:** Ride Management Service

```mermaid
sequenceDiagram
    actor Driver
    participant RMS as Ride Management Service

    Driver->>RMS: PATCH /rides/{rideId}/status {action: ACCEPT}
    RMS->>RMS: Verify driver is the assigned driver for this ride
    RMS->>RMS: Verify current status is ASSIGNED
    RMS->>RMS: Transition status: ASSIGNED → ACCEPTED
    RMS->>RMS: Record transition in ride_status_history
    RMS-->>Driver: 200 OK {rideId, status: ACCEPTED, acceptedAt}
```

**Failure scenarios:**
- Wrong driver tries to accept → `403 Forbidden`
- Ride is not in ASSIGNED state → `409 Invalid transition`

---

## Workflow 6 – Ride Start

**Actor:** Driver  
**Services involved:** Ride Management Service

```mermaid
sequenceDiagram
    actor Driver
    participant RMS as Ride Management Service

    Driver->>RMS: PATCH /rides/{rideId}/status {action: START}
    RMS->>RMS: Verify driver is assigned driver
    RMS->>RMS: Verify current status is ACCEPTED
    RMS->>RMS: Transition status: ACCEPTED → IN_PROGRESS
    RMS->>RMS: Record startedAt timestamp
    RMS-->>Driver: 200 OK {rideId, status: IN_PROGRESS, startedAt}
```

---

## Workflow 7 – Ride Completion

**Actor:** Driver  
**Services involved:** Ride Management Service, Driver & Vehicle Service, Fare & Payment Service

```mermaid
sequenceDiagram
    actor Driver
    participant RMS as Ride Management Service
    participant DVS as Driver & Vehicle Service
    participant FPS as Fare & Payment Service

    Driver->>RMS: PATCH /rides/{rideId}/status {action: COMPLETE}
    RMS->>RMS: Verify assigned driver and IN_PROGRESS status
    RMS->>RMS: Transition status: IN_PROGRESS → COMPLETED
    RMS->>RMS: Set completedAt, calculate/record distanceKm, durationMinutes
    RMS->>DVS: PATCH /drivers/{driverId}/availability {status: AVAILABLE}
    DVS-->>RMS: 200 OK
    RMS->>FPS: POST /fares/calculate {rideId, distanceKm, durationMinutes}
    FPS->>RMS: GET /rides/{rideId}
    RMS-->>FPS: 200 OK {ride details}
    FPS->>FPS: Calculate fare, store fare record
    FPS-->>RMS: 200 OK {fareId, totalAmount}
    RMS-->>Driver: 200 OK {rideId, status: COMPLETED}
```

> **[RESOLVED]** – The trigger mechanism for fare calculation is a synchronous call from RMS to FPS (see OB-02 in `03-service-boundaries.md`).

---

## Workflow 8 – Final Fare and Simulated Payment

**Actor:** Passenger  
**Services involved:** Fare & Payment Service

```mermaid
sequenceDiagram
    actor Passenger
    participant FPS as Fare & Payment Service

    Passenger->>FPS: GET /fares/{rideId}
    FPS-->>Passenger: 200 OK {fareId, totalAmount, breakdown}

    Passenger->>FPS: POST /payments {rideId, paymentMethod: CARD}
    FPS->>FPS: Verify ride is COMPLETED and fare exists
    FPS->>FPS: Verify no duplicate payment
    FPS->>FPS: Simulate payment processing
    alt Payment succeeds
        FPS->>FPS: Record payment {status: COMPLETED}
        FPS->>FPS: Generate receipt
        FPS-->>Passenger: 200 OK {paymentId, status: COMPLETED}
    else Payment fails (simulated)
        FPS->>FPS: Record payment {status: FAILED}
        FPS-->>Passenger: 402/422 {code: PAYMENT_FAILED}
    end
```

---

## Workflow 9 – Receipt Retrieval

**Actor:** Passenger or Driver  
**Services involved:** Fare & Payment Service, Account Service

```mermaid
sequenceDiagram
    actor Passenger
    participant FPS as Fare & Payment Service
    participant AS as Account Service

    Passenger->>FPS: GET /receipts/{rideId}
    FPS->>FPS: Verify ride is paid and receipt exists
    FPS->>AS: GET /accounts/{passengerId}
    AS-->>FPS: 200 OK {name, email}
    FPS->>AS: GET /accounts/{driverId}
    AS-->>FPS: 200 OK {name}
    FPS->>FPS: Compose receipt with fare, payment, and account details
    FPS-->>Passenger: 200 OK {receipt}
```

> If the receipt was previously generated and a name snapshot was stored, the second pair of Account Service calls may be skipped.

---

## Workflow 10 – Cancellation

**Sub-workflow A: Passenger cancels while REQUESTED or ASSIGNED**

```mermaid
sequenceDiagram
    actor Passenger
    participant RMS as Ride Management Service
    participant DVS as Driver & Vehicle Service

    Passenger->>RMS: DELETE /rides/{rideId}
    RMS->>RMS: Verify passenger owns ride
    RMS->>RMS: Verify status is REQUESTED or ASSIGNED
    RMS->>RMS: Transition status: → CANCELLED
    opt If driver was assigned
        RMS->>DVS: PATCH /drivers/{driverId}/availability {status: AVAILABLE}
        DVS-->>RMS: 200 OK
    end
    RMS-->>Passenger: 200 OK {rideId, status: CANCELLED}
```

**Sub-workflow B: Passenger tries to cancel a COMPLETED ride (negative scenario)**

```mermaid
sequenceDiagram
    actor Passenger
    participant RMS as Ride Management Service

    Passenger->>RMS: DELETE /rides/{rideId}
    RMS->>RMS: Verify status
    RMS-->>Passenger: 409 Conflict {code: CANNOT_CANCEL_COMPLETED_RIDE}
```

---

## Workflow 11 – Negative Scenarios (Required Demonstrations)

The team must select at least two of the following to formally demonstrate.

### N-01: No Available Driver

```
POST /rides
↓
RMS calls GET /drivers/available
↓
DVS returns [] (empty list)
↓
RMS returns 503 / 409 {code: NO_DRIVER_AVAILABLE}
```

### N-02: Invalid Ride Status Transition

```
PATCH /rides/{rideId}/status {action: COMPLETE}
(when ride is still in ASSIGNED state)
↓
RMS checks: ASSIGNED → COMPLETED is not a valid transition
↓
RMS returns 409 {code: INVALID_STATUS_TRANSITION}
```

### N-03: Unauthorized Access

```
GET /rides/{rideId}
(with no token, or a passenger trying to view another passenger's ride)
↓
Returns 401 or 403
```

### N-04: Invalid Input

```
POST /auth/register {email: "not-an-email", password: "1"}
↓
Account Service validates input
↓
Returns 400 {error: {code: VALIDATION_ERROR, details: [...]}}
```

### N-05: Simulated Payment Failure

```
POST /payments {rideId, paymentMethod: CARD}
↓
Fare & Payment Service simulates processing
↓
Returns 402 / 422 {code: PAYMENT_FAILED}
```

### N-06: Duplicate Registration

```
POST /auth/register {email: "existing@email.com", ...}
↓
Account Service checks uniqueness
↓
Returns 409 {code: EMAIL_ALREADY_REGISTERED}
```

---

## Fare Calculation Formula

The fare calculation uses a fixed, configurable formula. This is a **team design decision** (not an assignment requirement), but must be consistent across all services that reference fares.

```
totalFare = baseFare + (distanceKm × ratePerKm)
```

| Parameter | Description | Example Value |
|---|---|---|
| `baseFare` | Fixed minimum charge per ride | 300.00 |
| `ratePerKm` | Charge per kilometre | 100.00 |
| `surcharge` | Optional peak-hour or other charge | 0.00 (default) |

> Example: 12.5 km ride = 300.00 + (12.5 × 100.00) = 300.00 + 1250.00 = **1550.00 LKR**

Exact values are **[RESOLVED]** (baseFare: 300.00, ratePerKm: 100.00, Currency: LKR) and should be stored as configuration, not hardcoded.

---

## Open Workflow Decisions

| ID | Decision Required |
|---|---|
| OW-01 | **[RESOLVED]** Driver selection algorithm: FIRST AVAILABLE DRIVER |
| OW-02 | **[RESOLVED]** When an assigned driver declines: Ride becomes CANCELLED (no reassignment) |
| OW-03 | **[RESOLVED]** Driver rating update is NOT part of Phase 1 implementation |
| OW-04 | **[RESOLVED]** Fare calculation is synchronous from RMS |
| OW-05 | **[RESOLVED]** Fare formula parameter values (baseFare: 300.00, ratePerKm: 100.00, LKR) |
| OW-06 | **[RESOLVED]** Negative scenarios: N-02 (Invalid ride status transition), N-05 (Simulated payment failure) |
