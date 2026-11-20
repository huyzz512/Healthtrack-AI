import { Injectable } from '@nestjs/common';

import type {
  NotificationDeliveryReceipt,
  NotificationDeliveryRequest,
  NotificationProvider,
} from './notification-provider.js';

@Injectable()
export class MockNotificationProvider implements NotificationProvider {
  deliver(
    request: NotificationDeliveryRequest,
  ): Promise<NotificationDeliveryReceipt> {
    return Promise.resolve({
      accepted: true,
      messageId: `mock-notification-${request.notificationId}`,
      provider: 'mock',
    });
  }
}
