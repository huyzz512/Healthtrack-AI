# Module map

```text
auth -> users
users -> health-profile
medical-records -> storage
body-metrics ----\
vital-signs ------+-> analytics -> reports
nutrition --------+
mood -------------+
activity ----------/
health-sync -> body-metrics / vital-signs / activity through application interfaces
reminders -> Android local scheduler contract
smart-alerts -> authorized domain context interfaces
ai-assistant -> HealthContextProvider -> authorized domain services
```

Arrows express application-level collaboration, not permission to read another module's tables.

