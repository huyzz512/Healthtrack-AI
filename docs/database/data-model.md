# Database foundation

Status: **APPROVED FOR INITIAL LOCAL MIGRATION — 2026-11-19**  
Schema: `backend/prisma/schema.prisma`  
Source: approved Business Analysis v0.2 and Foundation integration contract.

The user approved creation and local application of the initial migration on 2026-11-19 under D-017 and D-022.

## Design goals

- PostgreSQL is the primary store; Prisma owns the server persistence mapping.
- Every user-owned health record carries `userId`; repositories must always scope access to the authenticated owner.
- Health measurements and workouts retain event time, source type, original source identifier, source application, and source device when available.
- Health Connect imports are idempotent through unique source/client identifiers.
- Medical objects and generated reports remain private in S3-compatible storage; PostgreSQL stores metadata and object keys only.
- Passwords, OTPs, and refresh tokens are represented only by hashes.
- AI consent is explicit; AI messages never modify source health records.
- Audit metadata must be sanitized and must not contain passwords, OTPs, tokens, files, or raw sensitive payloads.

## Ownership map

| Module | Primary models |
|---|---|
| Auth / Users | `User`, `UserCredential`, `RefreshToken`, `OtpChallenge` |
| Health Profile | `HealthProfile` |
| Medical Records | `MedicalDocument` |
| Body Metrics | `BodyMetric` |
| Vital Signs | `VitalSign` |
| Reminders | `Reminder`, `ReminderOccurrence`, `MedicationSchedule`, `Appointment`, `WaterIntakeLog` |
| Smart Alerts | `SmartAlert` |
| Nutrition / Mood | `FoodEntry`, `MoodEntry` |
| Health Sync | `HealthSyncRecord` |
| Activity | `ActivityGoal`, `WorkoutSession`, `ExerciseRecommendation` |
| AI Assistant | `AIConversation`, `AIMessage`, `UserConsent` |
| Reports | `Report` |
| Governance | `AuditLog` |

Cross-module consumers use exported application/domain interfaces and do not query another module's tables directly.

## Canonical representations

- Instants use PostgreSQL `timestamptz(6)` and are exchanged as ISO 8601 UTC.
- Calendar-only values use PostgreSQL `date`.
- Body metrics use explicit canonical fields: centimetres, kilograms, and kg/m².
- Vital signs use explicit canonical fields: bpm, mmHg, Celsius, percent, and breaths/minute.
- Water is stored in millilitres; nutrition and workout energy use kcal.
- Flexible reminder recurrence remains `schedule` JSON until the shared reminder DTO is separately approved. Backend validation must enforce the later contract.

## Application-enforced invariants

Prisma cannot express every business check. The application layer and tests must enforce:

- a user has at least one normalized identifier (`email` or `phone`);
- email/phone uniqueness applies to active registration semantics;
- a body/vital row contains at least one measurement;
- numeric health, water, nutrition, goal, and workout values are within approved ranges;
- `endDate`/`periodTo`/`endedAt` is not earlier than its start value;
- medication and appointment rows have the same owner as their parent reminder;
- reminder schedule JSON matches the approved type-specific schema and IANA timezone;
- Health Connect records are immutable from manual edit flows;
- `URGENT` smart alerts remain informational and do not claim diagnosis or emergency dispatch;
- consent state is checked immediately before sharing minimum necessary AI context;
- `AuditLog.metadata` is sanitized.

## Additional supporting models

The proposal adds `OtpChallenge`, `ReminderOccurrence`, `WaterIntakeLog`, and `ExerciseRecommendation` beyond the minimum entity list because Business Analysis v0.2 explicitly requires OTP verification, reminder completion/progress, water-intake logging, and exercise suggestions.

## Initial migration record

The approved schema was materialized as:

- Migration: `backend/prisma/migrations/20261119033127_foundation_initial/migration.sql`
- Target: local Foundation PostgreSQL database only
- Prisma status: one migration applied; schema is up to date
- SQL inspection: 25 tables, 21 enum types, 42 indexes, and 29 foreign keys
- Destructive-statement inspection: no `DROP`, `TRUNCATE`, `DELETE FROM`, or `ALTER TABLE ... DROP`
- Generated client: Prisma Client 7.10.1 under the ignored `backend/src/generated/prisma` build output
- Runtime connection: `PrismaService` with the approved PostgreSQL driver adapter

Migration status: **APPLIED AND VERIFIED LOCALLY — 2026-11-19**.  
Database schema status: **IN SYNC FOR THE LOCAL FOUNDATION DATABASE**.
