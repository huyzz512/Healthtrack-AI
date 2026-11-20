# Foundation security and privacy baseline

Status: Foundation v0.1 baseline. This is not a claim of production readiness, regulatory compliance, or medical-grade security.

## Enforced boundaries

- Backend derives resource ownership from the authenticated principal; client-provided owner IDs are never authorization evidence.
- Android is an untrusted client and does not contain server, S3, AI, OTP, or signing secrets.
- Health Connect access remains on Android; imported provenance is retained and Backend owns validation/idempotency before persistence.
- Medical objects use private S3-compatible storage. PostgreSQL stores metadata and object keys, not permanent public URLs or storage credentials.
- External AI is Backend-only and requires the applicable explicit consent immediately before minimum-necessary context is prepared.
- Logs and error envelopes exclude passwords, OTPs, tokens, medical files, and raw sensitive payloads. Unexpected errors use a sanitized stable code and trace ID.
- Password, OTP, and refresh-token persistence is hash-only by contract and schema.
- `.env` and build outputs are ignored; `.env.example` contains local placeholders only.

## Foundation verification

- Environment validation rejects missing PostgreSQL configuration and unsupported non-mock providers.
- Provider tests verify that mocks do not echo OTP destinations/codes, notification contents, AI prompts, or report model data.
- Health error tests verify sanitized `503` envelopes and trace propagation.
- Private local storage verification performs write/read/delete without exposing a public object URL.
- CI has read-only repository permissions and uses service-only local test credentials.

## Deferred before production

Production identity flows, secret management, provider credentials, rate limiting, retention/deletion periods, backup/restore, threat modeling, dependency monitoring policy, penetration testing, incident response, and deployment hardening require separate approval and evidence.
