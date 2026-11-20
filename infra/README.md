# Infrastructure

Local infrastructure is defined by `docker-compose.yml` at the repository root. Production deployment is outside the Foundation scope.

## Services

- PostgreSQL 18.7 on local port 5432 by default.
- Silo pinned to `docker.io/pgsty/silo:RELEASE.2026-11-12T00-00-00Z`, exposed through the MinIO-compatible S3 API on port 9000 and embedded console on port 9001 by default.

Copy `.env.example` to `.env` and replace local placeholders before starting services. `.env` is ignored by Git.

```text
docker compose config
docker compose up -d postgres minio
docker compose ps
```

Do not use the example credentials outside local development. Volumes are persistent; `docker compose down` does not delete them. Volume deletion is a destructive action and is not part of normal project commands.

PostgreSQL and object-storage ports bind to `127.0.0.1` only. Silo is a maintained MinIO-compatible community fork selected for local Foundation work after the approved legacy MinIO registry stopped anonymous pulls. It preserves the S3 API and `MINIO_*` configuration surface used by the project. Production storage remains an independently reviewed S3-compatible adapter and deployment decision.

The Backend runs outside Docker during Foundation development and connects through `DATABASE_URL` and MinIO environment variables.

