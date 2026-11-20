import { readFile, writeFile } from 'node:fs/promises';
import { resolve } from 'node:path';

import { ConsoleLogger } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';

process.env.DATABASE_URL ??=
  'postgresql://healthtrack:local-only-change-me@localhost:5432/healthtrack?schema=public';
process.env.AI_PROVIDER ??= 'mock';
process.env.OTP_PROVIDER ??= 'mock';
process.env.NOTIFICATION_PROVIDER ??= 'mock';
process.env.PDF_PROVIDER ??= 'mock';
process.env.MINIO_ROOT_USER ??= 'healthtrack-local';
process.env.MINIO_ROOT_PASSWORD ??= 'local-only-change-me';
process.env.MINIO_ENDPOINT ??= 'localhost';
process.env.MINIO_PORT ??= '9000';
process.env.MINIO_USE_SSL ??= 'false';
process.env.MINIO_BUCKET ??= 'healthtrack-private';

const { configureApplication } = await import('../dist/app.setup.js');
const { AppModule } = await import('../dist/app.module.js');

const app = await NestFactory.create(AppModule, { logger: false });
const document = configureApplication(app, new ConsoleLogger({ json: true }));
const serialized = `${JSON.stringify(document, null, 2)}\n`;
const outputPath = resolve(process.cwd(), '../docs/api/openapi.json');

try {
  if (process.argv.includes('--check')) {
    const committed = await readFile(outputPath, 'utf8').catch(() => '');
    if (committed !== serialized) {
      throw new Error(
        'Committed OpenAPI is stale. Run "npm run openapi:generate" from backend/.',
      );
    }
    console.log('OpenAPI contract is up to date.');
  } else {
    await writeFile(outputPath, serialized, 'utf8');
    console.log(`Wrote ${outputPath}`);
  }
} finally {
  await app.close();
}
