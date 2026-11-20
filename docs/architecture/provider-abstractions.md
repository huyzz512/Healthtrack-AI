# Provider abstractions

Status: Foundation v0.1 implemented.

External capabilities are injected through application-owned interfaces. Feature modules must not import vendor SDKs directly.

| Capability | Injection token | Foundation adapter | External side effect |
|---|---|---|---|
| AI completion | `AI_PROVIDER` | `MockAIProvider` | None |
| OTP delivery | `OTP_PROVIDER` | `MockOTPProvider` | None |
| Notification delivery | `NOTIFICATION_PROVIDER` | `MockNotificationProvider` | None |
| PDF rendering | `PDF_RENDERER` | `MockPdfRenderer` | None |
| Private object storage | `FILE_STORAGE` | `S3FileStorage` using AWS SDK | Local Silo only |

The mock adapters return clearly labeled placeholder results, do not log inputs, and do not echo sensitive payloads in delivery receipts. They are test/development foundations, not production behavior.

`S3FileStorage` uses path-style requests against the Silo MinIO-compatible endpoint. Objects are stored only in the configured private bucket; database rows retain ownership and object-key metadata. Authorization remains the responsibility of the application service before any storage operation.

Adding a production AI, OTP, notification, PDF, or storage adapter requires dependency/configuration review and approval. A real AI adapter also requires explicit consent enforcement and minimum-necessary context in the calling application service.
