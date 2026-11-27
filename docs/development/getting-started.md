# Getting started

## Prerequisites

- Node.js 24 and npm.
- Docker Desktop with Compose.
- JDK 17 for the approved Android/CI toolchain.
- Android SDK 36 and accepted SDK licenses.

Use only the committed npm lockfile, Gradle Wrapper, and version catalog. Do not put real credentials in `.env` or Android resources.

## Local configuration and infrastructure

From the repository root:

```powershell
Copy-Item .env.example .env
docker compose config
docker compose up -d postgres minio
docker compose ps
```

The example configuration is local-only. PostgreSQL binds to `127.0.0.1:5432`; Silo's S3-compatible API and console bind to `127.0.0.1:9000` and `127.0.0.1:9001`. `docker compose down` preserves volumes; volume deletion is intentionally not a Foundation command.

## Backend

From `backend/`:

```powershell
npm ci
npm run prisma:generate
npm run prisma:validate
npm exec prisma migrate deploy
npm exec prisma migrate status
npm run lint
npm run typecheck
npm test
npm run build
npm run openapi:check
npm run integration:verify
npm run storage:verify
npm run start:dev
```

The API is rooted at `http://localhost:3000/api/v1`, Swagger is at `http://localhost:3000/api/docs`, and the executable Foundation endpoint is `GET /api/v1/health`.

Run `npm run openapi:generate` after an approved API documentation change, then review and commit `docs/api/openapi.json` with the contract change.

## Android

Set `sdk.dir` in the ignored `android/local.properties` when the Android SDK is not discoverable automatically. From `android/` on Windows:

```powershell
.\gradlew.bat --no-daemon assembleDebug testDebugUnitTest lintDebug
```

Debug builds default to `http://127.0.0.1:3001/api/v1/` for local LDPlayer development. Forward that port to the host before launching the app:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb -s 127.0.0.1:5555 reverse tcp:3001 tcp:3001
```

Run the backend on port `3001` when the default port `3000` is already occupied:

```powershell
$env:APP_HOST = "0.0.0.0"
$env:APP_PORT = "3001"
npm run start:dev
```

For another emulator or API host, override the debug URL with `-PhealthtrackApiBaseUrl=http://<host>:<port>/api/v1/` or `HEALTHTRACK_API_BASE_URL`. Debug permits cleartext HTTP for local development only; release builds must use HTTPS and an explicitly configured API URL.

## Stop local services

From the repository root:

```powershell
docker compose down
```

This does not remove database or object-storage volumes.
