# Backend Phase 1 — Authentication & Users

Status: **BACKEND IMPLEMENTED — integration candidate; Android review pending**.
Date: 2026-11-25.

The user authorized Phase 1 and assigned this workstream Backend only, then
explicitly instructed implementation after the shared-gate report. The Backend
candidate is implemented with choices recorded in CCR-002. The collaborator owns
Android implementation/review/verification; no Android approval or joint phase
acceptance is inferred. See [run and handoff guide](backend-auth-handoff.md).

## Source evidence

The approved business analysis named by project governance was located and its
UC-01/UC-02 sections were read directly:

```text
File: HealthTrack_AI_Hoan_thien_phan_tich_v0.2.docx
Local source: C:/Users/nhath/Downloads/HealthTrack_AI_Hoan_thien_phan_tich_v0.2.docx
SHA256: 6167C1C9C82B300ED130390AFB275631114F45AFC68DFE4E2F830DEECEC84380
```

This identifies the reviewed copy; it is not a new approval or a portable archive.
Archive/link the source in the team's approved document system when available.
The revised deployment roadmap is supporting planning material. Where it lacks
details, the approved analysis and released integration contract take priority.

| Requirement | Source | Backend consequence |
|---|---|---|
| UC-01 is login; UC-02 is registration | Business analysis section titles | Use these IDs consistently in tickets and tests |
| Login with email or Vietnam phone | UC-01 BR2/BR3 and released auth contract | Normalize deterministically; reject unsupported identity formats |
| Generic invalid credentials | UC-01 E3 | Unknown user and wrong password share the public failure |
| Account state blocks login | UC-01 E4 | Pending, locked and disabled accounts cannot create sessions |
| Five failures / 15-minute window / 15-minute lock | Released contract | Atomic accounting, deterministic clock tests |
| Name, one contact, password and confirmation | UC-02 BR1–BR4 | Draft required fields; no silent omission of confirmation |
| Six-digit OTP, five-minute TTL, five failures, resend limits | Released contract; UC-02 BR5/BR6 | Atomic verify/resend and counters; previous OTP invalidated |
| Activate only after OTP, then navigate to login | UC-02 normal flow steps 9–10 | No automatic login on verification |
| Hash password/OTP; no secret logs | UC-01 BR1/BR6, UC-02 BR7/BR8 | Provider capture only in isolated tests |
| Actual OTP delivery in deployed environment | UC-02 Other Information 3 | Mock tests cannot establish deployed delivery acceptance |
| Supporting logout/recovery/password changes | Business analysis section 6.4 | Detailed wire/policy rules still need approval; do not invent released behavior |

## Concrete contract for review

[CCR-002](../governance/contract-changes/CCR-002-auth-session.md) documents the ten
implemented paths, DTOs, errors, OTP/session behavior and resolved Backend
decisions. Generated OpenAPI reflects this candidate. The integration contract
separates the released Foundation baseline from the new candidate; Android
review and joint release remain pending.

## Initial schema and implementation gap assessment (kickoff)

This table records the gaps found before implementation; resolutions follow it.

| Current evidence | What can be reused | Gap / review needed |
|---|---|---|
| AuthModule and UsersModule are empty shells | Module boundaries | Application services, repositories, controllers and tests |
| User: pending status, contact verification dates, soft delete | Pending registration and self-profile | Global unique contacts are stricter than active-only business uniqueness; abandoned pending account recovery unresolved |
| UserCredential: passwordHash, failedLoginCount/window, lockedUntil | Password verification and lockout | Atomic transitions and exact lock boundary tests |
| RefreshToken: hash, familyId, unique parentTokenId, expiry/revocation | Rotation ancestry and family revocation | Serialize family mutation; prove logout/reuse cannot race a child into an active state |
| OtpChallenge: purpose, hash, counters, expiry/consumption/invalidation | Registration challenge lifecycle | No lastSentAt or root-request link; proposed stable ID needs explicit timestamp semantics/design |
| OtpChallenge.userId is required | Real-user OTP challenges | Synthetic recovery challenges cannot simply use this model; assess migration or an alternative approved design |
| OTPProvider and non-echoing mock are available | Delivery abstraction and test injection | Delivery timeout/failure behavior; production provider remains OQ-002 |
| @nestjs/jwt and argon2 are already pinned | JWT and password hashing | Approve key config, parameters, token claims and verification policy |
| No request-throttle persistence abstraction | Account lockout and OTP counters exist | Cross-account/IP throttles require approved limits, store and proxy policy |
| Exception filter logs request.originalUrl | Existing envelope | Before auth rollout, redact query strings and never attach DTOs/secrets to log payloads |

Implemented resolutions: existing tables retained; unique contacts remain
reserved even for pending/deleted accounts. Pending recovery requires password
proof to recover the registration handle or OTP contact proof through reset.
Opaque encrypted recovery handles conceal decoy UUIDs without synthetic users
or schema changes. Registration resend time is derived from expiry; existing
counters enforce limits. Account-keyed advisory locks coordinate transactions.
No new migration/dependency, reset or weakened unique constraint was needed.

