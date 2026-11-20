# HealthTrack AI

HealthTrack AI is a foundation-stage personal health management system composed of an Android application, a NestJS backend, PostgreSQL, and S3-compatible object storage.

Foundation v0.1 contains the verified architecture, contracts, bootable shells, persistence baseline, provider abstractions, placeholder UI, CI, and test foundations. It does not claim production readiness, medical-grade behavior, regulatory compliance, or completion of UC-01 through UC-20.

## Foundation commands

See [`docs/development/getting-started.md`](docs/development/getting-started.md) for setup and [`docs/development/verification.md`](docs/development/verification.md) for the complete quality gates. The released API contract is [`docs/api/integration-contract.md`](docs/api/integration-contract.md), with generated OpenAPI at [`docs/api/openapi.json`](docs/api/openapi.json).

## Repository layout

- `android/` — native Android application foundation.
- `backend/` — NestJS modular-monolith foundation.
- `infra/` — local infrastructure notes and configuration.
- `docs/` — governance, architecture, contracts, security, and development documentation.
- `scripts/` — cross-platform helper scripts where justified.
- `.github/workflows/` — continuous-integration workflows.

Foundation acceptance and known limitations are recorded in [`docs/governance/foundation-v0.1-acceptance.md`](docs/governance/foundation-v0.1-acceptance.md).

