import assert from 'node:assert/strict';
import { after, before, test } from 'node:test';
import { randomBytes, randomUUID } from 'node:crypto';
import { readFile, readdir } from 'node:fs/promises';
import dotenv from 'dotenv';
import pg from 'pg';
import { PrismaPg } from '@prisma/adapter-pg';
import { Test } from '@nestjs/testing';
import { ConfigService } from '@nestjs/config';
import { PrismaClient } from '../dist/generated/prisma/client.js';
import { PrismaService } from '../dist/infrastructure/database/prisma.service.js';
import { AppModule } from '../dist/app.module.js';
import { AuthApplicationService } from '../dist/modules/auth/auth.service.js';
import { AuthCrypto } from '../dist/modules/auth/auth.crypto.js';
import { AuthClock, AuthRepository } from '../dist/modules/auth/auth.port.js';
import { AccountUnitOfWork } from '../dist/modules/users/account.port.js';
import { OTP_PROVIDER } from '../dist/infrastructure/providers/provider.tokens.js';
import { configureApplication } from '../dist/app.setup.js';

dotenv.config({ path: ['.env', '../.env'], quiet: true });
const connectionString = process.env.AUTH_TEST_DATABASE_URL ?? process.env.DATABASE_URL;
if (!connectionString) throw new Error('DATABASE_URL or AUTH_TEST_DATABASE_URL is required. No database URL is logged.');
const schema = `auth_test_${randomUUID().replaceAll('-', '')}`;
assert.match(schema, /^auth_test_[a-f0-9]{32}$/);
let app, db, auth, module, base;
const deliveries = new Map();
let instant = Date.parse('2026-11-25T03:00:00Z');
const clock = { now: () => new Date(instant), advance: ms => { instant += ms; } };
const password = 'Integration!234';
const crypto = new AuthCrypto(new ConfigService({ JWT_ACCESS_SECRET: randomBytes(48).toString('hex'), JWT_REFRESH_SECRET: randomBytes(48).toString('hex') }));

before(async () => {
  // Only a freshly named schema is written. Existing public-schema rows and
  // migration history are never reset, updated or deleted by this suite.
  const setup = new pg.Client({ connectionString, connectionTimeoutMillis: 5_000 });
  await setup.connect();
  try {
    await setup.query('BEGIN');
    await setup.query(`CREATE SCHEMA "${schema}"`);
    await setup.query(`SET LOCAL search_path TO "${schema}"`);
    for (const entry of (await readdir(new URL('../prisma/migrations/', import.meta.url), { withFileTypes: true })).filter(e => e.isDirectory()).sort((a,b) => a.name.localeCompare(b.name))) {
      await setup.query(await readFile(new URL(`../prisma/migrations/${entry.name}/migration.sql`, import.meta.url), 'utf8'));
    }
    await setup.query('COMMIT');
  } catch (error) { await setup.query('ROLLBACK'); throw error; }
  finally { await setup.end(); }
  db = new PrismaClient({ adapter: new PrismaPg({ connectionString, max: 5 }, { schema }) });
  module = await Test.createTestingModule({ imports: [AppModule] })
    .overrideProvider(PrismaService).useValue(db)
    .overrideProvider(AuthCrypto).useValue(crypto)
    .overrideProvider(AuthClock).useValue(clock)
    .overrideProvider(OTP_PROVIDER).useValue({ async deliver(request) { deliveries.set(request.challengeId, request); return { accepted: true, provider: 'integration-only', messageId: request.challengeId }; } })
    .compile();
  app = module.createNestApplication({ logger: false });
  configureApplication(app, { error() {} });
  await app.listen(0, '127.0.0.1'); base = await app.getUrl();
  auth = module.get(AuthApplicationService);
}, { timeout: 30_000 });

