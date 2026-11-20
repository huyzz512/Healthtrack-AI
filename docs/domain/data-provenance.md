# Data provenance

Every health measurement or activity record carries owner, source, event/measurement time, creation/update time, and source record identifiers when available.

Shared contract sources:

- `USER_INPUT` — business-analysis term `MANUAL` maps here.
- `HEALTH_CONNECT`.
- `IMPORTED`.
- `SYSTEM_GENERATED`.

The shared enum name is `DataSource`. Prisma persists the same enum name and values. The historical persistence-only `SourceType.OTHER` value was migrated to `DataSource.IMPORTED` during Foundation alignment; no source record identifiers or health values are modified by that migration.

Imported/Health Connect records are not directly editable as user input. Display and reporting preserve source application/device where available. Deduplication keys and sync audit data must make repeated synchronization idempotent.

