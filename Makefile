.PHONY: backend-install backend-lint backend-typecheck backend-test backend-build backend-openapi android-verify infra-config infra-up infra-down

backend-install:
	cd backend && npm ci

backend-lint:
	cd backend && npm run lint

backend-typecheck:
	cd backend && npm run typecheck

backend-test:
	cd backend && npm test

backend-build:
	cd backend && npm run build

backend-openapi:
	cd backend && npm run openapi:check

android-verify:
	cd android && ./gradlew --no-daemon assembleDebug testDebugUnitTest lintDebug

infra-config:
	docker compose config

infra-up:
	docker compose up -d postgres minio

infra-down:
	docker compose down

