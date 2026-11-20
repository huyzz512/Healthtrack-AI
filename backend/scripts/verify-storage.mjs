import {
  CreateBucketCommand,
  ListBucketsCommand,
  S3Client,
} from '@aws-sdk/client-s3';
import { ConfigService } from '@nestjs/config';
import dotenv from 'dotenv';
import { randomUUID } from 'node:crypto';
import { resolve } from 'node:path';

import { S3FileStorage } from '../dist/infrastructure/providers/storage/s3-file.storage.js';

dotenv.config({ path: resolve(process.cwd(), '../.env'), quiet: true });
dotenv.config({ path: resolve(process.cwd(), '.env'), override: true, quiet: true });

const settings = {
  MINIO_BUCKET: process.env.MINIO_BUCKET ?? 'healthtrack-private',
  MINIO_ENDPOINT: process.env.MINIO_ENDPOINT ?? 'localhost',
  MINIO_PORT: Number(process.env.MINIO_PORT ?? 9000),
  MINIO_ROOT_PASSWORD:
    process.env.MINIO_ROOT_PASSWORD ?? 'local-only-change-me',
  MINIO_ROOT_USER: process.env.MINIO_ROOT_USER ?? 'healthtrack-local',
  MINIO_USE_SSL: (process.env.MINIO_USE_SSL ?? 'false') === 'true',
};

const endpoint = `${settings.MINIO_USE_SSL ? 'https' : 'http'}://${settings.MINIO_ENDPOINT}:${settings.MINIO_PORT}`;
const credentials = {
  accessKeyId: settings.MINIO_ROOT_USER,
  secretAccessKey: settings.MINIO_ROOT_PASSWORD,
};
const client = new S3Client({
  credentials,
  endpoint,
  forcePathStyle: true,
  region: 'us-east-1',
});

const buckets = await client.send(new ListBucketsCommand({}));
if (!buckets.Buckets?.some((bucket) => bucket.Name === settings.MINIO_BUCKET)) {
  await client.send(
    new CreateBucketCommand({ Bucket: settings.MINIO_BUCKET }),
  );
}

const storage = new S3FileStorage(new ConfigService(settings));
if (!(await storage.isAvailable())) {
  throw new Error('S3-compatible storage availability check failed.');
}

const key = `foundation/connection-check-${randomUUID()}.txt`;
const expected = Buffer.from('HealthTrack AI Foundation storage check', 'utf8');
let objectCreated = false;

try {
  await storage.putPrivateObject({
    body: expected,
    contentType: 'text/plain',
    key,
  });
  objectCreated = true;

  const stored = await storage.getPrivateObject(key);
  if (!Buffer.from(stored.body).equals(expected)) {
    throw new Error('Stored object content did not round-trip correctly.');
  }

  console.log(
    `Storage verification passed for private bucket "${settings.MINIO_BUCKET}".`,
  );
} finally {
  if (objectCreated) {
    await storage.deletePrivateObject(key);
  }
  client.destroy();
}
