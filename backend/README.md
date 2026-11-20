# Backend Foundation

NestJS modular-monolith foundation for HealthTrack AI. This package contains infrastructure wiring and module boundaries only; it does not implement UC-01 through UC-20.

## Commands

```text
npm ci
npm run lint
npm run typecheck
npm test
npm run build
npm run start:dev
```

The process reads configuration from `../.env` when run from the repository root or from `backend/.env` when run inside this directory. Never commit real credentials.

Swagger is exposed at `/api/docs`; the Foundation health endpoint is `GET /api/v1/health`.
