# ADR-003: PostgreSQL primary database

## Status

Accepted.

## Context

The system requires relational ownership, constraints, transactions, auditability, and time-based queries.

## Decision

Use PostgreSQL as server primary storage through Prisma. Use UUID identifiers and UTC instants.

## Alternatives

- Document databases: rejected because no requirement justifies weaker relational constraints.
- Multiple databases: rejected as unnecessary Foundation complexity.

## Consequences

Schema changes use reviewed migrations. Medical binaries remain outside PostgreSQL.

## Impact

Local Docker Compose, Backend persistence, CI services, and backup planning target PostgreSQL.

