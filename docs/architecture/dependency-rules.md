# Dependency rules

## Repository-wide

Dependencies must be pinned by lockfile or version catalog. No dynamic versions. New packages require documented approval and security/maintenance review.

## Backend

Allowed direction:

```text
modules/controller -> modules/application -> module interfaces
infrastructure adapters -> module interfaces
common/config -> framework/platform utilities only
```

Forbidden:

- Controller → Prisma.
- DTO → repository/provider.
- Provider adapter → domain table query.
- Module repository → another module's tables.
- Domain/application code → transport response objects.

Infrastructure may depend on Prisma/provider SDKs. Application code depends on interfaces, not concrete adapters.

## Android

Allowed direction:

```text
feature presentation -> feature/domain or core model
data implementation -> domain interface
app composition root -> all concrete bindings
```

Forbidden:

- Composable → Retrofit/Room/HealthConnectClient.
- Domain model → Android UI/framework type.
- Network DTO reused as editable UI state.
- Feature package reaching into another feature's implementation.

## Dependency requests

Record package, purpose, why the current stack cannot solve it, alternatives, security impact, maintenance impact, affected owners, and recommendation before adding an unapproved dependency.

