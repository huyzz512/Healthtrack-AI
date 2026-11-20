# CCR-001 — Foundation enum alignment

Status: **APPROVED AND IMPLEMENTED — 2026-11-20**

```text
UC/Feature: Foundation shared contract
Type: BREAKING relative to the unreleased draft; initial released baseline
Current contract: Draft terms and approved Prisma persistence names conflict
Proposed contract: DataSource; NORMAL; CRITICAL; REQUESTED/READY; explicit provenance and AI consent values
Reason: Resolve OQ-012 and provide one Android–Backend vocabulary before the first contract release
Backend impact: Align Prisma enum type/value names through a non-destructive migration
Android impact: Add canonical enum mappings with UNKNOWN fallbacks
Migration: 20261120090000_align_foundation_contract_enums
Backward compatibility: No released API existed; database enum renames retain rows
Tests required: Prisma validation/migration, Backend contract tests, Android mapping tests, OpenAPI drift check
Backend approval: APPROVED under the Project Owner's explicit Foundation-completion delegation
Android approval: APPROVED under the Project Owner's explicit Foundation-completion delegation
```

This approval is limited to Foundation v0.1 completion and does not authorize implementation of UC-01 through UC-20.
