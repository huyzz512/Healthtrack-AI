# Open questions

These questions do not block the repository and core Foundation phases unless marked otherwise.

| ID | Question | Owner | Status |
|---|---|---|---|
| OQ-001 | What GitHub organization and remote URL will host the repository? | Project | Open |
| OQ-002 | Which production SMS/email OTP providers will be used? | Backend | Deferred; mock-first |
| OQ-003 | Which production AI model/provider configuration will be approved? | Backend + Privacy | Deferred; mock-first |
| OQ-004 | Which production S3-compatible service and region will be used? | Backend/Infrastructure | Deferred |
| OQ-005 | What are medical-document, audit-log, chat-history, and generated-report retention periods? | Project/Privacy | Open before production design |
| OQ-006 | What Android branding, icon, and localization set will be used? | Android | Deferred |
| OQ-007 | Which medical expert approves smart-alert thresholds and rule versions? | Project | Blocking before real alert rules |
| OQ-008 | Will a food database provider be integrated or will calorie data remain user-entered? | Product | Deferred |
| OQ-009 | Which smartwatch/vendor combinations are in the verification matrix? | Android | Deferred |
| OQ-010 | Which production S3-compatible service, region, and operational controls will be approved independently of the local Silo service? | Backend/Infrastructure | Blocking before production design |
| OQ-011 | Approve the exact Android/Gradle/Kotlin/dependency version set and local Android SDK CLI installation? | Project + Android | Resolved by D-024 |
| OQ-012 | How should the locked Foundation shared enums be reconciled with the approved Prisma persistence enums? The conflicts include `DataSource` vs `SourceType`, `NORMAL` vs `NEUTRAL`, `CRITICAL` vs `URGENT`, `REQUESTED/READY` vs `PENDING/COMPLETED`, and missing provenance/consent values. | Project + Backend + Android | **Blocking Phase L completion and Phases M–N** |

