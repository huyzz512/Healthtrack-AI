export type OTPChannel = 'email' | 'sms';

export interface OTPDeliveryRequest {
  readonly challengeId: string;
  readonly channel: OTPChannel;
  readonly recipient: string;
  readonly code: string;
  readonly expiresAt: Date;
}

export interface OTPDeliveryReceipt {
  readonly accepted: boolean;
  readonly messageId: string;
  readonly provider: string;
}

export interface OTPProvider {
  deliver(request: OTPDeliveryRequest): Promise<OTPDeliveryReceipt>;
}
