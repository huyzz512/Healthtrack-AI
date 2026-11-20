import { MockAIProvider } from '../dist/infrastructure/providers/ai/mock-ai.provider.js';
import { MockNotificationProvider } from '../dist/infrastructure/providers/notifications/mock-notification.provider.js';
import { MockOTPProvider } from '../dist/infrastructure/providers/otp/mock-otp.provider.js';
import { MockPdfRenderer } from '../dist/infrastructure/providers/pdf/mock-pdf.renderer.js';

describe('Foundation mock providers', () => {
  it('labels AI output and does not echo sensitive input', async () => {
    const provider = new MockAIProvider();
    const result = await provider.complete({
      consentReference: 'consent-test',
      prompt: 'sensitive health prompt',
    });

    expect(result.provider).toBe('mock');
    expect(result.content).not.toContain('sensitive health prompt');
  });

  it('does not expose OTP destination or code in its receipt', async () => {
    const provider = new MockOTPProvider();
    const result = await provider.deliver({
      challengeId: 'challenge-test',
      channel: 'sms',
      code: '123456',
      expiresAt: new Date('2026-11-19T04:00:00.000Z'),
      recipient: '+84000000000',
    });
    const serialized = JSON.stringify(result);

    expect(result.provider).toBe('mock');
    expect(serialized).not.toContain('123456');
    expect(serialized).not.toContain('+84000000000');
  });

  it('does not expose notification contents in its receipt', async () => {
    const provider = new MockNotificationProvider();
    const result = await provider.deliver({
      body: 'sensitive notification body',
      channel: 'in_app',
      notificationId: 'notification-test',
      title: 'sensitive title',
      userId: 'user-test',
    });
    const serialized = JSON.stringify(result);

    expect(result.provider).toBe('mock');
    expect(serialized).not.toContain('sensitive');
  });

  it('returns a clearly labeled PDF placeholder', async () => {
    const renderer = new MockPdfRenderer();
    const result = await renderer.render({
      documentId: 'document-test',
      model: { sensitive: 'value' },
      title: 'Foundation test',
    });

    expect(result.provider).toBe('mock');
    expect(result.contentType).toBe('application/pdf');
    expect(Buffer.from(result.bytes).toString('utf8')).toMatch(/^%PDF-1\.4/);
    expect(Buffer.from(result.bytes).toString('utf8')).not.toContain('value');
  });
});
