# API conventions

The normative shared contract is `integration-contract.md`.

- Paths are plural resource nouns under `/api/v1`.
- Request DTOs reject unknown unsafe input through validation/whitelisting.
- IDs are UUID strings.
- Lists use `page`, `pageSize`, and `total`; default page size is 20 unless an endpoint contract says otherwise.
- `message` is for humans; clients branch on HTTP status and stable `error.code`.
- Trace IDs are returned for errors and propagated through structured logs.
- Timestamps are UTC instants; dates are `YYYY-MM-DD`.
- Ownership is never selected by an authenticated client's `userId` field.
- Binary uploads use endpoint-specific multipart contracts and the 10 MB medical-file limit.

