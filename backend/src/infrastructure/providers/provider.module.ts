import { Global, Module } from '@nestjs/common';

import { MockAIProvider } from './ai/mock-ai.provider.js';
import { MockNotificationProvider } from './notifications/mock-notification.provider.js';
import { MockOTPProvider } from './otp/mock-otp.provider.js';
import { MockPdfRenderer } from './pdf/mock-pdf.renderer.js';
import {
  AI_PROVIDER,
  FILE_STORAGE,
  NOTIFICATION_PROVIDER,
  OTP_PROVIDER,
  PDF_RENDERER,
} from './provider.tokens.js';
import { S3FileStorage } from './storage/s3-file.storage.js';

const providerBindings = [
  { provide: AI_PROVIDER, useExisting: MockAIProvider },
  { provide: FILE_STORAGE, useExisting: S3FileStorage },
  { provide: OTP_PROVIDER, useExisting: MockOTPProvider },
  { provide: NOTIFICATION_PROVIDER, useExisting: MockNotificationProvider },
  { provide: PDF_RENDERER, useExisting: MockPdfRenderer },
];

@Global()
@Module({
  exports: [
    AI_PROVIDER,
    FILE_STORAGE,
    OTP_PROVIDER,
    NOTIFICATION_PROVIDER,
    PDF_RENDERER,
  ],
  providers: [
    MockAIProvider,
    S3FileStorage,
    MockOTPProvider,
    MockNotificationProvider,
    MockPdfRenderer,
    ...providerBindings,
  ],
})
export class ProviderModule {}
