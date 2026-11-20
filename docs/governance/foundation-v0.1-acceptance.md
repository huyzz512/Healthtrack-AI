# Foundation v0.1 acceptance

Acceptance date: 2026-11-20
Scope: repository/tooling, governance, architecture, initial contract, persistence, provider abstractions, placeholder Android UI, CI, and test foundations only.

## Acceptance result

| Area | Evidence | Result |
|---|---|---|
| Governance and ownership | ADR-001–ADR-008, decision register, CCR-001, ownership and collaboration rules | PASS |
| Shared contract | Released integration contract, canonical enum registry, Android unknown fallbacks, contract tests | PASS |
| OpenAPI | Generated `docs/api/openapi.json` and drift check | PASS |
| Backend | ESLint, strict TypeScript, 3 Jest suites/12 tests, production build | PASS |
| Dependency audit | Patched Effect override; npm production audit | PASS — 0 vulnerabilities |
| PostgreSQL | 2 migrations applied; Prisma schema valid and migration status current | PASS |
| Runtime integration | Real PostgreSQL health endpoint and Swagger document | PASS |
| Provider boundaries | AI/OTP/notification/PDF tests and private S3 write/read/delete | PASS |
| Android | Debug APK assembly, unit tests, and lint; 61 Gradle tasks successful | PASS |
| CI definition | Backend/PostgreSQL/contract and Android jobs in `foundation-ci.yml` | PASS |
| GitHub-hosted CI | Run `44735597051` for commit `56cd134` | PASS — Backend and Android |

## Contract alignment

CCR-001 resolves OQ-012. The released vocabulary is `DataSource`, `NORMAL`, `CRITICAL`, `REQUESTED`/`READY`, explicit provenance values, and explicit AI consent values. Migration `20261120090000_align_foundation_contract_enums` preserves existing rows while renaming PostgreSQL enum labels and adding `SYSTEM_GENERATED`.

## Verification executed locally

On 2026-11-20:

- Prisma generate/validate: passed.
- Prisma migrate deploy/status against PostgreSQL 18.7: 2 migrations applied and schema up to date.
- Backend lint/typecheck/build: passed.
- Jest: 3 suites, 12 tests passed.
- Locked install and `npm audit --omit=dev --audit-level=high`: passed, 0 vulnerabilities.
- Generated OpenAPI drift check: passed.
- Database health endpoint and Swagger runtime verification: passed.
- Silo private-bucket write/read/delete verification: passed.
- Android `assembleDebug testDebugUnitTest lintDebug`: `BUILD SUCCESSFUL`, 61 tasks.
- GitHub-hosted Foundation CI run `44735597051`: Backend/PostgreSQL/contract and Android jobs passed.

## Explicit non-goals and residual items

- UC-01 through UC-20 business flows are not implemented by Foundation.
- Production AI, OTP, notification, PDF, storage, deployment, and secret-management choices remain deferred.
- Smart-alert medical thresholds remain blocked pending an approved medical expert and must not be inferred from this baseline.
- Production retention periods, device/vendor matrix, branding/localization, and food-provider decisions remain open without blocking Foundation.
- No production-readiness, regulatory-compliance, HIPAA, diagnostic, emergency-monitoring, or medical-grade claim is made.
