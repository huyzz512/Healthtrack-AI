# System overview

## Context

HealthTrack AI is a personal-health system with an Android client, a Backend API, PostgreSQL, private object storage, local device integrations, and replaceable external providers.

```text
Smartwatch/vendor ecosystem
            |
      Health Connect
            |
      Android application
            |
       HTTPS /api/v1
            |
    NestJS modular monolith
      |       |        |
 PostgreSQL  Silo/S3  External providers
                    (AI/OTP/PDF/notification)
```

## Trust boundaries

- The Android device is an untrusted API client. Backend derives identity from the authenticated principal.
- Health Connect data enters through Android with permission and provenance, then is revalidated and deduplicated by Backend.
- Medical objects are private and accessed only through authorized server-controlled flows.
- External AI receives only consented, minimized, sanitized context.

## Major responsibilities

Android owns user experience, local cache, Health Connect, device permissions, background work, and local notifications. Backend owns shared truth, validation, authorization, persistence, aggregation, report generation, provider access, and contract publication.

## Foundation boundary

The Foundation provides bootable shells, contracts, boundaries, provider interfaces, and verification. It does not complete UC-01 through UC-20 or provide real medical alert thresholds.

