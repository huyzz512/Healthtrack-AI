# Backend architecture

## Style

The Backend is a NestJS modular monolith. Modules deploy together but own explicit application and persistence boundaries.

```text
HTTP Controller
      |
Application Service
      |
Repository / Provider interface
      |
Infrastructure adapter
      |
Prisma / external SDK
```

Controllers map transport input/output. Application services orchestrate use cases and enforce business rules. Repository interfaces express domain persistence needs. Infrastructure adapters contain Prisma or provider-specific code.

## Modules

Foundation module boundaries:

- auth, users
- health-profile, medical-records
- body-metrics, vital-signs
- reminders, smart-alerts
- nutrition, mood
- health-sync
- ai-assistant
- activity
- analytics, reports

Cross-module access uses an exported application interface. A module must not query tables owned by another module directly.

## Common platform

- Config validation at startup.
- Global validation pipe.
- Request/trace identifier middleware.
- JSON structured logging with sensitive-data prohibition.
- Global exception filter producing the contract error envelope.
- Swagger rooted at `/api/docs` with application paths under `/api/v1`.
- Prisma isolated in `infrastructure/database`.

## Health behavior

The Foundation health endpoint checks PostgreSQL. A successful check returns the locked response. Dependency failure becomes a sanitized 503 error with trace ID.

## External providers

AI, files, OTP, notification, and PDF behavior depend on interfaces. Local defaults are mock/local adapters; production implementations are selected by configuration after approval.

