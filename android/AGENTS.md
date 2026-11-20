# Android instructions

The root `AGENTS.md` applies.

## Ownership

Android owns Kotlin, Compose/Material 3, MVVM/UDF, navigation, ViewModels, Retrofit/OkHttp clients, Room, DataStore, Health Connect reads, WorkManager, notifications, AlarmManager usage, Android permissions, and UI/UX.

Android does not own server persistence, Prisma schema, API responses, JWT policy, backend authorization, storage infrastructure, or external AI provider selection.

## Boundaries

- Follow Presentation → Domain → Data.
- Composables are primarily stateless; ViewModels own exposed `StateFlow` state.
- UI never calls Retrofit, Room, or Health Connect directly.
- Every feature uses `UiState`, `UiEvent`, `UiEffect`, ViewModel, and Screen conventions where applicable.
- Handle loading, empty, error, offline, unavailable Health Connect, and unknown enum values without crashing.
- Local reminder scheduling remains on Android; Backend persists and validates configuration.

## Contract rules

Map only the API defined in `docs/api/integration-contract.md` and generated OpenAPI. Do not infer server fields or error behavior. Shared changes require both owners' approval.

## Forbidden changes

- Do not edit Backend implementation to compensate for an Android mismatch.
- Do not place OpenAI, MinIO, S3, or other server credentials in the application.
- Do not treat Health Connect data as user input or silently edit imported records.
- Do not introduce complex Gradle multi-module architecture without an approved ADR.

## Required checks

From `android/` run:

```text
gradlew.bat assembleDebug
gradlew.bat testDebugUnitTest
gradlew.bat lintDebug
```

