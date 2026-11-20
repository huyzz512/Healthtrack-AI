import { NestFactory } from '@nestjs/core';
import { ConsoleLogger } from '@nestjs/common';

const { configureApplication } = await import('../dist/app.setup.js');
const { AppModule } = await import('../dist/app.module.js');

const app = await NestFactory.create(AppModule, { logger: false });
configureApplication(app, new ConsoleLogger({ json: true }));

try {
  await app.listen(0, '127.0.0.1');
  const address = app.getHttpServer().address();
  const baseUrl = `http://127.0.0.1:${address.port}`;

  const healthResponse = await fetch(`${baseUrl}/api/v1/health`, {
    headers: { 'x-request-id': 'foundation-integration-check' },
  });
  const healthBody = await healthResponse.json();
  if (
    healthResponse.status !== 200 ||
    healthBody?.data?.status !== 'ok' ||
    healthBody?.data?.database !== 'connected' ||
    healthBody?.data?.version !== 'foundation-v0.1'
  ) {
    throw new Error(
      `Health integration check failed (${healthResponse.status}): ${JSON.stringify(healthBody)}`,
    );
  }

  const swaggerResponse = await fetch(`${baseUrl}/api/docs-json`);
  const swaggerBody = await swaggerResponse.json();
  if (
    swaggerResponse.status !== 200 ||
    swaggerBody?.info?.version !== 'foundation-v0.1' ||
    swaggerBody?.paths?.['/api/v1/health']?.get === undefined
  ) {
    throw new Error('Swagger integration check failed.');
  }

  console.log('Database health endpoint and Swagger integration verification passed.');
} finally {
  await app.close();
}
