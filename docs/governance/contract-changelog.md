# Contract changelog

## Foundation v0.1 — released 2026-11-20

- Established `/api/v1` root, response envelopes, trace identifiers, HTTP status semantics, initial error codes, shared enums, UTC/local-date rules, and explicit units.
- Established authenticated ownership, Health Connect, reminder, private file, AI consent, analytics, and report boundaries.
- Added the only executable Foundation endpoint: `GET /api/v1/health`.
- Added `DATABASE_UNAVAILABLE` and `INTERNAL_SERVER_ERROR` to support the approved health-check and sanitized internal-error behavior.
- Business term `MANUAL` maps to shared contract value `USER_INPUT`; no business meaning changes.
- Resolved CCR-001/OQ-012 by releasing the canonical cross-platform values `DataSource`, `NORMAL`, `CRITICAL`, and `REQUESTED`/`READY`.
- Released provenance values `USER_INPUT`, `HEALTH_CONNECT`, `IMPORTED`, and `SYSTEM_GENERATED` plus the explicit AI consent type/status values.
- Preserved Android forward compatibility through `UNKNOWN` fallbacks; no UC-01–UC-20 business endpoint was added.

This is the initial contract baseline. Breaking changes require explicit Backend and Android approval.

## Backend Phase 1 candidate — 2026-11-25

- CCR-002: user-directed Backend Auth/Users implementation, ten previously
  reserved paths, DTOs/errors and generated OpenAPI; Android review pending.
- Existing Foundation health behavior and canonical enums retained.
- No migration or dependency addition. Mock OTP and process-local IP throttling;
  joint acceptance and production readiness are not claimed.

