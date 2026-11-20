# ADR-001: Modular monolith Backend

## Status

Accepted.

## Context

Two developers need clear domain boundaries without distributed-system operational cost.

## Decision

Use one NestJS deployable organized into explicit modules with Controller → Application Service → Repository → Prisma layering and interface-based cross-module collaboration.

## Alternatives

- Microservices: rejected as unnecessary operational and contract overhead.
- Unstructured monolith: rejected because it weakens ownership and testability.

## Consequences

All modules share one runtime and database but may not bypass module ownership. Future extraction remains possible only through a new approved ADR.

## Impact

Backend structure, testing, Prisma ownership, CI, and review rules follow modular boundaries.

