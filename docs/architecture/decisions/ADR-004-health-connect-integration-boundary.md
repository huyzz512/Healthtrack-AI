# ADR-004: Health Connect integration boundary

## Status

Accepted.

## Context

Health Connect is device-local and permission-controlled; Backend cannot access it directly.

## Decision

Android owns availability, permission, read, mapping, cache, WorkManager, retry, and device integration. Backend owns authenticated validation, deduplication, idempotency, and persistence.

## Alternatives

- Backend directly accessing Health Connect: technically invalid and rejected.
- Treating all imported values as user input: rejected because provenance would be lost.

## Consequences

Sync DTOs require stable source identifiers and shared review. The app must tolerate unavailable/denied/no-data states.

## Impact

Android permissions, Room cache, sync protocol, database provenance, and integration tests.