after(async () => {
  await app?.close(); await db?.$disconnect();
  // Retained intentionally: no DROP SCHEMA CASCADE or destructive cleanup.
  console.log(`Isolated synthetic test schema retained: ${schema}`);
});
async function createActive() {
  const email = `test-${randomUUID()}@example.test`;
  const c = await auth.register({ email, fullName: 'Integration Person', password, confirmPassword: password });
  await auth.verifyOtp(c.challengeId, deliveries.get(c.challengeId).code);
  const session = await auth.login({ email, password });
  return { email, session, principal: await auth.authenticate(session.accessToken) };
}
const hasCode = code => error => error?.response?.code === code;

test('HTTP registration/OTP/login/profile use real persistence and retain the health envelope', async () => {
  const request = async (path, body, token, method = 'POST') => {
    const r = await fetch(`${base}/api/v1/${path}`, { method, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) }, ...(body ? { body: JSON.stringify(body) } : {}) });
    return { status: r.status, body: await r.json() };
  };
  const email = `http-${randomUUID()}@example.test`;
  const registration = await request('auth/register', { email, fullName: 'HTTP User', password, confirmPassword: password });
  assert.equal(registration.status, 201);
  const challengeId = registration.body.data.challengeId;
  assert.equal((await request('auth/verify-otp', { challengeId, code: deliveries.get(challengeId).code })).status, 200);
  const login = await request('auth/login', { email, password }); assert.equal(login.status, 200);
  const me = await request('users/me', undefined, login.body.data.accessToken, 'GET');
  assert.equal(me.body.data.email, email); assert.equal(me.body.data.credential, undefined);
  const stored = await db.userCredential.findUnique({ where: { userId: me.body.data.id } });
  assert.match(stored.passwordHash, /^\$argon2id\$/);
  assert.equal((await request('health', undefined, undefined, 'GET')).body.data.status, 'ok');
});

test('concurrent duplicate registration and OTP consumption each produce one winner', async () => {
  const input = { email: `race-${randomUUID()}@example.test`, fullName: 'Race User', password, confirmPassword: password };
  const results = await Promise.allSettled([auth.register(input), auth.register(input)]);
  assert.equal(results.filter(r => r.status === 'fulfilled').length, 1);
  const challenge = results.find(r => r.status === 'fulfilled').value;
  const verifies = await Promise.allSettled([auth.verifyOtp(challenge.challengeId, deliveries.get(challenge.challengeId).code), auth.verifyOtp(challenge.challengeId, deliveries.get(challenge.challengeId).code)]);
  assert.equal(verifies.filter(r => r.status === 'fulfilled').length, 1);
  assert.equal(await db.user.count({ where: { email: input.email } }), 1);
});

test('concurrent invalid OTP attempts persist exactly five attempts', async () => {
  const c = await auth.register({ email: `otp-${randomUUID()}@example.test`, fullName: 'OTP User', password, confirmPassword: password });
  const code = deliveries.get(c.challengeId).code;
  const wrong = code === '000000' ? '000001' : '000000';
  const attempts = await Promise.allSettled(Array.from({ length: 8 }, () => auth.verifyOtp(c.challengeId, wrong)));
  assert.ok(attempts.every(r => r.status === 'rejected'));
  assert.equal((await db.otpChallenge.findUnique({ where: { id: c.challengeId } })).failedAttempts, 5);
  await assert.rejects(auth.verifyOtp(c.challengeId, code), hasCode('AUTH_OTP_ATTEMPTS_EXCEEDED'));
});

test('concurrent refresh revokes the racing child, committing revocation despite the error', async () => {
  const u = await createActive();
  const results = await Promise.allSettled([auth.refresh(u.session.refreshToken), auth.refresh(u.session.refreshToken)]);
  assert.equal(results.filter(r => r.status === 'fulfilled').length, 1);
  assert.equal(await db.refreshToken.count({ where: { userId: u.principal.userId, revokedAt: null } }), 0);
  const child = results.find(r => r.status === 'fulfilled').value;
  await assert.rejects(auth.authenticate(child.accessToken), hasCode('AUTH_TOKEN_INVALID'));
});

