-- Align persistence names with the approved Foundation v0.1 shared contract.
-- PostgreSQL preserves existing rows when enum types and values are renamed.

ALTER TYPE "SourceType" RENAME TO "DataSource";
ALTER TYPE "DataSource" RENAME VALUE 'OTHER' TO 'IMPORTED';
ALTER TYPE "DataSource" ADD VALUE 'SYSTEM_GENERATED';

ALTER TYPE "MoodType" RENAME VALUE 'NEUTRAL' TO 'NORMAL';
ALTER TYPE "AlertSeverity" RENAME VALUE 'URGENT' TO 'CRITICAL';
ALTER TYPE "ReportStatus" RENAME VALUE 'PENDING' TO 'REQUESTED';
ALTER TYPE "ReportStatus" RENAME VALUE 'COMPLETED' TO 'READY';
