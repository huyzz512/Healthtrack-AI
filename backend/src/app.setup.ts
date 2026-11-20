import { ValidationPipe } from '@nestjs/common';
import type { ConsoleLogger, INestApplication } from '@nestjs/common';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';

import { API_PREFIX } from './common/constants/api.constants.js';
import { ApiExceptionFilter } from './common/filters/api-exception.filter.js';
import { ResponseEnvelopeInterceptor } from './common/interceptors/response-envelope.interceptor.js';

export function configureApplication(
  app: INestApplication,
  logger: ConsoleLogger,
): void {
  app.setGlobalPrefix(API_PREFIX);
  app.useGlobalPipes(
    new ValidationPipe({
      forbidNonWhitelisted: true,
      transform: true,
      whitelist: true,
    }),
  );
  app.useGlobalFilters(new ApiExceptionFilter(logger));
  app.useGlobalInterceptors(new ResponseEnvelopeInterceptor());

  const origins = (process.env.APP_CORS_ORIGINS ?? 'http://localhost:3000')
    .split(',')
    .map((origin) => origin.trim())
    .filter(Boolean);
  app.enableCors({ credentials: false, origin: origins });

  const swaggerConfig = new DocumentBuilder()
    .setTitle('HealthTrack AI API')
    .setDescription('Foundation contract surface; business endpoints are not implemented.')
    .setVersion(process.env.APP_VERSION ?? 'foundation-v0.1')
    .addBearerAuth()
    .build();
  const document = SwaggerModule.createDocument(app, swaggerConfig);
  SwaggerModule.setup('api/docs', app, document);
}
