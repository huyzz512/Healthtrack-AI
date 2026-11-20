import { ConsoleLogger } from '@nestjs/common';
import { Test } from '@nestjs/testing';

process.env.DATABASE_URL =
  process.env.DATABASE_URL ??
  'postgresql://healthtrack:local-only-change-me@localhost:5432/healthtrack?schema=public';

const { configureApplication } = await import('../dist/app.setup.js');
const { AppModule } = await import('../dist/app.module.js');
const { DatabaseHealthService } = await import(
  '../dist/infrastructure/database/database-health.service.js'
);

describe('GET /api/v1/health', () => {
  let app;
  let databaseConnected;

  beforeAll(async () => {
    databaseConnected = true;
    const moduleRef = await Test.createTestingModule({ imports: [AppModule] })
      .overrideProvider(DatabaseHealthService)
      .useValue({ isConnected: async () => databaseConnected })
      .compile();

    app = moduleRef.createNestApplication({ logger: false });
    configureApplication(app, new ConsoleLogger({ json: true }));
    await app.listen(0, '127.0.0.1');
  });

  afterAll(async () => {
    await app.close();
  });

  it('returns the approved health envelope', async () => {
    const address = app.getHttpServer().address();
    const response = await fetch(`http://127.0.0.1:${address.port}/api/v1/health`);

    expect(response.status).toBe(200);
    expect(await response.json()).toEqual({
      data: {
        status: 'ok',
        database: 'connected',
        version: 'foundation-v0.1',
      },
    });
  });

  it('returns the approved sanitized error envelope when the database is unavailable', async () => {
    databaseConnected = false;
    const address = app.getHttpServer().address();
    const response = await fetch(`http://127.0.0.1:${address.port}/api/v1/health`, {
      headers: { 'x-request-id': 'foundation-test-trace' },
    });

    expect(response.status).toBe(503);
    expect(response.headers.get('x-request-id')).toBe('foundation-test-trace');
    expect(await response.json()).toEqual({
      error: {
        code: 'DATABASE_UNAVAILABLE',
        message: 'Database connectivity check failed.',
        details: null,
        traceId: 'foundation-test-trace',
      },
    });
    databaseConnected = true;
  });
});
