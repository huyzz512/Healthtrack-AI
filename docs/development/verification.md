# Foundation verification

Foundation changes are accepted only when the affected checks below pass without disabled tests or weakened expectations.

## Static and generated contract

From `backend/` with `DATABASE_URL` set:

```text
npm run prisma:generate
npm run prisma:validate
npm run lint
npm run typecheck
npm test
npm run build
npm audit --omit=dev --audit-level=high
npm run openapi:check
```

`openapi:check` fails when generated Swagger differs from the committed `docs/api/openapi.json`. Contract alignment tests compare released shared enum values with Prisma; Android tests verify known and unknown-safe mappings.

## PostgreSQL and provider integration

With `postgres` and `minio` healthy under Docker Compose:

```text
npm exec prisma migrate deploy
npm exec prisma migrate status
npm run integration:verify
npm run storage:verify
```

The integration check boots the real application, queries PostgreSQL through the health endpoint, and loads Swagger. Storage verification creates a temporary object in the private local bucket, reads it, and deletes it in a `finally` block.

## Android

From `android/`:

```text
gradlew.bat --no-daemon assembleDebug testDebugUnitTest lintDebug
```

CI uses the equivalent Linux Gradle Wrapper command with JDK 17.

## Continuous integration

Backend Phase 1 adds `npm run test:auth:integration` after build. It exercises
real PostgreSQL HTTP flows, transaction rollback and auth races inside a new
isolated `auth_test_<random>` schema. It requires DATABASE_URL (or
AUTH_TEST_DATABASE_URL) and schema-create permission. Synthetic schemas are
retained intentionally; no existing data is reset or deleted. The Backend CI
job runs this suite against its ephemeral PostgreSQL service.

`.github/workflows/foundation-ci.yml` runs two independent jobs:

- Backend/contract/PostgreSQL: locked install, Prisma generation and migration, lint, typecheck, tests, build, production dependency audit, OpenAPI drift, database health, and Swagger integration.
- Android: Gradle Wrapper build, unit tests, and lint on JDK 17.

Production providers, production infrastructure, device instrumentation, medical thresholds, and UC-01 through UC-20 acceptance remain outside Foundation v0.1.
