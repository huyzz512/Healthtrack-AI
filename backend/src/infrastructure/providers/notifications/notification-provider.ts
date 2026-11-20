export type NotificationChannel = 'in_app' | 'push';

export interface NotificationDeliveryRequest {
  readonly notificationId: string;
  readonly userId: string;
  readonly channel: NotificationChannel;
  readonly title: string;
  readonly body: string;
}

export interface NotificationDeliveryReceipt {
  readonly accepted: boolean;
  readonly messageId: string;
  readonly provider: string;
}

export interface NotificationProvider {
  deliver(
    request: NotificationDeliveryRequest,
  ): Promise<NotificationDeliveryReceipt>;
}
