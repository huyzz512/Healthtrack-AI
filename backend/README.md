# Backend Foundation

NestJS modular-monolith foundation for HealthTrack AI. This package contains infrastructure wiring and module boundaries only; it does not implement UC-01 through UC-20.

## Commands

```text
npm ci
npm run lint
npm run typecheck
npm test
npm run build
npm run openapi:check
npm run start:dev
```

The process reads configuration from `../.env` when run from the repository root or from `backend/.env` when run inside this directory. Never commit real credentials.

Swagger is exposed at `/api/docs`; the Foundation health endpoint is `GET /api/v1/health`.

Database/integration verification additionally requires local PostgreSQL:

```text
npm run prisma:generate
npm run prisma:validate
npm exec prisma migrate deploy
npm exec prisma migrate status
npm run integration:verify
```

Regenerate the committed OpenAPI artifact with `npm run openapi:generate`. Private S3-compatible storage can be checked with `npm run storage:verify` while the local Silo service is running.
