# 04 – API Conventions and Contract Rules

**Document type:** API design rules and conventions  
**Source of truth for:** API design conventions, naming rules, versioning, error format, and the contract-first process  
**Status:** Draft – Pending team review

---

## Contract-First Development

RideLink follows a **contract-first** approach to API development.

### Rule: The OpenAPI Files Are the Authoritative Contracts

```
This document defines RULES and CONVENTIONS.
The contracts/*.yaml files define the EXACT API CONTRACTS.
```

| What | Where |
|---|---|
| API design rules, naming, conventions, error format | **This document** (`04-api-contracts.md`) |
| Exact endpoint definitions, schemas, status codes | **`contracts/*.yaml`** |

**Do not define endpoint details in this document.** Endpoint definitions belong exclusively in the OpenAPI contract files.

### Authoritative Contract Files

| Service | Contract File |
|---|---|
| Account Service | [`contracts/account-service.yaml`](../../contracts/account-service.yaml) |
| Driver & Vehicle Service | [`contracts/driver-vehicle-service.yaml`](../../contracts/driver-vehicle-service.yaml) |
| Ride Management Service | [`contracts/ride-service.yaml`](../../contracts/ride-service.yaml) |
| Fare & Payment Service | [`contracts/fare-payment-service.yaml`](../../contracts/fare-payment-service.yaml) |

All four files use **OpenAPI 3.0.3**.

### Contract Authority Hierarchy

```
contracts/*.yaml              ← AUTHORITATIVE (exact contract)
        ↓
Service implementation        ← Must conform exactly
        ↓
Swagger UI                    ← Reflects the implementation
        ↓
Postman collection            ← Mirrors the contracts
```

---

## API-First / Contract-First Workflow

When adding or changing an API:

1. **Define or update** the relevant `contracts/*.yaml` file.
2. **Review** the contract change as a team (pull request).
3. **Approve** — all members acknowledge the change.
4. **Implement** the endpoint in the service to match the contract.
5. **Update tests** — unit tests and Postman requests.
6. **Update Postman** — collection must reflect the contract.
7. **Validate Swagger** — Swagger UI must match the contract.
8. **Run CI** — all tests must pass.
9. **Create ADR** if the change is architecturally significant.

---

## URL and Versioning Convention

### Base Path

All endpoints are versioned under `/api/v1/`.

```
http://localhost:{PORT}/api/v1/{resource}
```

### Examples

```
POST   http://localhost:8081/api/v1/auth/register
GET    http://localhost:8083/api/v1/rides/{rideId}
PATCH  http://localhost:8083/api/v1/rides/{rideId}/status
POST   http://localhost:8084/api/v1/fares/estimate
```

### Versioning Rule

- The current version is `v1`.
- Version is part of the URL path (not a header).
- A new version (`v2`) would be introduced as a separate path prefix, not by modifying `v1`.
- For the scope of this assignment, `v1` is the only version needed.

---

## Resource Naming Convention

| Rule | Correct | Incorrect |
|---|---|---|
| Resource names are plural nouns | `/rides`, `/accounts` | `/ride`, `/account` |
| Path segments use kebab-case | `/ride-service`, `/driver-vehicle` | `/rideService` |
| Path parameters use camelCase | `{rideId}`, `{accountId}` | `{ride_id}`, `{RideID}` |
| Sub-resources follow the parent | `/rides/{rideId}/status` | `/status/rides/{rideId}` |
| Actions that are not CRUD use descriptive sub-paths | `/rides/{rideId}/status`, `/auth/login` | `/updateRideStatus` |

---

## HTTP Method Conventions

| Method | Use | Idempotent |
|---|---|---|
| `GET` | Retrieve a resource or collection | Yes |
| `POST` | Create a new resource or trigger an action | No |
| `PATCH` | Partial update of an existing resource | No (by convention) |
| `PUT` | Full replacement of a resource (not used in this project) | Yes |
| `DELETE` | Remove or cancel a resource | Yes |

### Specific Conventions

- Use `POST` for login, registration, and triggering actions (e.g., calculate fare).
- Use `PATCH` for partial updates and status transitions.
- Use `DELETE` for cancellation (semantically: the ride request is cancelled/removed).

---

## JSON Naming Convention

All JSON field names use **camelCase**.

```json
{
  "rideId": "uuid",
  "pickupLocation": "...",
  "rideStatus": "ASSIGNED",
  "requestedAt": "2024-01-01T10:00:00Z"
}
```

