import { Module, RequestMethod } from '@nestjs/common';
import type { MiddlewareConsumer } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';

import { RequestIdMiddleware } from './common/middleware/request-id.middleware.js';
import { validateEnvironment } from './config/environment.js';
import { DatabaseModule } from './infrastructure/database/database.module.js';
import { ProviderModule } from './infrastructure/providers/provider.module.js';
import { ActivityModule } from './modules/activity/activity.module.js';
import { AiAssistantModule } from './modules/ai-assistant/ai-assistant.module.js';
import { AnalyticsModule } from './modules/analytics/analytics.module.js';
import { AuthModule } from './modules/auth/auth.module.js';
import { BodyMetricsModule } from './modules/body-metrics/body-metrics.module.js';
import { HealthModule } from './modules/health/health.module.js';
import { HealthProfileModule } from './modules/health-profile/health-profile.module.js';
import { HealthSyncModule } from './modules/health-sync/health-sync.module.js';
import { MedicalRecordsModule } from './modules/medical-records/medical-records.module.js';
import { MoodModule } from './modules/mood/mood.module.js';
import { NutritionModule } from './modules/nutrition/nutrition.module.js';
import { RemindersModule } from './modules/reminders/reminders.module.js';
import { ReportsModule } from './modules/reports/reports.module.js';
import { SmartAlertsModule } from './modules/smart-alerts/smart-alerts.module.js';
import { UsersModule } from './modules/users/users.module.js';
import { VitalSignsModule } from './modules/vital-signs/vital-signs.module.js';

@Module({
  imports: [
    ConfigModule.forRoot({
      cache: true,
      envFilePath: ['.env', '../.env'],
      isGlobal: true,
      validate: validateEnvironment,
    }),
    DatabaseModule,
    ProviderModule,
    HealthModule,
    AuthModule,
    UsersModule,
    HealthProfileModule,
    MedicalRecordsModule,
    BodyMetricsModule,
    VitalSignsModule,
    RemindersModule,
    SmartAlertsModule,
    NutritionModule,
    MoodModule,
    HealthSyncModule,
    AiAssistantModule,
    ActivityModule,
    AnalyticsModule,
    ReportsModule,
  ],
})
export class AppModule {
  configure(consumer: MiddlewareConsumer): void {
    consumer
      .apply(RequestIdMiddleware)
      .forRoutes({ path: '{*path}', method: RequestMethod.ALL });
  }
}
