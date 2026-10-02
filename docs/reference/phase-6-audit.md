# Phase 6 Audit — RideLink Backend Integrated Workflow, Validation & Hardening

**Date:** 2026-09-26  
**Conducted by:** Antigravity AI / Phase 6 Verification  
**Status at Phase Entry:** Phases 0–5 complete ✅  

---

## Table of Contents

1. [Workstream A — Build & Static Verification](#workstream-a)
2. [Workstream B — Security & Configuration Hardening](#workstream-b)
3. [Workstream C — Defect Analysis & Resolution](#workstream-c)
4. [Workstream D — Test Hardening](#workstream-d)
5. [Final Test Counts](#final-test-counts)
6. [Phase 6 Verdict](#phase-6-verdict)

---

## Workstream A — Build & Static Verification

All four services compile successfully using **Java 21.0.12.1 / Spring Boot 3.3.4 / Maven 3.x**.

| Service | Build Result | Initial Tests | Notes |
|---------|-------------|---------------|-------|
| account-service (AS) | BUILD SUCCESS | 17 | Port 8081 |
| driver-vehicle-service (DVS) | BUILD SUCCESS | 9 | Port 8082 |
| ride-service (RMS) | BUILD SUCCESS | 11 | Port 8083 |
| fare-payment-service (FPS) | BUILD SUCCESS | 21 | Port 8084 |

Note: "Exit code 1" appearing in CI output is a Windows PowerShell artifact from stream redirection
(`2>&1 | Select-String`), NOT a build failure. All `BUILD SUCCESS` results are genuine.

---

## Workstream B — Security & Configuration Hardening

### B-1. JWT Secret Alignment

**DEFECT FOUND (D-01a): Inconsistent `jwt.secret` defaults across services.**

Before Phase 6, services used different default values:
- AS, DVS, RMS → `defaultSecretKeyWhichShouldBeAtLeast32BytesLong`
- FPS → `dGhpc2lzYXN1cGVyc2VjcmV0a2V5Zm9ycmlkZWxpbmtwcm9qZWN0MjAyNA==` (DIFFERENT!)

Impact: Tokens issued by Account Service would fail validation in Fare-Payment Service
when services are started with no environment variables — a critical regression in default-config deployments.

**Resolution:** All four services now use: `default-insecure-secret-for-local-dev-only`

Files modified:
- services/account-service/.../security/JwtUtil.java
- services/driver-vehicle-service/.../security/JwtUtil.java
- services/ride-service/.../config/JwtUtil.java
- services/fare-payment-service/.../config/JwtUtil.java

### B-2. Missing application.yml Configuration

**DEFECT FOUND (D-01b): Missing explicit configuration in ride-service and fare-payment-service.**

`ride-service/application.yml` was missing:
- fare.payment.service.url
- driver.vehicle.service.url
- jwt.service.ride.secret and jwt.service.fare.secret

`fare-payment-service/application.yml` was missing:
- jwt.service.ride.secret and jwt.service.fare.secret
- account.service.url and ride.service.url

`account-service/application.yml` was missing:
- jwt.service.fare.secret (needed to validate FPS service tokens — see D-02)

**Resolution:** All service URLs and service JWT secrets are now explicit in `application.yml`
with full environment variable support via `${VAR_NAME:default}` pattern.

### B-3. Service JWT Architecture (OS-05)

The OS-05 service JWT architecture is correctly implemented in RMS and FPS:

| Caller → Target | Token Type | Endpoint | Authorization |
|----------------|------------|----------|---------------|
| RMS → DVS (GET /drivers/available) | User JWT | .authenticated() | DVS JwtAuthFilter |
| RMS → DVS (PATCH /drivers/{id}/availability) | User JWT | .authenticated() | DVS JwtAuthFilter |
| RMS → FPS (POST /fares/calculate) | RIDE_SERVICE JWT | hasRole('SERVICE') and principal == 'RIDE_SERVICE' | FPS JwtAuthFilter |
| FPS → RMS (GET /rides/{id}) | FARE_PAYMENT_SERVICE JWT | .authenticated() | RMS JwtAuthFilter |
| FPS → AS (GET /accounts/{id}) | FARE_PAYMENT_SERVICE JWT | hasRole('SERVICE') (new) | AS JwtAuthFilter (new) |

Note on RMS→DVS: These calls use the caller's passenger JWT rather than a dedicated
RIDE_SERVICE JWT. This works at runtime because DVS internal endpoints only require
`.authenticated()`, but it is an architectural deviation from OS-05. No remediation
applied as it does not cause a runtime failure. Recommended for a future hardening pass.

---

## Workstream C — Defect Analysis & Resolution

### D-02 (CRITICAL): Receipt Generation — Cross-Account Access Failure

**Root Cause:**
`FPS.ReceiptService.generateReceipt()` calls `AccountClient.getAccountDetails(driverId, passengerToken)`
to build the receipt. The Account Service `AccountController.getAccountById` uses:

    @PreAuthorize("hasRole('ADMIN') or principal == #accountId")

When a passenger token is used, `principal` = passenger UUID != driver UUID → HTTP 403.
The FPS wraps this in a `RuntimeException` which becomes 500 to the API client.

**Workflow Impact:** `GET /api/v1/receipts/{rideId}` always fails with 500 for any ride
where passenger != driver (i.e., every real ride).

**Resolution Applied:**

1. account-service/security/JwtUtil.java:
   Added `validateFarePaymentServiceToken()` — validates tokens signed with
   `jwt.service.fare.secret` and checks `type=SERVICE, service=FARE_PAYMENT_SERVICE` claims.

2. account-service/security/JwtAuthenticationFilter.java:
   Updated to check for FARE_PAYMENT_SERVICE tokens first. When recognized, sets
   principal to "FARE_PAYMENT_SERVICE" with ROLE_SERVICE authority.

3. account-service/config/SecurityConfig.java:
   Updated `GET /api/v1/accounts/**` rule to allow ROLE_SERVICE.

4. account-service/controller/AccountController.java:
   Updated `@PreAuthorize` to:
   `"hasRole('ADMIN') or hasRole('SERVICE') or principal == #accountId"`

5. fare-payment-service/client/AccountClient.java:
   Updated to inject `JwtUtil` and call `jwtUtil.generateFarePaymentServiceToken()`
   for the Authorization header. The `callerToken` parameter is kept for API
   compatibility but no longer forwarded.

**Before/After:**
```
# BEFORE (BROKEN)
FPS → AS GET /accounts/{driverId} [Bearer: passengerJWT]
AS: @PreAuthorize: passenger != driver → 403
FPS: RuntimeException → 500

# AFTER (FIXED)
FPS → AS GET /accounts/{driverId} [Bearer: farePaymentServiceJWT]
AS: JwtAuthFilter: validateFarePaymentServiceToken = true → ROLE_SERVICE
AS: @PreAuthorize: hasRole('SERVICE') → PASS → 200 OK
```

---

## Workstream D — Test Hardening

### New Test Classes Added

| File | Service | Tests | Coverage |
|------|---------|-------|---------|
| RideServiceHardenedTest.java | ride-service | 10 | N-02 invalid transitions, ownership, completion |
| FarePaymentHardenedTest.java | fare-payment-service | 8 | N-05 WALLET failure, fare formula, duplicates |
| AccountJwtHardenedTest.java | account-service | 11 | Service JWT accept/reject, user JWT security |

### Negative Scenario Coverage Matrix

| Scenario | Before Phase 6 | After Phase 6 |
|----------|----------------|---------------|
| N-02: Invalid ride status transitions | Partial (3 tests) | Full (10 tests) |
| N-05: WALLET payment simulated failure | 2 tests | 3 tests (adds persistence check) |
| Service JWT rejection for non-service callers | 2 tests (FPS) | 5 tests (FPS) + 6 tests (AS) |
| Service JWT expiry rejection | 0 tests | 2 tests (AS) |
| Service JWT wrong-secret rejection | 0 tests | 2 tests (AS) |
| User JWT tampering rejection | 0 tests | 1 test (AS) |
| Duplicate payment rejection | 1 test | 2 tests (adds retry-allowed check) |
| Fare already calculated rejection | 1 test | 1 test |
| Ride not completed rejection | 1 test | 1 test |
| Authoritative fare formula assertion | 2 tests | 3 tests |

---

## Final Test Counts

| Service | Phase 5 Tests | Phase 6 Added | Total | Result |
|---------|--------------|---------------|-------|--------|
| account-service | 17 | 11 | 28 | ALL PASS |
| driver-vehicle-service | 9 | 0 | 9 | ALL PASS |
| ride-service | 11 | 10 | 21 | ALL PASS |
| fare-payment-service | 21 | 8 | 29 | ALL PASS |
| **Grand Total** | **58** | **29** | **87** | **0 FAILURES** |

---

## Phase 6 Verdict

### VERDICT: READY

### Evidence Summary

| Check | Result |
|-------|--------|
| All 4 services build cleanly with `mvn clean test` | PASS |
| 87 tests across all services — 0 failures | PASS |
| D-02 (receipt cross-account access) — fixed | PASS |
| D-01a (JWT secret misalignment) — fixed | PASS |
| D-01b (missing application.yml config) — fixed | PASS |
| OS-05 service JWT (RIDE_SERVICE → FPS) verified by test | PASS |
| OS-05 service JWT (FPS → AS) — new implementation verified | PASS |
| N-02 invalid transitions fully covered | PASS |
| N-05 WALLET failure behavior and persistence verified | PASS |
| Database isolation: each service has its own MongoDB URI | PASS |
| Security: all sensitive endpoints require ROLE_ authority | PASS |

### Open Items (Not Phase 6 Blockers)

1. **RMS→DVS service JWT**: Internal driver availability calls use the caller's user JWT
   instead of a RIDE_SERVICE JWT. Functional today (DVS internal endpoints require
   `.authenticated()`), but architecturally inconsistent with OS-05.
   Recommend addressing in a future security hardening pass.

2. **Payment idempotency**: No idempotency key support on `POST /payments`. Second retry
   creates a new payment record. The `paymentAlreadyCompleted` guard mitigates this
   but doesn't prevent race conditions under concurrent requests.

3. **Compensation on RMS/DVS race**: If `rideRepository.save()` fails after
   `updateDriverAvailability("ON_TRIP")`, the rollback calls
   `updateDriverAvailability("AVAILABLE")`. If that second DVS call also fails, the
   driver is stuck as ON_TRIP. Accepted behavior (no distributed transactions in scope).

4. **E2E runtime tests**: All tests are unit/MockMvc. Runtime integration testing
   requires all four services and MongoDB to be running simultaneously.
