const DEFAULT_APP_PORT = 3000;

export interface Environment {
  APP_ENV: string;
  APP_HOST: string;
  APP_PORT: number;
  APP_VERSION: string;
  DATABASE_URL: string;
  AI_PROVIDER: 'mock';
  OTP_PROVIDER: 'mock';
  NOTIFICATION_PROVIDER: 'mock';
  PDF_PROVIDER: 'mock';
  MINIO_ROOT_USER: string;
  MINIO_ROOT_PASSWORD: string;
  MINIO_ENDPOINT: string;
  MINIO_PORT: number;
  MINIO_USE_SSL: boolean;
  MINIO_BUCKET: string;
}

function requiredString(
  config: Record<string, unknown>,
  key: string,
  fallback: string,
): string {
  const value = String(config[key] ?? fallback).trim();
  if (value.length === 0) {
    throw new Error(`${key} must not be empty.`);
  }
  return value;
}

function mockProvider(
  config: Record<string, unknown>,
  key: string,
): 'mock' {
  const value = String(config[key] ?? 'mock');
  if (value !== 'mock') {
    throw new Error(`${key} only supports mock during Foundation.`);
  }
  return value;
}

export function validateEnvironment(config: Record<string, unknown>): Environment {
  const databaseUrl = String(config.DATABASE_URL ?? '');
  if (!databaseUrl.startsWith('postgresql://') && !databaseUrl.startsWith('postgres://')) {
    throw new Error('DATABASE_URL must be a PostgreSQL connection URL.');
  }

  const appPort = Number(config.APP_PORT ?? DEFAULT_APP_PORT);
  if (!Number.isInteger(appPort) || appPort < 1 || appPort > 65_535) {
    throw new Error('APP_PORT must be an integer between 1 and 65535.');
  }

  const minioPort = Number(config.MINIO_PORT ?? 9000);
  if (!Number.isInteger(minioPort) || minioPort < 1 || minioPort > 65_535) {
    throw new Error('MINIO_PORT must be an integer between 1 and 65535.');
  }

  const minioUseSsl = String(config.MINIO_USE_SSL ?? 'false').toLowerCase();
  if (minioUseSsl !== 'true' && minioUseSsl !== 'false') {
    throw new Error('MINIO_USE_SSL must be true or false.');
  }

  return {
    APP_ENV: String(config.APP_ENV ?? 'development'),
    APP_HOST: String(config.APP_HOST ?? '0.0.0.0'),
    APP_PORT: appPort,
    APP_VERSION: String(config.APP_VERSION ?? 'foundation-v0.1'),
    DATABASE_URL: databaseUrl,
    AI_PROVIDER: mockProvider(config, 'AI_PROVIDER'),
    OTP_PROVIDER: mockProvider(config, 'OTP_PROVIDER'),
    NOTIFICATION_PROVIDER: mockProvider(config, 'NOTIFICATION_PROVIDER'),
    PDF_PROVIDER: mockProvider(config, 'PDF_PROVIDER'),
    MINIO_ROOT_USER: requiredString(
      config,
      'MINIO_ROOT_USER',
      'healthtrack-local',
    ),
    MINIO_ROOT_PASSWORD: requiredString(
      config,
      'MINIO_ROOT_PASSWORD',
      'local-only-change-me',
    ),
    MINIO_ENDPOINT: requiredString(config, 'MINIO_ENDPOINT', 'localhost'),
    MINIO_PORT: minioPort,
    MINIO_USE_SSL: minioUseSsl === 'true',
    MINIO_BUCKET: requiredString(
      config,
      'MINIO_BUCKET',
      'healthtrack-private',
    ),
  };
}
