# Backend Phase 1: run and integrate

Backend implementation candidate, 2026-11-25. Android review and joint acceptance
are pending. [CCR-002](../governance/contract-changes/CCR-002-auth-session.md)
contains exact decisions and errors; [OpenAPI](../api/openapi.json) is generated
from executable DTOs/controllers. Example serialization fixtures are in
[auth-contract.json](../../backend/test/fixtures/auth-contract.json).

## Run locally

Use the existing PostgreSQL/Silo Compose services and Foundation migrations.
No new migration or npm dependency is needed. Set DATABASE_URL as described in
getting-started. Provide two different locally generated random secrets with at
least 32 bytes through JWT_ACCESS_SECRET and JWT_REFRESH_SECRET. Do not use the
example placeholders. Secret generation may be done directly into process
environment without printing them:

```powershell
$env:JWT_ACCESS_SECRET = node -p "require('node:crypto').randomBytes(48).toString('hex')"
$env:JWT_REFRESH_SECRET = node -p "require('node:crypto').randomBytes(48).toString('hex')"
npm.cmd run build
npm.cmd run start
```

Run from backend/. Persist development secrets through an approved ignored local
configuration if sessions must survive restart; regenerating secrets invalidates
existing access tokens, OTP verification and recovery handles. There is no
default signing key. Health/Swagger can boot without secrets; Auth fails closed
with AUTH_CONFIGURATION_UNAVAILABLE if required keys are invalid.

Swagger: http://localhost:3000/api/docs. API: http://localhost:3000/api/v1.
Transport must be HTTPS outside local development.

## OTP limitation and verification

The configured OTP provider is still mock-only. It neither delivers SMS/email
nor prints or returns the code. Therefore a manual phone/email registration
cannot be completed just by looking at Swagger. Automated tests inject an
isolated capture provider and demonstrate the complete flow without an OTP
debug endpoint. Selecting real SMS/email delivery is the separate OQ-002 task.

From backend/:

```text
npm run lint
npm run typecheck
npm test
npm run test:auth:integration
npm run openapi:check
```

The PostgreSQL suite requires DATABASE_URL (or AUTH_TEST_DATABASE_URL) and permission
to create a schema. It creates a fresh auth_test_<random> schema, applies existing
migration SQL there, injects a schema-scoped Prisma client and runs real HTTP and
transaction tests. It does not migrate/reset/delete existing public-schema data.
Synthetic test schemas are intentionally retained; there is no automatic DROP
CASCADE cleanup. CI's database is ephemeral. The suite never logs connection
credentials, captured OTPs or issued session tokens.

## Android handoff notes

- Register/verify succeeds without a session; login is a separate step.
- Keep exactly the returned challenge handle. Registration is UUID; recovery is
  opaque ciphertext. Recovery uses forgot-password again after its displayed
  wait, not resend-otp. Read CCR-002 for generic cooldown acknowledgements.
- Serialize refresh. Strict reuse detection revokes the family when an old
  rotated token is reused, including a concurrent successful child. Do not retry
  an uncertain refresh blindly. A timeout is not itself proof of revocation.
- Logout CURRENT/ALL needs a valid Bearer credential; 204 has no JSON body.
- AUTH_ACCOUNT_UNVERIFIED after correct password may include a registration
  Challenge so an interrupted registration can resume.
- PATCH me accepts fullName only. Client userId/status/contact updates are rejected.
- OTP mock, per-process IP throttling and unverified Android integration are
  explicit limitations. Backend checks are not a production-readiness claim.

## Delivery status

Backend source, tests, generated contract and fixtures are local changes. No
push, deployment, Android modification or production-provider configuration is
included in this implementation task.
