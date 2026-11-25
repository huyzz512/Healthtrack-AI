# [CONTRACT] CCR-002 — Phase 1 Authentication & Session

Status: **BACKEND IMPLEMENTATION CANDIDATE — Android review pending — 2026-11-25**.

After the initial approval-gate report, the user explicitly instructed Backend
to proceed. This records authorization for Backend implementation; it does not
impersonate the Android reviewer or mark joint acceptance complete. The choices
below describe executable candidate behavior for collaborator review.

```text
UC/Feature: UC-01 login, UC-02 registration, supporting account/session lifecycle
Type: NON-BREAKING candidate addition to Foundation health; new auth behavior requires review
Current contract: Foundation policies and ten reserved paths
Proposed contract: Executable Backend candidate described below
Reason: User explicitly instructed Backend implementation after the gate report
Backend impact: Auth/users services, repositories, guards, DTOs and tests
Android impact: Client DTOs, states and mocks; collaborator owns implementation
Migration: None; existing tables and constraints retained
Backward compatibility: Existing health response, envelopes and canonical enums retained
Tests required: Unit/HTTP, PostgreSQL transactions, concurrency, ownership, OpenAPI drift
Backend approval: User authorized Backend implementation on 2026-11-25; decisions recorded below
Android approval: PENDING; no Android approval inferred from Backend authorization
```

## Sources and retained rules

Approved business analysis v0.2 UC-01/UC-02 and the released integration contract
are the sources. [Backend Phase 1](../../development/backend-phase-1.md) records
source hash and traceability. Retain Argon2id, access 15 minutes, hash-only rotating
refresh 30 days, password 8–30 characters with upper/lower/digit/special, six-digit
OTP with five-minute TTL/five failures, resend after 60 seconds at most three
times, and five login failures in 15 minutes imposing a 15-minute restriction.

## DTOs and endpoints

All paths use /api/v1. JSON success is wrapped once in { "data": ... }; 204 has no
body. Errors retain the existing envelope. Auth/profile responses are no-store.
Extra request fields are rejected.

| Type | Fields |
|---|---|
| Identity | Exactly one non-null string email or phone |
| Registration | Identity + fullName, password, confirmPassword |
| Login | Identity + password |
| Challenge | challengeId, expiresAt, resendAvailableAt, resendsRemaining |
| Session | accessToken, refreshToken, tokenType: "Bearer", accessTokenExpiresAt, refreshTokenExpiresAt |
| Me | id, fullName, nullable email/phone, nullable emailVerifiedAt/phoneVerifiedAt, createdAt, updatedAt |

Instants are UTC ISO 8601. Registration challenge IDs are UUIDs. Recovery handles
are opaque authenticated ciphertext: retain verbatim; do not pass them to
registration verify/resend. Me.id is a UUID.

Identity normalization trims and lowercases valid email; phone is ten ASCII
digits starting with 0. +84/interior separators are not supported. Full name is
trimmed and has 2–80 Unicode code points. Password is not trimmed/normalized:
8–30 Unicode code points, ASCII upper/lower/digit and a non-alphanumeric,
non-whitespace special character; confirmation must match exactly. DTO size
caps bound request validation work.

| Endpoint | Authorization / request | Success |
|---|---|---|
| POST /auth/register | Public; Registration | 201 Challenge; no session |
| POST /auth/verify-otp | Public; { challengeId: UUID, code: six-digit string } | 200 { verified: true }; proceed to login |
| POST /auth/resend-otp | Public; { challengeId: UUID } | 200 Challenge; registration only |
| POST /auth/login | Public; Login | 200 Session |
| POST /auth/refresh | { refreshToken: 43-character base64url string } | 200 Session |
| POST /auth/logout | Bearer; { scope: "CURRENT" or "ALL" } required | 204 |
| POST /auth/forgot-password | Public; Identity | 200 recovery Challenge, including unknown/ineligible identities |
| POST /auth/reset-password | Public; { challengeId: opaque handle, code, password, confirmPassword } | 204; no session |
| GET /users/me | Bearer | 200 Me |
| PATCH /users/me | Bearer; { fullName } only | 200 Me |

No owner ID, status, credential or email/phone change is accepted in PATCH.
Contact linking and authenticated password change remain outside this slice.

## Errors

Codes marked NEW belong to this candidate, pending Android review. Details are
null unless specified. Unexpected failures remain sanitized. Clients branch on
HTTP status/code, not English error messages.

| HTTP | Code | Meaning / details |
|---|---|---|
| 400 | VALIDATION_FAILED | Invalid/missing/extra fields; existing validationErrors where produced by the global pipe; no submitted values |
| 401 | AUTH_INVALID_CREDENTIALS | Same for unknown identity and wrong password |
| 401 | AUTH_TOKEN_EXPIRED | Expired validly signed access token |
| 401 | AUTH_TOKEN_INVALID (NEW) | Missing, malformed, revoked or invalid access session |
| 401 | AUTH_REFRESH_INVALID | Unknown, expired, revoked or reused refresh |
| 403 | AUTH_ACCOUNT_LOCKED | Temporary restriction includes retryAfterSeconds; administrative lock has null details |
| 403 | AUTH_ACCOUNT_UNVERIFIED (NEW) | Pending account after password proof; includes latest registration Challenge when available |
| 403 | AUTH_ACCOUNT_DISABLED (NEW) | Disabled account; login discloses only after password proof |
| 409 | AUTH_IDENTITY_UNAVAILABLE (NEW) | Existing contact, including pending/deleted identities under current unique constraints |
| 422 | AUTH_OTP_INVALID (NEW) | Wrong/invalid/consumed challenge; registration wrong-code details: attemptsRemaining |
| 422 | AUTH_OTP_EXPIRED (NEW) | Registration code expired |
| 422 | AUTH_OTP_ATTEMPTS_EXCEEDED (NEW) | Five registration failures used |
| 422 | AUTH_OTP_RESEND_LIMIT (NEW) | Three registration resends used |
| 429 | AUTH_RATE_LIMITED (NEW) | retryAfterSeconds and matching Retry-After header |
| 503 | AUTH_OTP_UNAVAILABLE (NEW) | Registration delivery failed; challengeId permits controlled resend |
| 503 | AUTH_CONFIGURATION_UNAVAILABLE (NEW) | Missing, placeholder, too-short or equal secrets |

