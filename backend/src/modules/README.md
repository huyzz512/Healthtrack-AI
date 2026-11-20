# Backend module boundaries

These folders establish the approved modular-monolith ownership map. During Foundation they contain NestJS module shells only.

Each business module must later expose application-level interfaces. Controllers must call application services; application services must use repositories; only infrastructure adapters may access Prisma. Cross-module database queries are forbidden.
