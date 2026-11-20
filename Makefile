.PHONY: backend-install backend-lint backend-typecheck backend-test backend-build infra-config infra-up infra-down

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

infra-config:
	docker compose config

infra-up:
	docker compose up -d postgres minio

infra-down:
	docker compose down

