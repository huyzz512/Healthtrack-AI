# HealthTrack AI repository instructions

These instructions apply to the entire repository. More specific `AGENTS.md` files may add module rules but may not override shared governance without approval.

## Mission and scope

This repository is in Foundation stage. Build repository structure, tooling, architecture, contracts, interfaces, provider abstractions, placeholder UI, CI, and test foundations. Do not implement the complete UC-01 through UC-20 business flows unless a later approved development task explicitly authorizes them.

## Source of truth

Use this order when resolving requirements:

1. Approved HealthTrack AI business analysis.
2. Approved Android–Backend integration contract.
3. OpenAPI generated from the current integration branch.
4. ADRs and architecture documentation.
5. Database documentation.
6. Code.
7. Tests.

Report conflicts. Do not silently choose a lower-priority source or redefine a released contract.

## Shared approval boundary

Changes to API paths, DTO fields, shared enums, error codes, authentication, date/time, units, Health Connect sync, uploads, AI consent, reports, or breaking behavior require Android and Backend review through the contract-change process.

Architecture, security, privacy, database, dependency, and cross-owner decisions must be documented and approved before implementation. Internal naming or private refactoring that does not cross these boundaries may be decided locally.

## Ownership

- `backend/`, server persistence, authorization, provider adapters, Swagger, and server business rules: Backend Owner.
- `android/`, Compose UI, Android state, permissions, Health Connect access, local scheduling, and lifecycle: Android Owner.
- `docs/api/`, shared enums/errors, and integration behavior: shared ownership.
- `infra/`, root contracts, and integration CI: Backend Owner acting as Integration Owner, with Android review where Android is affected.

Do not edit another owner's implementation merely to make a build pass. Report the impact and fix the owning contract or ask the owner.

## Architecture constraints

- Backend is a modular monolith. Controller → application service → repository → Prisma → PostgreSQL.
- Controllers and DTOs contain no business logic. Controllers do not access Prisma directly.
- Modules do not query another module's tables directly; use an exported application/domain interface.
- Android follows Presentation → Domain → Data with MVVM/UDF. UI does not call Retrofit, Room, or Health Connect directly.
- External AI is called only by Backend after consent and data minimization.
- Health Connect access exists only on Android; Backend owns authorization, validation, idempotency, and persistence.

## Security and privacy

Health and medical data are sensitive. Enforce authenticated ownership on the server, private storage, least access, explicit consent, data provenance, sanitized logs, and minimum necessary AI context. Never log passwords, OTPs, tokens, medical files, or raw sensitive payloads.

Do not commit real secrets. Do not claim HIPAA compliance, medical-grade behavior, full security, or production readiness without evidence.

## Dependency and version governance

Use only dependencies recorded in project documentation and lockfiles. A new dependency needs a documented purpose, alternatives, security/maintenance impact, and approval. Do not silently upgrade major versions or use dynamic dependency versions.

## Contract changes

Open a contract change using the template in `docs/governance/collaboration-rules.md`. Breaking changes require explicit Backend and Android approval. Update the integration contract, changelog, OpenAPI, mocks, and tests together.

## Verification

Run the relevant lint, typecheck, tests, and build before claiming success. If a command was not run, report `NOT VERIFIED`. Do not disable tests, weaken expectations, ignore exceptions, or comment out code to make a build green.

## Prohibited destructive actions

Do not run destructive database resets, delete migrations, rewrite shared Git history, force-push shared branches, delete user data, or perform recursive cleanup without explicit approval and verified targets.

