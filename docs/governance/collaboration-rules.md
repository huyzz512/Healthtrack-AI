# Collaboration rules

## Working branches

- `main`: stable/demo/release.
- `develop`: buildable integration branch.
- `feature/backend-*`: Backend Owner work.
- `feature/android-*`: Android Owner work.
- `hotfix/*`: narrowly scoped urgent fixes.

Do not commit directly to `main`, force-push `main` or `develop`, or rewrite shared history.

## Commit convention

Use Conventional Commit-style scopes:

```text
feat(backend): ...
feat(android): ...
fix(api): ...
docs(contract): ...
refactor(backend): ...
refactor(android): ...
test(backend): ...
test(android): ...
chore(...): ...
```

## Definition of Ready

A feature is ready only when its use case, rules, owner, dependencies, API behavior, enums, errors, date/time and unit semantics, ownership/security, UI states, offline behavior, idempotency where relevant, and acceptance criteria are known.

## Contract-change request

Use the title `[CONTRACT] <title>` and include:

```text
UC/Feature:
Type: NON-BREAKING / BREAKING
Current contract:
Proposed contract:
Reason:
Backend impact:
Android impact:
Migration:
Backward compatibility:
Tests required:
Backend approval:
Android approval:
```

Do not implement until both approvals are present. An optional field is non-breaking only after compatibility is evaluated.

## Cross-owner failures

If a Backend change breaks Android, or the reverse, report the failing contract and owning area. Do not patch the other owner's code only to make CI pass.

## Verification reports

Every implementation phase reports files created/modified, commands executed, tests/build results, known issues, contract impact, and the next step. Claims must match commands actually run.

