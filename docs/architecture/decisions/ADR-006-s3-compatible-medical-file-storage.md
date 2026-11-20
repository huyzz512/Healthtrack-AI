# ADR-006: S3-compatible medical file storage

## Status

Accepted.

## Context

Medical documents are large sensitive objects and do not belong as PostgreSQL binary fields.

## Decision

Store private objects through a `FileStorage` interface. Use Silo's MinIO-compatible S3 surface locally and an approved S3-compatible service in production. PostgreSQL stores metadata, object key, and ownership. The Backend adapter uses the AWS SDK for JavaScript S3 client so the storage boundary does not depend on the withdrawn MinIO JavaScript SDK.

## Alternatives

- PostgreSQL binary storage: rejected.
- Permanent public URLs: rejected for privacy and authorization reasons.

## Consequences

Upload and metadata updates require consistency/cleanup behavior. Downloads remain authorized or use later-approved short-lived signed URLs.

## Impact

Docker Compose, medical-record module, audit, object retention, and integration tests.

