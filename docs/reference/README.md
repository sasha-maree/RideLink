# RideLink – Developer Reference Documentation

This directory is the **architectural source of truth** for the entire RideLink backend project.

All implementation work must be traceable to a decision recorded here.  
If something is not in these documents, it has not been decided.

---

## Purpose

These documents exist to:

- Give every group member a shared, authoritative understanding of what we are building.
- Prevent scope creep, architectural drift, and conflicting interpretations of the assignment.
- Act as the first reference for any technical decision before implementation begins.
- Serve as supporting evidence for the technical report and viva.

---

## Document Index and Source-of-Truth Hierarchy

### Reference Documents (this directory)

| Document | Controls | Source of Truth For |
|---|---|---|
| [01-system-overview.md](./01-system-overview.md) | Project scope | What we are building, what is out of scope, assignment constraints |
| [02-architecture.md](./02-architecture.md) | System architecture | Service decomposition, communication style, architectural patterns |
| [03-service-boundaries.md](./03-service-boundaries.md) | Service responsibilities | What each service owns, what it must not do |
| [04-api-contracts.md](./04-api-contracts.md) | API conventions and rules | Naming, versioning, error format, HTTP conventions, contract-first process |
| [05-data-ownership.md](./05-data-ownership.md) | Data boundaries | Which service owns which data, IDs exchanged between services |
| [06-business-workflows.md](./06-business-workflows.md) | Business logic | End-to-end flows, ride state machine, interaction sequences |
| [07-communication-and-security.md](./07-communication-and-security.md) | Cross-cutting concerns | Interservice communication, auth, roles, validation, security |
| [08-development-plan.md](./08-development-plan.md) | Execution sequence | Phase-by-phase implementation roadmap |

### Authoritative API Contracts (contracts/ directory)

> **These files are the authoritative API contracts. They supersede any endpoint definitions in prose documents.**

| Contract File | Service |
|---|---|
| [`contracts/account-service.yaml`](../../contracts/account-service.yaml) | Account Service |
| [`contracts/driver-vehicle-service.yaml`](../../contracts/driver-vehicle-service.yaml) | Driver & Vehicle Service |
| [`contracts/ride-service.yaml`](../../contracts/ride-service.yaml) | Ride Management Service |
| [`contracts/fare-payment-service.yaml`](../../contracts/fare-payment-service.yaml) | Fare & Payment Service |

### Contract-First Hierarchy

```
04-api-contracts.md       ← API design rules and conventions
        ↓
contracts/*.yaml          ← AUTHORITATIVE exact endpoint/schema contracts
        ↓
Service implementation     ← Must conform to contracts
        ↓
Swagger UI                ← Reflects the implementation
        ↓
Postman collection         ← Mirrors the contracts
```

---

## How to Use These Documents

### Before starting any feature or task

1. Read `01-system-overview.md` to confirm the feature is in scope.
2. Read `03-service-boundaries.md` to confirm which service owns the feature.
3. Read `04-api-contracts.md` to understand the API conventions and rules.
4. Read the relevant `contracts/*.yaml` file to understand the exact API contract you must implement.
5. Read `05-data-ownership.md` to understand what data your service may touch.
6. Read `06-business-workflows.md` to understand how your feature fits into the end-to-end flow.

### Before making architectural or API changes

Changes to the architecture, service boundaries, or API contracts must follow this process:

1. **Propose** the change by raising it in the group chat or pull request discussion.
2. **Update** the relevant reference document before or alongside the code change.
3. **Reference** the document update in your commit or PR.
4. **All four members must acknowledge** a boundary or contract change before it is merged.

> Unilateral changes to APIs or service boundaries without updating these documents are not permitted.

---

## Rules

### Rule 1 – Implementation follows the reference

No service may implement behaviour that contradicts these documents without first updating the document and obtaining team agreement.

### Rule 2 – API contract changes must be documented

Any addition, removal, or modification of an endpoint, field, or status code must be reflected in `04-api-contracts.md` before or alongside the implementation PR.

### Rule 3 – Architectural changes must be recorded

Any change to the architecture (e.g., adding a component, changing communication method, changing security approach) must be reflected in `02-architecture.md`.

### Rule 4 – Open decisions must be resolved before implementation

Items marked `[DECISION REQUIRED]` in any document must be resolved and documented in the relevant Open Decisions section before work that depends on them begins.

### Rule 5 – Cross-service database access is strictly prohibited

No service may read from or write to another service's database or tables, under any circumstances. All cross-service data access must go through the approved service API. See `05-data-ownership.md`.

---

## Open Decision Process

When a decision cannot be resolved immediately:

1. Record it in the relevant document under its **Open Decisions** section using the tag `[DECISION REQUIRED]`.
2. Add a brief description of what must be decided and the options being considered.
3. When the team reaches consensus, update the document, remove the `[DECISION REQUIRED]` tag, and record the chosen option and rationale.

---

## Document Status

| Document | Status |
|---|---|
| 01-system-overview.md | Draft – Pending team review |
| 02-architecture.md | Draft – Pending team review |
| 03-service-boundaries.md | Draft – Pending team review |
| 04-api-contracts.md | Draft – Pending team review |
| 05-data-ownership.md | Draft – Pending team review |
| 06-business-workflows.md | Draft – Pending team review |
| 07-communication-and-security.md | Draft – Pending team review |
| 08-development-plan.md | Draft – Pending team review |

> Status should be updated to **Approved** once the team has reviewed and agreed on the document contents.
