# Backend module boundaries

These folders establish the approved modular-monolith ownership map. Auth and
Users now implement the Backend Phase 1 candidate; the remaining business
modules retain Foundation shells (Health retains its executable health check).

Each business module must later expose application-level interfaces. Controllers must call application services; application services must use repositories; only infrastructure adapters may access Prisma. Cross-module database queries are forbidden.

Users exports AccountUnitOfWork and Accounts domain interfaces. Its Prisma
adapter alone accesses User/UserCredential. Auth binds its own OTP/refresh
repository to the opaque transaction scope supplied through that interface.
Per-account advisory locks coordinate auth writes; provider delivery is outside
transactions. UsersController is wired by AuthModule to reuse its access guard.
