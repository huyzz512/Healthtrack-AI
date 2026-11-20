# ADR-005: AI accessed only through Backend

## Status

Accepted.

## Context

Direct client calls would expose credentials and bypass ownership, consent, minimization, policy, and audit controls.

## Decision

Android calls Backend. Backend checks consent, requests authorized context through domain services, sanitizes/minimizes it, and calls an `AIProvider`.

## Alternatives

- Android calling OpenAI directly: rejected.
- AI provider querying PostgreSQL directly: rejected.

## Consequences

Backend is responsible for provider credentials and safety boundaries. Mock AI is the local default.

## Impact

AI consent contract, audit events, provider interfaces, logging, and privacy tests.

