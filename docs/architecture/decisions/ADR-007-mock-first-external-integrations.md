# ADR-007: Mock-first external integrations

## Status

Accepted.

## Context

Foundation must run without paid AI, SMS, email, production S3, or production PDF services.

## Decision

Define provider interfaces and select mock/local implementations by default. Production adapters are configuration-driven and require explicit dependency/config approval.

## Alternatives

- Require real providers for local development: rejected because it slows and destabilizes development.
- Hard-code provider calls in application services: rejected because it prevents testing and replacement.

## Consequences

Mocks must be clearly labeled and never presented as production behavior.

## Impact

Configuration, dependency injection, tests, documentation, and local onboarding.

