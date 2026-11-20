# DR-001 — Effect transitive security override

Status: **APPROVED — 2026-11-20**

## Request

- Package: `effect@3.22.5`, npm override of Prisma CLI's exact transitive `effect@3.20.0`.
- Purpose: remediate GHSA-7589-6fvf-x4pw, a high-severity ReDoS advisory affecting `effect <=3.22.0`.
- Current-stack limitation: Prisma 7.10.1 pins the vulnerable transitive version; npm's automatic remediation proposes a breaking downgrade to Prisma 6.12.0.

## Alternatives

- Downgrade Prisma to 6.12.0: rejected because it is a breaking toolchain/schema-client change and conflicts with the approved Prisma 7.10.1 baseline.
- Accept/suppress the advisory: rejected because a patched same-major Effect release is available.
- Upgrade Effect to 4.x: rejected because a major transitive override has unnecessary compatibility risk.

## Impact and approval

- Security: removes the audited vulnerable version from the locked dependency tree.
- Maintenance: the override is explicit and must be reconsidered when Prisma ships a patched direct pin.
- Runtime: no new direct application dependency; Effect remains Prisma tooling/config infrastructure.
- Owners: Backend/Integration.
- Approval: Project Owner's explicit Foundation-completion delegation, recorded as D-028.

Acceptance requires locked install, `npm audit --omit=dev --audit-level=high`, Prisma generate/validate/migrate status, lint, typecheck, tests, build, and OpenAPI drift verification.
