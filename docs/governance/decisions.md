# Decision register

| ID | Decision | Status | Source |
|---|---|---|---|
| D-001 | One monorepo for Android, Backend, infrastructure, and documentation | Approved | Foundation Governance v1.0 |
| D-002 | Backend is a modular monolith | Approved | Foundation Governance v1.0 / ADR-001 |
| D-003 | Android is native Kotlin + Compose with package-based boundaries in a single `:app` module for Foundation | Approved | User approval / ADR-002 |
| D-004 | PostgreSQL is the primary database | Approved | Foundation Governance v1.0 / ADR-003 |
| D-005 | Health Connect access remains on Android | Approved | Business Analysis v0.2 / ADR-004 |
| D-006 | External AI is accessed only through Backend | Approved | Business Analysis v0.2 / ADR-005 |
| D-007 | Medical files use private S3-compatible object storage | Approved | Business Analysis v0.2 / ADR-006 |
| D-008 | External integrations are mock-first | Approved | Foundation Governance v1.0 / ADR-007 |
| D-009 | Android–Backend contract changes require dual review | Approved | Foundation Governance v1.0 / ADR-008 |
| D-010 | Android application ID is `com.healthtrackai.app` | Approved | User approval |
| D-011 | Android baseline is minSdk 28, compileSdk 36, targetSdk 36 | Approved | User approval |
| D-012 | Backend uses npm, ESM, and Jest | Approved | User approval |
| D-013 | Android JSON mapping uses Kotlinx Serialization | Approved | User approval |
| D-014 | Access tokens expire after 15 minutes; refresh tokens after 30 days with rotation and hashed server storage | Approved for contract foundation | User approval |
| D-015 | Unit representation is hybrid and must be explicit per DTO | Approved | User approval / Business Analysis v0.2 |
| D-016 | Database-unavailable health checks return HTTP 503 error envelope | Approved | User approval |
| D-017 | Initial Prisma migration requires a separate schema approval | Approved | User approval |
| D-018 | Local Foundation would use legacy MinIO Quay image `RELEASE.2025-09-07T16-13-09Z` | Superseded because the registry rejected anonymous pulls | User approval / verification result |
| D-019 | Local Foundation uses maintained Silo image `docker.io/pgsty/silo:RELEASE.2026-11-12T00-00-00Z` through its MinIO-compatible S3 surface | Approved | User approval |
| D-020 | Backend uses `@aws-sdk/client-s3@3.1169.0` for the Silo/S3-compatible adapter instead of the vulnerable MinIO JavaScript SDK dependency | Approved | User approval / npm audit |
| D-021 | Backend test foundation pins Jest `29.7.0` instead of Jest 30.5.2 to remove the audited vulnerable tooling chain | Approved | User approval / npm audit |
| D-022 | Prisma Foundation schema with 25 models and 21 enums is approved for initial migration on the local Foundation database | Approved | User approval, 2026-11-19 |
| D-023 | Foundation external capabilities use DI-owned interfaces; AI, OTP, notification, and PDF default to non-echoing mocks while private local object storage uses the AWS SDK against Silo | Approved by ADR-006/ADR-007 and implemented | Foundation Governance v1.0 / Phase H |
| D-024 | Android Foundation pins AGP 9.5.1, Gradle 9.9.1, Kotlin/Compose Compiler 2.4.21, KSP 2.3.14, Java target 17, SDK 36, Compose BOM 2026.06.01, Hilt 2.60.1, Room 2.8.5, Health Connect 1.1.0, Retrofit 3.0.0, OkHttp 4.12.1, Coroutines 1.11.0, and Serialization 1.11.0 | Approved | User approval, 2026-11-19 |
| D-025 | Keep compileSdk/targetSdk 36 for the approved Foundation checkpoint. Defer the optional AndroidX Hilt Navigation Compose 1.4.0 adapter because its published AAR requires compileSdk 37 and upgrades Lifecycle transitively; use Dagger Hilt core without the unused navigation adapter | Approved under the user's standing approval of the recommended Foundation corrections; verified by AAR metadata | Android build verification, 2026-11-19 |

