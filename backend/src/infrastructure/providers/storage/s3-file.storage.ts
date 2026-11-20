import {
  DeleteObjectCommand,
  GetObjectCommand,
  ListBucketsCommand,
  PutObjectCommand,
  S3Client,
} from '@aws-sdk/client-s3';
import { Inject, Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

import type {
  FileStorage,
  PrivateObject,
  PutPrivateObjectRequest,
  PutPrivateObjectResult,
} from './file-storage.js';

function assertSafeObjectKey(key: string): void {
  const segments = key.split('/');
  if (
    key.length === 0 ||
    key.startsWith('/') ||
    key.includes('\0') ||
    segments.some((segment) => segment === '..')
  ) {
    throw new Error('Object key is invalid.');
  }
}

@Injectable()
export class S3FileStorage implements FileStorage {
  private readonly bucket: string;
  private readonly client: S3Client;

  constructor(@Inject(ConfigService) configService: ConfigService) {
    const useSsl = configService.getOrThrow<boolean>('MINIO_USE_SSL');
    const host = configService.getOrThrow<string>('MINIO_ENDPOINT');
    const port = configService.getOrThrow<number>('MINIO_PORT');

    this.bucket = configService.getOrThrow<string>('MINIO_BUCKET');
    this.client = new S3Client({
      credentials: {
        accessKeyId: configService.getOrThrow<string>('MINIO_ROOT_USER'),
        secretAccessKey: configService.getOrThrow<string>('MINIO_ROOT_PASSWORD'),
      },
      endpoint: `${useSsl ? 'https' : 'http'}://${host}:${port}`,
      forcePathStyle: true,
      region: 'us-east-1',
    });
  }

  async putPrivateObject(
    request: PutPrivateObjectRequest,
  ): Promise<PutPrivateObjectResult> {
    assertSafeObjectKey(request.key);
    const response = await this.client.send(
      new PutObjectCommand({
        Body: request.body,
        Bucket: this.bucket,
        ContentType: request.contentType,
        Key: request.key,
      }),
    );

    return response.ETag === undefined ? {} : { etag: response.ETag };
  }

  async getPrivateObject(key: string): Promise<PrivateObject> {
    assertSafeObjectKey(key);
    const response = await this.client.send(
      new GetObjectCommand({ Bucket: this.bucket, Key: key }),
    );
    if (response.Body === undefined) {
      throw new Error('Storage returned an object without a body.');
    }

    const body = await response.Body.transformToByteArray();
    return response.ContentType === undefined
      ? { body }
      : { body, contentType: response.ContentType };
  }

  async deletePrivateObject(key: string): Promise<void> {
    assertSafeObjectKey(key);
    await this.client.send(
      new DeleteObjectCommand({ Bucket: this.bucket, Key: key }),
    );
  }

  async isAvailable(): Promise<boolean> {
    try {
      await this.client.send(new ListBucketsCommand({}));
      return true;
    } catch {
      return false;
    }
  }
}