**Not** snake_case, PascalCase, or UPPER_CASE for JSON fields.

---

## Identifier Conventions

- All entity identifiers are **UUIDs** (format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`).
- IDs are represented as `string` with `format: uuid` in OpenAPI.
- IDs are named `{entityName}Id` — e.g., `rideId`, `accountId`, `driverId`, `vehicleId`, `fareId`.
- IDs are assigned by the owning service and are immutable.

**[RESOLVED]** – ID format is confirmed as UUID for all services. If the team changes this, all contracts must be updated.

---

## Stable IDs Exchanged Between Services

| ID | Issued By | Used By |
|---|---|---|
| `accountId` | Account Service | Driver & Vehicle Service (as `driverId`), Ride Management Service (as `passengerId`) |
| `driverId` | Account Service (via Account Service; equals `accountId`) | Ride Management Service, Fare & Payment Service |
| `vehicleId` | Driver & Vehicle Service | Ride Management Service |
| `rideId` | Ride Management Service | Fare & Payment Service |
| `fareId` | Fare & Payment Service | Payment, Receipt |

These IDs are **stable** — they never change after creation and are the only cross-service references allowed.

---

## Request and Response Conventions

### Content Type

All requests and responses use `application/json`.

No other content types are used in this project.

### Request Body

- Request bodies are JSON objects.
- Required fields are clearly marked in the OpenAPI contract.
- Optional fields may be omitted from the request — the service must treat absence as "no change" (for PATCH) or apply a default.

### Response Envelope

All responses use a consistent envelope:

**Success:**
```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 201,
  "data": { ... }
}
```

**Error:**
```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "One or more fields failed validation.",
  "path": "/api/v1/auth/register",
  "details": [
    "email: must be a valid email address",
    "password: must be at least 8 characters"
  ]
}
```

All four services must use this exact response envelope structure.

---

## Standard Error Response Structure

Defined and used consistently across all four services.

| Field | Type | Required | Description |
|---|---|---|---|
| `timestamp` | `string (date-time)` | Yes | UTC timestamp of the error (ISO 8601) |
| `status` | `integer` | Yes | HTTP status code |
| `error` | `string` | Yes | Machine-readable error code (e.g., `VALIDATION_ERROR`) |
| `message` | `string` | Yes | Human-readable description |
| `path` | `string` | Yes | Request path that caused the error |
| `details` | `array of string` | No | Field-level validation error messages |

This structure is defined in each `contracts/*.yaml` file as `ErrorResponse`.

---

## HTTP Status Code Conventions

| Code | When to Use |
|---|---|
| `200 OK` | Successful retrieval or successful update |
| `201 Created` | New resource successfully created |
| `204 No Content` | Successful operation with no response body (e.g., logout) |
| `400 Bad Request` | Malformed request body or failed field validation |
| `401 Unauthorized` | No valid authentication token provided |
| `403 Forbidden` | Token is valid but the role is not permitted for this operation |
| `404 Not Found` | Requested resource does not exist |
| `409 Conflict` | State conflict – e.g., duplicate email, invalid ride status transition, payment already made |
| `422 Unprocessable Entity` | Request is syntactically valid but semantically unprocessable – e.g., simulated payment failure |
| `500 Internal Server Error` | Unexpected server-side error – should not appear in normal operation |
| `503 Service Unavailable` | Downstream service call failed – e.g., no drivers available, downstream timeout |

### When NOT to use a status code

- Do **not** return `200` for failures. A `200` response always means the operation succeeded.
- Do **not** return `404` when a list is empty — return `200` with an empty array.
- Do **not** use `422` unless `409` and `400` are both genuinely inappropriate.

---

## Machine-Readable Error Codes

Error codes in the `error` field are `UPPER_SNAKE_CASE` strings. Examples:

| Code | Used When |
|---|---|
| `VALIDATION_ERROR` | One or more input fields failed validation |
| `EMAIL_ALREADY_REGISTERED` | Attempted registration with a duplicate email |
| `INVALID_CREDENTIALS` | Wrong email or password |
| `ACCOUNT_NOT_ACTIVE` | Account is suspended or deactivated |
| `DRIVER_PROFILE_EXISTS` | Driver profile already exists for this account |
| `NO_ACTIVE_VEHICLE` | Driver has no active vehicle and cannot go AVAILABLE |
| `NO_DRIVER_AVAILABLE` | No available drivers found during ride assignment |
| `RIDE_NOT_FOUND` | Ride ID does not exist |
| `INVALID_STATUS_TRANSITION` | Attempted an invalid ride lifecycle transition |
| `CANNOT_CANCEL_RIDE` | Ride cannot be cancelled at its current status |
| `FARE_ALREADY_CALCULATED` | Fare has already been calculated for this ride |
| `PAYMENT_ALREADY_COMPLETED` | Payment for this ride has already been successfully processed |
| `PAYMENT_FAILED` | Simulated payment failure |
| `DOWNSTREAM_SERVICE_UNAVAILABLE` | A dependent service could not be reached |

---

## Validation Rules (Global)

These apply uniformly across all services. Specific rules per endpoint are in the `contracts/*.yaml` files.

| Rule | Description |
|---|---|
| Required fields | All required fields must be present and non-null |
| String length | Enforced per field — see contracts |
| Email format | Must match RFC 5322 email format |
| Password | Minimum 8 characters, at least one digit |
| Enumerations | Must be one of the declared values |
| UUIDs | Must be valid UUID format for ID fields |
| Numbers | Must be within declared minimum/maximum bounds |
| Dates | ISO 8601 format (`date-time` = UTC with `Z` suffix) |

---

## Date and Time Format

All timestamps use **ISO 8601 UTC** format with a `Z` suffix.

```
2024-01-01T10:00:00Z
```

- Type in OpenAPI: `string` with `format: date-time`
- All dates are stored and transmitted in UTC.
- No timezone offsets are used in API responses.

---

## Pagination Convention

Pagination is used only on list endpoints that may return many results. For this assignment, the following simple page-based pagination is used where needed:

| Parameter | Type | Default | Max |
|---|---|---|---|
| `page` | query integer | 1 | — |
| `size` | query integer | 20 | 100 |

Paginated responses include:

```json
{
  "items": [ ... ],
  "total": 150,
  "page": 1,
  "size": 20
}
```

Pagination is not required on endpoints that typically return a small, bounded result set (e.g., `GET /vehicles/me`, `GET /drivers/available`).

---

## Authentication Requirements

### Public endpoints

No token required. Only these are public:

- `POST /api/v1/auth/register` (Account Service)
- `POST /api/v1/auth/login` (Account Service)
- `GET /api/v1/{service}/health` (all services — to be added in Phase 5)

### Protected endpoints

All other endpoints require a valid bearer token in the `Authorization` header:

```
Authorization: Bearer <token>
```

Missing or invalid tokens return `401 Unauthorized`.

The token type is **JWT** (OS-01 in `07-communication-and-security.md`).

### Role requirements

Each endpoint's role requirement is defined in the OpenAPI contract's `description` field and the authorization rules table in `07-communication-and-security.md`.

---

## Internal vs External Endpoints

| Type | Description | Marked In Contracts |
|---|---|---|
| **EXTERNAL** | Called by Swagger UI, Postman, or client actors | No special marking (default) |
| **INTERNAL** | Called by other services only | Noted in the endpoint `description` field |

Internal endpoints are still secured — they require a valid token.

`[TEAM DECISION REQUIRED]` — The internal service authentication mechanism (OS-05) must be decided. Options:
1. Pass the original user's token to the downstream service.
2. Use a shared service API key.

---

## Interservice API Conventions

When one service calls another:

1. Call the **published API** of the target service — never its database.
2. Use the **stable ID** (UUID) to reference entities.
3. Handle `503` and timeout errors gracefully — return a meaningful error to the original caller.
4. Do not store a full copy of the target entity — only store the foreign ID.
5. Internal calls use the same base path `/api/v1/`.

Interservice calls are documented in `docs/reference/07-communication-and-security.md`.

---

## Backward Compatibility

For the scope of this assignment (single development phase), breaking changes are permissible with team agreement. However:

- Any breaking change must update the relevant `contracts/*.yaml` file first.
- Breaking changes to interservice contracts must be discussed as a team before implementation.
- A breaking change is: removing an endpoint, removing a required field, changing a field type, changing a status code meaning.

---

## API Contract Change Process

1. **Raise** the proposed change (PR or team discussion).
2. **Update** the relevant `contracts/*.yaml`.
3. **Team review and approval** — all members must acknowledge.
4. **Update implementation** of the affected service.
5. **Update tests** (unit tests + Postman).
6. **Validate** Swagger UI.
7. **Run CI** — all tests pass.
8. **Create ADR** in `docs/reference/adr/` for significant architectural decisions.

Changes to this conventions document follow the same process.
