# Android–Backend integration contract

Status: **Foundation v0.1 released — 2026-11-20**, shared ownership.

## Transport and root

- HTTPS is mandatory outside local development.
- All application endpoints are rooted at `/api/v1/`.
- JSON uses UTF-8.
- Authenticated endpoints derive ownership from the authenticated principal, never a client-supplied owner ID.

## Success envelopes

Single resource:

```json
{
  "data": {}
}
```

Paginated list:

```json
{
  "data": [],
  "meta": {
    "page": 1,
    "pageSize": 20,
    "total": 0
  }
}
```

## Error envelope

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable message",
    "details": null,
    "traceId": "request-correlation-id"
  }
}
```

Android branches on HTTP status and `error.code`; it does not parse messages for business logic.

## HTTP semantics

Use 200, 201, and 204 for successful operations; 400 invalid request, 401 unauthenticated, 403 unauthorized, 404 not found, 409 conflict, 413 payload too large, 422 business validation, 429 rate limit, 500 unexpected server error, and 503 unavailable dependency.

## Foundation error codes

```text
AUTH_INVALID_CREDENTIALS
AUTH_TOKEN_EXPIRED
AUTH_REFRESH_INVALID
AUTH_ACCOUNT_LOCKED
VALIDATION_FAILED
FORBIDDEN_RESOURCE
RESOURCE_NOT_FOUND
FILE_TYPE_UNSUPPORTED
FILE_TOO_LARGE
FILE_STORAGE_UNAVAILABLE
HEALTH_METRIC_INVALID
HEALTH_SYNC_DUPLICATE
HEALTH_SYNC_REJECTED
HEALTH_CONNECT_CONSENT_REQUIRED
AI_CONSENT_REQUIRED
AI_PROVIDER_UNAVAILABLE
REMINDER_INVALID_SCHEDULE
REPORT_NOT_READY
REPORT_GENERATION_FAILED
VERSION_CONFLICT
DATABASE_UNAVAILABLE
INTERNAL_SERVER_ERROR
```

Unknown server errors map to an internal code without exposing sensitive details.

## Time and dates

- Instants are ISO 8601 UTC, for example `2026-11-17T03:30:00Z`.
- Calendar dates use `YYYY-MM-DD`.
- Backend stores instants in UTC; Android converts to the device/user timezone for display.
- Reminder schedules that depend on wall-clock behavior must carry an IANA timezone identifier when contracted.

## Units

Units are explicit per DTO. Semantic fields are preferred where unambiguous (`heartRateBpm`, `weightKg`); reusable quantity structures may use `{ value, unit }`. A DTO may not switch representation after approval without a contract change.

Canonical storage units include cm, kg, kg/m², bpm, mmHg, °C, breaths/minute, percent, steps, minutes, millilitres, and kcal. Fahrenheit input converts to Celsius before server persistence when supported.

## Foundation shared enums

These wire values are canonical. Persistence and Android models use the same names, and Android maps any future unknown value to `UNKNOWN` without crashing. The enums reserve terminology for later business endpoint contracts; they do not make those endpoints executable in Foundation.

```text
DataSource: USER_INPUT | HEALTH_CONNECT | IMPORTED | SYSTEM_GENERATED
MoodType: HAPPY | NORMAL | SAD | STRESSED | ANXIOUS
AlertSeverity: INFO | WARNING | CRITICAL
ReportStatus: REQUESTED | GENERATING | READY | FAILED
ConsentType: AI_HEALTH_CONTEXT | AI_MEDICAL_DOCUMENT | AI_REPORT_SUMMARY
ConsentStatus: GRANTED | REVOKED
```

`MANUAL` from the business analysis maps to `USER_INPUT`. `CRITICAL` is an informational severity label only; it does not claim diagnosis, medical-grade triage, emergency monitoring, or emergency dispatch.

## Authentication foundation

- Access token: Bearer JWT, 15-minute lifetime.
- Refresh token: 30-day lifetime, rotated on refresh, stored only as a hash on Backend.
- Android uses a secure token-storage abstraction and never logs tokens.
- Register and login support either normalized email or Vietnamese phone-number policy from Business Analysis v0.2.
- Passwords are 8–30 characters with upper, lower, digit, and special-character requirements; Backend hashes with Argon2id.
- Registration OTP is six digits, valid for five minutes, at most five failed entries; resend after 60 seconds, at most three resends per registration request, invalidating the previous OTP.
- Five failed login attempts within 15 minutes temporarily restrict login for 15 minutes.

Prepared endpoint paths (business flows are not implemented by Foundation):

```text
POST /api/v1/auth/register
POST /api/v1/auth/verify-otp
POST /api/v1/auth/resend-otp
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
GET   /api/v1/users/me
PATCH /api/v1/users/me
```

## Foundation health endpoint

`GET /api/v1/health` returns 200 only when the database check succeeds:

```json
{
  "data": {
    "status": "ok",
    "database": "connected",
    "version": "foundation-v0.1"
  }
}
```

Database unavailability returns HTTP 503 using the error envelope.

## Health Connect boundary

Android owns availability, permissions, reads, mapping, local cache, WorkManager, retry, and device integration. Backend owns authorization, validation, deduplication, idempotency, and persistence. Repeated sync of the same source record must not create duplicates.

Final sync DTOs require a separate shared contract approval before UC-14 implementation.

## Reminder boundary

Backend persists configuration and validates business rules. Android schedules local notifications and handles device permissions. Saving a reminder may succeed while local notification permission is unavailable; Android exposes that state.

## Medical files

Allowed formats are PDF, JPG, JPEG, and PNG, maximum 10 MB per file. Objects are private. Android never receives MinIO/S3 credentials. Database records contain metadata, ownership, and object keys only. Download is authorized API access or a later-approved short-lived signed URL.

## AI consent and data access

Android never calls the external AI provider. Backend verifies `AI_HEALTH_CONTEXT` consent, obtains data through authorized domain services, minimizes and sanitizes context, and invokes `AIProvider`. No health context is sent when the user asks in general-information mode.

AI content is labeled as AI-generated, does not diagnose, prescribe, alter medication, or update source health data. Medical documents are excluded unless explicitly selected.

## Analytics and reports

Backend aggregates; Android renders. Missing data remains missing rather than becoming zero or generated sample data. Backend creates PDFs with source, unit, time range, creation time, and a separate label for optional AI summaries.

## Compatibility

Android must safely handle unknown enum values. Removing/renaming fields, changing types/semantics, changing auth, errors, time, or units is breaking. Adding an optional field is non-breaking only after Android compatibility is confirmed.

Foundation v0.1 is the initial released contract baseline. Later changes follow the contract-change process and changelog in `docs/governance/`.