test('logout ALL racing refresh leaves no active family', async () => {
  const u = await createActive();
  await Promise.allSettled([auth.refresh(u.session.refreshToken), auth.logout(u.principal, 'ALL')]);
  assert.equal(await db.refreshToken.count({ where: { userId: u.principal.userId, revokedAt: null } }), 0);
});

test('activation failure rolls back OTP consumption and account status', async () => {
  const c = await auth.register({ email: `rollback-${randomUUID()}@example.test`, fullName: 'Rollback User', password, confirmPassword: password });
  const realUow = module.get(AccountUnitOfWork);
  const failing = {
    read: work => realUow.read(work),
    run: (key, work) => realUow.run(key, (accounts, scope) => work(new Proxy(accounts, { get(target, prop) {
      if (prop === 'activate') return async (...args) => { await target.activate(...args); throw new Error('injected activation failure'); };
      const value = Reflect.get(target, prop); return typeof value === 'function' ? value.bind(target) : value;
    } }), scope)),
  };
  const service = new AuthApplicationService(failing, module.get(AuthRepository), crypto, clock, {});
  await assert.rejects(service.verifyOtp(c.challengeId, deliveries.get(c.challengeId).code), /injected activation failure/);
  const stored = await db.otpChallenge.findUnique({ where: { id: c.challengeId } });
  assert.equal(stored.consumedAt, null);
  assert.equal((await db.user.findUnique({ where: { id: stored.userId } })).status, 'PENDING_VERIFICATION');
});

test('password reset is single-use, revokes sessions, and persists a new hash', async () => {
  const u = await createActive(); const c = await auth.forgotPassword({ email: u.email });
  const id = crypto.openRecovery(c.challengeId, clock.now());
  const reset = { challengeId: c.challengeId, code: deliveries.get(id).code, password: 'NewPassword!234', confirmPassword: 'NewPassword!234' };
  const results = await Promise.allSettled([auth.resetPassword(reset), auth.resetPassword(reset)]);
  assert.equal(results.filter(r => r.status === 'fulfilled').length, 1);
  await assert.rejects(auth.authenticate(u.session.accessToken), hasCode('AUTH_TOKEN_INVALID'));
  await assert.rejects(auth.login({ email: u.email, password }), hasCode('AUTH_INVALID_CREDENTIALS'));
  assert.ok((await auth.login({ email: u.email, password: reset.password })).accessToken);
}, { timeout: 10_000 });

test('concurrent failed logins commit counters and enforce the lockout', async () => {
  const u = await createActive();
  const failures = await Promise.allSettled(Array.from({ length: 6 }, () => auth.login({ email: u.email, password: 'WrongPassword!1' })));
  assert.ok(failures.every(r => r.status === 'rejected'));
  const credential = await db.userCredential.findUnique({ where: { userId: u.principal.userId } });
  assert.equal(credential.failedLoginCount, 5);
  assert.equal(credential.lockedUntil.getTime(), clock.now().getTime() + 900_000);
  await assert.rejects(auth.login({ email: u.email, password }), hasCode('AUTH_ACCOUNT_LOCKED'));
  clock.advance(900_000);
  assert.ok((await auth.login({ email: u.email, password })).accessToken);
});

test('profile update touches only the authenticated account and disabled account access fails', async () => {
  const a = await createActive(); const b = await createActive();
  await auth.updateMe(a.principal, 'Renamed User');
  assert.equal((await db.user.findUnique({ where: { id: a.principal.userId } })).fullName, 'Renamed User');
  assert.equal((await db.user.findUnique({ where: { id: b.principal.userId } })).fullName, 'Integration Person');
  await db.user.update({ where: { id: a.principal.userId }, data: { status: 'DISABLED' } });
  await assert.rejects(auth.authenticate(a.session.accessToken), hasCode('AUTH_ACCOUNT_DISABLED'));
  await assert.rejects(auth.refresh(a.session.refreshToken), hasCode('AUTH_REFRESH_INVALID'));
});
