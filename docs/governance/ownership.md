# Ownership

| Area | Primary owner | Shared review required |
|---|---|---|
| NestJS modules and server application logic | Backend | When API/contract changes |
| Prisma schema and PostgreSQL | Backend | When shared data semantics change |
| Authentication/authorization implementation | Backend | Authentication contract |
| Swagger/OpenAPI | Backend / Integration Owner | Android |
| Object storage, AI, OTP, PDF adapters | Backend | Privacy/contract impact |
| Compose UI and Android state | Android | No, unless contract changes |
| Retrofit mapping and local cache | Android | API mapping changes |
| Health Connect permission/read/cache | Android | Sync protocol |
| Local reminders and notifications | Android | Reminder contract |
| Integration contract, enums, errors, dates, units | Shared | Backend + Android |
| CI integration and release readiness | Integration Owner | Affected owner |

The Backend Owner acts as Integration Owner. Ownership does not permit unilateral changes to shared contracts.