Every invalid reset challenge (unknown, expired, consumed, wrong code, exhausted
or wrong purpose) returns 422 AUTH_OTP_INVALID with null details.

## Resolved Backend choices

### Registration and OTP

- Pending account, hash and challenge persist before delivery. Activation and
  consumption commit together. Verification does not issue a session.
- Resend keeps the same challenge ID, replaces hash, restarts TTL, resets failed
  attempts and increments resendCount. Last-send time is expiresAt minus five
  minutes; createdAt remains the initial request time.
- Duplicate registration never overwrites profile/password. Correct password
  for a pending account returns its latest challenge in AUTH_ACCOUNT_UNVERIFIED.
- Abandoned/resend-exhausted registrations can use password recovery. Successful
  reset for a pending account proves contact ownership, activates/verifies that
  contact and invalidates registration OTPs. It returns no session. This extends
  the initial draft and needs Android navigation/state review.
- Delivery is outside DB transactions, timeout-bounded. Failure invalidates only
  the still-current code; account remains pending. Failed resend consumes its
  allowance.

### Password recovery without migration

- Eligible users get PASSWORD_RESET rows; unknown/ineligible identities use a
  random decoy UUID without creating a user/OTP row.
- External handle encrypts real/decoy UUID and expiry with AES-256-GCM using a
  domain-derived key. Both have the same shape and invalid-reset errors.
- Every forgot response has expiry now + five minutes, resendAvailableAt now +
  60 seconds and resendsRemaining = 0. Request recovery again after that wait;
  resend-otp is registration-only. New real codes invalidate their predecessors.
- Requests in cooldown return decoy acknowledgements. Account-level issuance
  permits an initial code plus three further codes, separated by at least 60
  seconds, until a 15-minute quiet interval. Each code has five failed attempts.
- Delivery failure also returns a generic acknowledgement and invalidates the
  failed code. Real/decoy paths wait at least one second at the delivery stage;
  this mitigates but does not prove indistinguishable network timing.
- Successful reset consumes OTP, updates password, clears login counters and
  revokes all sessions atomically. Pending-account activation is described above.

### Session and concurrency

- JWT: HS256, issuer healthtrack-backend, audience healthtrack-app, sub user ID,
  sid family ID, iat/exp. Protected requests check current account and a live
  family; profile writes/logout recheck inside the transaction.
- Each login creates a family. Refresh is 32 random bytes in base64url; only its
  SHA-256 hash persists. Rotation atomically creates one child.
- Reuse commits complete family revocation before returning 401. Concurrent
  refresh or a lost successful response can force login again; Android must
  serialize refresh and avoid blind retries of uncertain refresh requests.
- Expiry rolls 30 days forward at rotation; no separate absolute family expiry.
- CURRENT logout revokes caller family; ALL revokes all current user families.
  Repeated logout using the revoked bearer returns 401. A later new login may
  create a new family. Temporary password lock restricts login only;
  administrative LOCKED/DISABLED and deleted accounts cannot use protected
  routes or refresh.
- Read-committed PostgreSQL transactions use per-account advisory locks for
  auth mutations; initial registration locks normalized identity. Users exports
  a domain unit-of-work. Auth queries its own tables only.
- 401 refresh rejection is terminal. Timeout/429/503 alone is not evidence of
  permanent revocation; Android owns handling of uncertain requests.

### Security and dependencies

- No new dependency. Argon2id uses memoryCost 19456 KiB, timeCost 2, parallelism 1.
  OTP uses HMAC-SHA-256 over domain, challenge ID and code with a server secret.
- JWT_ACCESS_SECRET and JWT_REFRESH_SECRET must be different, at least 32 bytes,
  and not example placeholders. The latter protects OTP and domain-separated
  recovery keys; refresh credentials are opaque, not JWTs.
- Process-local socket-IP throttle: register/forgot five per 15 minutes; refresh
  60 per minute; other Auth/profile operations 30 per minute per operation. Bound
  of 10,000 buckets. X-Forwarded-For is not trusted. Persistent OTP/account limits
  supplement it; a shared limiter/proxy policy is a production prerequisite.
- Query strings/sensitive DTOs are excluded from error logs. Mock OTP remains
  test-only delivery evidence; no fixed OTP/debug endpoint is exposed.

## Handoff

Generated OpenAPI documents this Backend candidate. Synthetic example fixtures
are in backend/test/fixtures/auth-contract.json. Backend verification is separate
from Android client/device testing and joint acceptance. Actual SMS/email
delivery remains OQ-002; the mock cannot demonstrate receipt. No production
release or joint Phase 1 completion is claimed.
