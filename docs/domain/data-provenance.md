# Data provenance

Every health measurement or activity record carries owner, source, event/measurement time, creation/update time, and source record identifiers when available.

Shared contract sources:

- `USER_INPUT` — business-analysis term `MANUAL` maps here.
- `HEALTH_CONNECT`.
- `IMPORTED`.
- `SYSTEM_GENERATED`.

Imported/Health Connect records are not directly editable as user input. Display and reporting preserve source application/device where available. Deduplication keys and sync audit data must make repeated synchronization idempotent.

