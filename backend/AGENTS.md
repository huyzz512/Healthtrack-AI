# Backend instructions

The root `AGENTS.md` applies.

## Ownership

Backend owns NestJS, PostgreSQL/Prisma, authentication and authorization, server business rules, object storage, AI/OTP/notification/PDF provider abstractions, analytics, reports, Swagger, Docker, and infrastructure.

Backend does not own Compose UI, Android lifecycle/state, Health Connect reads, Android permission flows, or local reminder scheduling.

## Boundaries

- Keep the backend a modular monolith.
- Controllers call application services; application services depend on repository/provider interfaces.
- Prisma is isolated behind infrastructure repositories/services.
- Cross-module reads go through an exported module interface.
- The authenticated principal is the ownership source; never trust client-supplied `userId` for authorization.
- Provider adapters do not query domain tables directly.

## Contract rules

`docs/api/integration-contract.md` is the shared contract. Do not add or alter endpoints, DTO fields, enums, error semantics, time/unit behavior, or auth behavior without the contract-change process.

## Forbidden changes

- Do not edit Android implementation to compensate for a backend mismatch.
- Do not expose private object-storage credentials or permanent public URLs.
- Do not put secrets or sensitive health payloads in logs.
- Do not create a migration until its schema proposal is approved.

## Required checks

From `backend/` run:

```text
npm run lint
npm run typecheck
npm test
npm run build
```

For database work also run Prisma formatting and validation, then migration validation only when migration creation has been approved.

