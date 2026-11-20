# Android architecture

## Style

The Foundation uses one Gradle `:app` module and package-based boundaries. This avoids early multi-module overhead while preserving dependency direction.

```text
Compose Screen
      |
ViewModel (UiState / UiEvent / UiEffect)
      |
Domain use case / repository interface
      |
Data repository
   /     |       \
Retrofit Room  Health Connect
```

Presentation depends on Domain; Domain does not depend on Android framework/data implementations; Data implements Domain interfaces.

## Packages

`core` contains common model, design system, navigation, network, database, datastore, Health Connect, notification, and testing foundations. `feature` contains package boundaries for auth, dashboard, medical records, health profile, body metrics, vitals, reminders, nutrition, mood, Health Connect, AI assistant, activity, analytics, reports, settings, and privacy.

## State and resilience

- ViewModels own private mutable state and expose read-only `StateFlow`.
- Screens handle loading, empty, error, unavailable, and permission-required states.
- Unknown server enum values map to an `UNKNOWN`/fallback model rather than crashing.
- Network mapping is separate from local/domain models.
- Health Connect unavailable or denied states do not prevent unrelated app functions.

## Device work

WorkManager handles deferrable sync. NotificationManager handles user-visible notifications. AlarmManager is reserved for approved exact-time needs and permission-aware behavior.

