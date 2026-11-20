import 'reflect-metadata';

import { ConsoleLogger } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';

import { AppModule } from './app.module.js';
import { configureApplication } from './app.setup.js';

async function bootstrap(): Promise<void> {
  const logger = new ConsoleLogger({ json: true, prefix: 'healthtrack-backend' });
  const app = await NestFactory.create(AppModule, { logger });

  configureApplication(app, logger);
  app.enableShutdownHooks();

  const host = process.env.APP_HOST ?? '0.0.0.0';
  const port = Number(process.env.APP_PORT ?? 3000);
  await app.listen(port, host);
}

void bootstrap();
