# ADR-008: Android–Backend contract governance

## Status

Accepted.

## Context

Two owners developing in parallel need stable endpoints, DTOs, enums, errors, time, units, auth, and sync semantics.

## Decision

Maintain a shared integration contract and generated OpenAPI. Shared or breaking changes use a documented request and require both Backend and Android approval before implementation.

## Alternatives

- Code-first unilateral changes: rejected because they create integration drift.
- Android mocks as independent truth: rejected; they must derive from the approved contract.

## Consequences

OpenAPI, Android mappings/mocks, changelog, and integration tests change together.

## Impact

Review workflow, CI, ownership, definition of ready/done, and release compatibility.

