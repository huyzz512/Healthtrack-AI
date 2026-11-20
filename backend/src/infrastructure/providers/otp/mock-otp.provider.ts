import { Injectable } from '@nestjs/common';

import type {
  OTPDeliveryReceipt,
  OTPDeliveryRequest,
  OTPProvider,
} from './otp-provider.js';

@Injectable()
export class MockOTPProvider implements OTPProvider {
  deliver(request: OTPDeliveryRequest): Promise<OTPDeliveryReceipt> {
    return Promise.resolve({
      accepted: true,
      messageId: `mock-otp-${request.challengeId}`,
      provider: 'mock',
    });
  }
}