## Backend ownership and implementation boundaries

`AuthController -> AuthApplicationService -> repository/provider interfaces ->
Prisma adapters`. Users exposes an application interface for identity/profile
operations; Auth does not directly query Users-owned tables. The exported domain
unit-of-work binds both repository adapters to one transaction and serializes
account mutations. External OTP delivery occurs after transaction commit.

No Android source, Gradle configuration or tests are assigned to this workstream.
Backend supplies the candidate contract, generated OpenAPI, example payloads and
integration instructions. The collaborator reviews the shared contract and
verifies Android against it.

## Delivery sequence

| ID | Backend deliverable | Entry condition | Exit evidence |
|---|---|---|---|
| BE-P1-01 | Source traceability, gap assessment and CCR-002 | User kickoff | DONE |
| BE-P1-02 | Backend security/transaction design and scope decisions | Explicit follow-up to implement | DONE for Backend candidate; Android approval pending |
| BE-P1-03 | Clock, hashing, token and repository adapters | Candidate design | IMPLEMENTED; tests |
| BE-P1-04 | Register, verify, resend, login and account-state handling | BE-P1-03 | IMPLEMENTED; unit/HTTP/PostgreSQL tests |
| BE-P1-05 | Rotation, reuse revocation, guard and logout | BE-P1-03 | IMPLEMENTED; concurrency tests |
| BE-P1-06 | GET/PATCH me and recovery | Candidate design | IMPLEMENTED; ownership/purpose/reset tests |
| BE-P1-07 | OpenAPI, fixtures and handoff | Backend implementation | IMPLEMENTED; local verification below |
| BE-P1-08 | Joint acceptance and actual OTP receipt | Android review and provider decision | PENDING; not claimed complete |

Owner for BE work: Backend Owner. Shared CCR reviewer: Android Owner. Passing
Backend checks alone does not close joint Phase 1 acceptance.

## Kickoff verification record (before implementation)

Kickoff baseline checks are recorded below after execution. These verify the
existing Foundation backend, not unimplemented Phase 1 behavior.

- `npm.cmd run lint`: PASS.
- `npm.cmd run typecheck`: PASS.
- `npm.cmd test`: PASS — pretest production build, 3 suites / 12 existing tests.
- `npm.cmd run openapi:check`: PASS — includes a further production build.
- Initial OpenAPI check failed because Git checked out CRLF while the generator
  emits LF (`git ls-files --eol` reported `i/lf w/crlf`). The Backend generator
  comparison now normalizes CRLF only, retaining exact content comparison.
  Rechecking passed without modifying the committed API artifact or its meaning.
- Real PostgreSQL Auth/session integration at kickoff: NOT VERIFIED; subsequent
  implementation results are recorded below.
- Android: NOT VERIFIED by this workstream; owned by collaborator.
- Production OTP delivery: NOT VERIFIED; provider not selected.

These results are local checks on 2026-11-25. GitHub-hosted CI has not been rerun
for this kickoff. Existing user DOCX output was left untouched.

Implementation scenarios include wrong/expired/consumed OTP; five-entry
and resend boundaries; transaction rollback after failed activation; failed-login
window expiry; unknown accounts; concurrent refresh/reuse/logout/reset; expired
and tampered JWTs; unavailable dependencies; cross-account access; and absence
of passwords/OTPs/tokens in responses other than the intended session response,
logs or fixtures. Tests use isolated accounts and databases, never destructive
resets of the user's Foundation database.

## Backend implementation verification — 2026-11-25

| Check | Result |
|---|---|
| npm run lint | PASS |
| npm run typecheck | PASS |
| npm test (includes production build) | PASS — 4 suites, 45 tests |
| npm run test:auth:integration (includes production build) | PASS — 9 PostgreSQL tests, zero skipped |
| npm run openapi:check (includes production build) | PASS |
| npm exec prisma format / npm run prisma:validate | PASS; no schema-content change or migration |
| git diff --check | PASS |
| Android implementation/tests | NOT VERIFIED — owned by collaborator; no Android files changed |
| GitHub-hosted CI for these changes | NOT VERIFIED — workflow updated locally; no push |
| Real SMS/email OTP | NOT VERIFIED — provider remains mock |

The initial integration attempt lacked DATABASE_URL. Rerunning against the
documented local Compose database in fresh isolated schemas passed. Both runs
that created schemas used synthetic records only; the schemas retained for
inspection are auth_test_772725c1d9ed423ca6e93665acdb7320 and
auth_test_4bf1ee88bd504543aab1f640bda3c50b. Public-schema user data was not changed.

Implementation evidence covers registration/verification/login, ownership,
profile updates, password reset, provider failure, expired/tampered credentials,
lockout/throttling, rotation/reuse/logout races and transaction rollback. No new
dependency was installed. See the handoff guide for required local signing
configuration, mock delivery limitations and Android review points.
