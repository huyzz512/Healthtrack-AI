import { randomBytes, randomUUID } from 'node:crypto';
import { readFile } from 'node:fs/promises';
import { jest } from '@jest/globals';
import { Test } from '@nestjs/testing';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { memoryAuth } from './support/memory-auth.mjs';
import { configureApplication } from '../dist/app.setup.js';
import { AuthApplicationService } from '../dist/modules/auth/auth.service.js';
import { AuthCrypto } from '../dist/modules/auth/auth.crypto.js';
import { AuthClock, AuthRepository } from '../dist/modules/auth/auth.port.js';
import { AccountUnitOfWork } from '../dist/modules/users/account.port.js';
import { AuthController, UsersController } from '../dist/modules/auth/auth.controller.js';
import { AccessGuard, AuthThrottleGuard } from '../dist/modules/auth/auth.guard.js';
import { OTP_PROVIDER } from '../dist/infrastructure/providers/provider.tokens.js';

const password = 'Testing!234';
const config = new ConfigService({ JWT_ACCESS_SECRET: randomBytes(48).toString('hex'), JWT_REFRESH_SECRET: randomBytes(48).toString('hex') });
let app, auth, memory, clock, crypto, sent, deliveryFails, logger, base;

async function http(path, body, token, method = 'POST') {
  const response = await fetch(`${base}/api/v1/${path}`, { method, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) }, ...(body !== undefined ? { body: JSON.stringify(body) } : {}) });
  return { status: response.status, headers: response.headers, body: response.status === 204 ? null : await response.json() };
}
async function register(email = `person-${randomUUID()}@example.test`) {
  const challenge = await auth.register({ email, fullName: 'Test Person', password, confirmPassword: password });
  return { email, challenge, code: sent.at(-1).code };
}
async function active(email) {
  const user = await register(email);
  await auth.verifyOtp(user.challenge.challengeId, user.code);
  const session = await auth.login({ email: user.email, password });
  const principal = await auth.authenticate(session.accessToken);
  return { ...user, session, principal };
}
const errorCode = (code) => expect.objectContaining({ response: expect.objectContaining({ code }) });

beforeEach(async () => {
  let instant = Date.parse('2026-11-25T03:00:00Z');
  clock = { now: () => new Date(instant), advance: ms => { instant += ms; } };
  memory = memoryAuth(clock); sent = []; deliveryFails = false;
  crypto = new AuthCrypto(config);
  logger = { error: jest.fn() };
  const module = await Test.createTestingModule({
    controllers: [AuthController, UsersController],
    providers: [AuthApplicationService, AccessGuard, AuthThrottleGuard,
      { provide: AccountUnitOfWork, useValue: memory.uow },
      { provide: AuthRepository, useValue: memory.repository },
      { provide: AuthClock, useValue: clock },
      { provide: AuthCrypto, useValue: crypto },
      { provide: OTP_PROVIDER, useValue: { async deliver(request) { sent.push(request); if (deliveryFails) throw new Error('provider-secret'); return { accepted: true, messageId: 'test', provider: 'test-only' }; } } },
    ],
  }).compile();
  app = module.createNestApplication({ logger: false });
  configureApplication(app, logger);
  await app.listen(0, '127.0.0.1');
  base = await app.getUrl();
  auth = module.get(AuthApplicationService);
});
afterEach(async () => { await app?.close(); });

describe('Backend Phase 1 Auth', () => {
  it('registers normalized email, hashes secrets and verifies without issuing a session', async () => {
    const result = await http('auth/register', { email: ' Person@Example.Test ', fullName: ' Test Person ', password, confirmPassword: password });
    expect(result.status).toBe(201);
    expect(result.headers.get('cache-control')).toBe('no-store');
    expect(Object.keys(result.body)).toEqual(['data']);
    const account = [...memory.state.accounts.values()][0];
    expect(account.email).toBe('person@example.test');
    expect(account.credential.passwordHash).toMatch(/^\$argon2id\$/);
    expect([...memory.state.challenges.values()][0].codeHash).not.toBe(sent[0].code);
    const verified = await http('auth/verify-otp', { challengeId: result.body.data.challengeId, code: sent[0].code });
    expect(verified).toMatchObject({ status: 200, body: { data: { verified: true } } });
    expect(memory.state.tokens.size).toBe(0);
    const login = await http('auth/login', { email: account.email, password });
    expect(login.status).toBe(200);
    expect([...memory.state.tokens.values()][0].tokenHash).not.toBe(login.body.data.refreshToken);
  });
  it('supports Vietnam phone registration and rejects mixed identities', async () => {
    const c = await auth.register({ phone: '0912345678', fullName: 'Phone User', password, confirmPassword: password });
    expect(sent[0].channel).toBe('sms');
    await auth.verifyOtp(c.challengeId, sent[0].code);
    expect((await auth.login({ phone: '0912345678', password })).tokenType).toBe('Bearer');
    await expect(auth.login({ phone: '0912345678', email: 'a@b.test', password })).rejects.toEqual(errorCode('VALIDATION_FAILED'));
  });
  it.each([
    { email: 'bad', fullName: 'Name', password, confirmPassword: password },
    { email: 'a@b.test', fullName: 'N', password, confirmPassword: password },
    { email: 'a@b.test', fullName: 'Name', password: 'weak', confirmPassword: 'weak' },
    { email: 'a@b.test', fullName: 'Name', password, confirmPassword: 'Different!1' },
    { email: 'a@b.test', fullName: 'Name', password, confirmPassword: password, userId: randomUUID() },
    { email: null, fullName: 'Name', password, confirmPassword: password },
    { email: null, phone: '0912345678', fullName: 'Name', password, confirmPassword: password },
  ])('rejects invalid registration before storing anything (%j)', async body => {
    expect((await http('auth/register', body)).status).toBe(400);
    expect(memory.state.accounts.size).toBe(0);
  });
  it('does not overwrite a pending identity on duplicate registration', async () => {
    await register('same@example.test');
    await expect(register('same@example.test')).rejects.toEqual(errorCode('AUTH_IDENTITY_UNAVAILABLE'));
    expect(memory.state.accounts.size).toBe(1);
    expect(sent).toHaveLength(1);
  });
  it('keeps failed OTP attempts committed, then enforces the five-entry bound', async () => {
    const u = await register(); const wrong = u.code === '000000' ? '000001' : '000000';
    for (let i = 0; i < 5; i++) await expect(auth.verifyOtp(u.challenge.challengeId, wrong)).rejects.toEqual(errorCode('AUTH_OTP_INVALID'));
    expect(memory.state.challenges.get(u.challenge.challengeId).failedAttempts).toBe(5);
    await expect(auth.verifyOtp(u.challenge.challengeId, u.code)).rejects.toEqual(errorCode('AUTH_OTP_ATTEMPTS_EXCEEDED'));
  });
  it('expires OTP exactly at the boundary and prevents consuming it twice', async () => {
    const u = await register(); clock.advance(300_000);
    await expect(auth.verifyOtp(u.challenge.challengeId, u.code)).rejects.toEqual(errorCode('AUTH_OTP_EXPIRED'));
    await auth.resendOtp(u.challenge.challengeId);
    const code = sent.at(-1).code;
    const results = await Promise.allSettled([auth.verifyOtp(u.challenge.challengeId, code), auth.verifyOtp(u.challenge.challengeId, code)]);
    expect(results.filter(r => r.status === 'fulfilled')).toHaveLength(1);
  });
  it('enforces resend cooldown, invalidates old codes and preserves the three-resend bound', async () => {
    const u = await register();
    const wait = await http('auth/resend-otp', { challengeId: u.challenge.challengeId });
    expect(wait.status).toBe(429); expect(wait.headers.get('retry-after')).toBe('60');
    for (let i = 0; i < 3; i++) { clock.advance(60_000); await auth.resendOtp(u.challenge.challengeId); }
    expect(memory.state.challenges.get(u.challenge.challengeId).resendCount).toBe(3);
    clock.advance(60_000);
    await expect(auth.resendOtp(u.challenge.challengeId)).rejects.toEqual(errorCode('AUTH_OTP_RESEND_LIMIT'));
    if (u.code !== sent.at(-1).code) await expect(auth.verifyOtp(u.challenge.challengeId, u.code)).rejects.toEqual(errorCode('AUTH_OTP_INVALID'));
  });
  it('keeps the account pending and invalidates the code after provider failure', async () => {
    deliveryFails = true;
    await expect(register()).rejects.toEqual(errorCode('AUTH_OTP_UNAVAILABLE'));
    const c = [...memory.state.challenges.values()][0];
    expect(c.invalidatedAt).not.toBeNull();
    expect([...memory.state.accounts.values()][0].status).toBe('PENDING_VERIFICATION');
    await expect(auth.verifyOtp(c.id, sent[0].code)).rejects.toEqual(errorCode('AUTH_OTP_INVALID'));
    deliveryFails = false; clock.advance(60_000);
    await auth.resendOtp(c.id); await auth.verifyOtp(c.id, sent.at(-1).code);
  });
  it('uses generic credentials errors and locks after five failures until the exact boundary', async () => {
    const u = await active();
    await expect(auth.login({ email: 'missing@example.test', password })).rejects.toEqual(errorCode('AUTH_INVALID_CREDENTIALS'));
    for (let i = 0; i < 4; i++) await expect(auth.login({ email: u.email, password: 'Wrong!123' })).rejects.toEqual(errorCode('AUTH_INVALID_CREDENTIALS'));
    await expect(auth.login({ email: u.email, password: 'Wrong!123' })).rejects.toEqual(errorCode('AUTH_ACCOUNT_LOCKED'));
    await expect(auth.login({ email: u.email, password })).rejects.toEqual(errorCode('AUTH_ACCOUNT_LOCKED'));
    clock.advance(900_000);
    expect((await auth.login({ email: u.email, password })).accessToken).toBeDefined();
  });
  it('returns the lost pending challenge only after password proof', async () => {
    const u = await register();
    const result = await http('auth/login', { email: u.email, password });
    expect(result.body.error).toMatchObject({ code: 'AUTH_ACCOUNT_UNVERIFIED', details: { challengeId: u.challenge.challengeId } });
    expect(memory.state.tokens.size).toBe(0);
  });
  it('rotates tokens and commits family revocation when an old token is reused', async () => {
    const u = await active();
    const rotated = await auth.refresh(u.session.refreshToken);
    expect(rotated.refreshToken).not.toBe(u.session.refreshToken);
    await expect(auth.refresh(u.session.refreshToken)).rejects.toEqual(errorCode('AUTH_REFRESH_INVALID'));
    await expect(auth.authenticate(rotated.accessToken)).rejects.toEqual(errorCode('AUTH_TOKEN_INVALID'));
    await expect(auth.refresh(rotated.refreshToken)).rejects.toEqual(errorCode('AUTH_REFRESH_INVALID'));
  });
  it('serializes concurrent refresh and invalidates the racing successful child', async () => {
    const u = await active();
    const results = await Promise.allSettled([auth.refresh(u.session.refreshToken), auth.refresh(u.session.refreshToken)]);
    expect(results.filter(r => r.status === 'fulfilled')).toHaveLength(1);
    expect([...memory.state.tokens.values()].every(t => t.revokedAt)).toBe(true);
  });
  it('CURRENT logout preserves another session; ALL revokes it immediately', async () => {
    const u = await active();
    const second = await auth.login({ email: u.email, password });
    const p2 = await auth.authenticate(second.accessToken);
    expect((await http('auth/logout', { scope: 'CURRENT' }, u.session.accessToken)).status).toBe(204);
    await expect(auth.authenticate(u.session.accessToken)).rejects.toEqual(errorCode('AUTH_TOKEN_INVALID'));
    await auth.authenticate(second.accessToken);
    await auth.logout(p2, 'ALL');
    await expect(auth.authenticate(second.accessToken)).rejects.toEqual(errorCode('AUTH_TOKEN_INVALID'));
  });
  it('derives me ownership from the bearer token and rejects mass assignment', async () => {
    const a = await active(); const b = await active();
    const me = await http('users/me', undefined, a.session.accessToken, 'GET');
    expect(me.body.data.id).toBe(a.principal.userId);
    expect(me.body.data).not.toHaveProperty('credential');
    expect((await http('users/me', { fullName: 'Updated', userId: b.principal.userId }, a.session.accessToken, 'PATCH')).status).toBe(400);
    expect((await http('users/me', { fullName: 'Updated' }, a.session.accessToken, 'PATCH')).body.data.fullName).toBe('Updated');
    expect(memory.state.accounts.get(b.principal.userId).fullName).toBe('Test Person');
    expect((await http('users/me', undefined, undefined, 'GET')).status).toBe(401);
  });
  it('rejects expired, tampered and wrong-audience access tokens', async () => {
    const u = await active();
    await expect(auth.authenticate(`${u.session.accessToken}bad`)).rejects.toEqual(errorCode('AUTH_TOKEN_INVALID'));
    const wrongAudience = new JwtService().sign({ sub: u.principal.userId, sid: u.principal.familyId }, { secret: config.get('JWT_ACCESS_SECRET'), issuer: 'healthtrack-backend', audience: 'wrong', expiresIn: '15m' });
    await expect(auth.authenticate(wrongAudience)).rejects.toEqual(errorCode('AUTH_TOKEN_INVALID'));
    clock.advance(900_000);
    await expect(auth.authenticate(u.session.accessToken)).rejects.toEqual(errorCode('AUTH_TOKEN_EXPIRED'));
  });
  it('blocks disabled/deleted accounts on existing sessions', async () => {
    const u = await active(); memory.state.accounts.get(u.principal.userId).status = 'DISABLED';
    await expect(auth.authenticate(u.session.accessToken)).rejects.toEqual(errorCode('AUTH_ACCOUNT_DISABLED'));
    await expect(auth.refresh(u.session.refreshToken)).rejects.toEqual(errorCode('AUTH_REFRESH_INVALID'));
  });
  it('recovers a password with opaque challenge, consumes once and revokes all sessions', async () => {
    const u = await active();
    const recovery = await auth.forgotPassword({ email: u.email });
    const code = sent.at(-1).code;
    expect(recovery.challengeId).not.toBe(sent.at(-1).challengeId);
    await auth.resetPassword({ challengeId: recovery.challengeId, code, password: 'Changed!234', confirmPassword: 'Changed!234' });
    await expect(auth.authenticate(u.session.accessToken)).rejects.toEqual(errorCode('AUTH_TOKEN_INVALID'));
    await expect(auth.login({ email: u.email, password })).rejects.toEqual(errorCode('AUTH_INVALID_CREDENTIALS'));
    expect((await auth.login({ email: u.email, password: 'Changed!234' })).accessToken).toBeDefined();
    await expect(auth.resetPassword({ challengeId: recovery.challengeId, code, password, confirmPassword: password })).rejects.toEqual(errorCode('AUTH_OTP_INVALID'));
  }, 10_000);
  it('does not expose account existence in recovery responses or invalid-reset errors', async () => {
    const u = await active();
    const real = await auth.forgotPassword({ email: u.email });
    const fake = await auth.forgotPassword({ email: 'missing@example.test' });
    expect(Object.keys(real)).toEqual(Object.keys(fake));
    expect(real.challengeId.length).toBe(fake.challengeId.length);
    expect(real.resendsRemaining).toBe(fake.resendsRemaining);
    for (const c of [real, fake]) {
      const result = await http('auth/reset-password', { challengeId: c.challengeId, code: 'not-six-digits', password, confirmPassword: password });
      expect(result.status).toBe(400);
    }
    await expect(auth.resetPassword({ challengeId: fake.challengeId, code: '123456', password, confirmPassword: password })).rejects.toEqual(errorCode('AUTH_OTP_INVALID'));
  }, 10_000);
  it('rejects OTP purpose confusion and recovers an abandoned pending registration with contact proof', async () => {
    const u = await register(); const recovery = await auth.forgotPassword({ email: u.email });
    const delivery = sent.at(-1);
    await expect(auth.verifyOtp(delivery.challengeId, delivery.code)).rejects.toEqual(errorCode('AUTH_OTP_INVALID'));
    await auth.resetPassword({ challengeId: recovery.challengeId, code: delivery.code, password: 'Changed!234', confirmPassword: 'Changed!234' });
    expect((await auth.login({ email: u.email, password: 'Changed!234' })).accessToken).toBeDefined();
    await expect(auth.verifyOtp(u.challenge.challengeId, u.code)).rejects.toEqual(errorCode('AUTH_OTP_INVALID'));
  }, 10_000);
  it('redacts query strings and submitted credentials from error logs', async () => {
    await http('auth/login?token=sensitive-query-value', { email: 'bad', password: 'sensitive-password' });
    const logs = JSON.stringify(logger.error.mock.calls);
    expect(logs).not.toContain('sensitive-query-value'); expect(logs).not.toContain('sensitive-password');
  });
  it('enforces the per-IP registration throttle independently of account state', async () => {
    for (let i = 0; i < 5; i++) await http('auth/register', {});
    const result = await http('auth/register', {});
    expect(result.status).toBe(429); expect(result.headers.get('retry-after')).toBe('900');
  });
  it('fails closed when signing secrets are missing or placeholders', () => {
    const bad = new AuthCrypto(new ConfigService({ JWT_ACCESS_SECRET: 'replace-with-at-least-32-random-characters' }));
    expect(() => bad.ready()).toThrow();
  });
  it('rejects refresh exactly at its 30-day expiry', async () => {
    const u = await active(); clock.advance(30 * 24 * 60 * 60_000);
    await expect(auth.refresh(u.session.refreshToken)).rejects.toEqual(errorCode('AUTH_REFRESH_INVALID'));
    expect(memory.state.tokens.size).toBe(1);
  });
  it('caps wrong reset attempts and gives real and decoy handles the same failure', async () => {
    const u = await active(); const c = await auth.forgotPassword({ email: u.email });
    const realCode = sent.at(-1).code;
    const wrongCode = realCode === '000000' ? '000001' : '000000';
    for (let i = 0; i < 5; i++) {
      const response = await http('auth/reset-password', { challengeId: c.challengeId, code: wrongCode, password, confirmPassword: password });
      expect(response.body.error).toMatchObject({ code: 'AUTH_OTP_INVALID', details: null });
    }
    await expect(auth.resetPassword({ challengeId: c.challengeId, code: realCode, password, confirmPassword: password })).rejects.toEqual(errorCode('AUTH_OTP_INVALID'));
    const resetRow = [...memory.state.challenges.values()].find(c => c.purpose === 'PASSWORD_RESET');
    expect(resetRow.failedAttempts).toBe(5);
    const decoy = crypto.sealRecovery(randomUUID(), new Date(clock.now().getTime() + 300_000));
    const response = await http('auth/reset-password', { challengeId: decoy, code: wrongCode, password, confirmPassword: password });
    expect(response.body.error).toMatchObject({ code: 'AUTH_OTP_INVALID', details: null });
  }, 10_000);
  it('keeps the current password and sessions when reset delivery fails', async () => {
    const u = await active(); deliveryFails = true;
    const result = await http('auth/forgot-password', { email: u.email });
    expect(result.status).toBe(200);
    await expect(auth.resetPassword({ challengeId: result.body.data.challengeId, code: sent.at(-1).code, password: 'Changed!234', confirmPassword: 'Changed!234' })).rejects.toEqual(errorCode('AUTH_OTP_INVALID'));
    await auth.authenticate(u.session.accessToken);
    expect((await auth.login({ email: u.email, password })).accessToken).toBeDefined();
  }, 10_000);
  it('expires and authenticates recovery handles without accepting altered ciphertext', async () => {
    const u = await active(); const c = await auth.forgotPassword({ email: u.email });
    const code = sent.at(-1).code;
    const bytes = Buffer.from(c.challengeId, 'base64url'); bytes[20] ^= 1;
    await expect(auth.resetPassword({ challengeId: bytes.toString('base64url'), code, password, confirmPassword: password })).rejects.toEqual(errorCode('AUTH_OTP_INVALID'));
    clock.advance(300_000);
    await expect(auth.resetPassword({ challengeId: c.challengeId, code, password, confirmPassword: password })).rejects.toEqual(errorCode('AUTH_OTP_INVALID'));
  }, 10_000);
  it('generates all ten Auth/Users operations and fixtures matching actual response fields', async () => {
    const response = await fetch(`${base}/api/docs-json`); const spec = await response.json();
    const operations = [
      ['post', 'auth/register'], ['post', 'auth/verify-otp'], ['post', 'auth/resend-otp'],
      ['post', 'auth/login'], ['post', 'auth/refresh'], ['post', 'auth/logout'],
      ['post', 'auth/forgot-password'], ['post', 'auth/reset-password'], ['get', 'users/me'], ['patch', 'users/me'],
    ];
    for (const [method, path] of operations) expect(spec.paths[`/api/v1/${path}`][method]).toBeDefined();
    expect(spec.paths['/api/v1/auth/logout'].post.responses['204'].content).toBeUndefined();
    expect(spec.paths['/api/v1/users/me'].get.security).toEqual([{ bearer: [] }]);
    const fixtures = JSON.parse(await readFile(new URL('./fixtures/auth-contract.json', import.meta.url), 'utf8'));
    const u = await active(); const me = await auth.getMe(u.principal);
    expect(Object.keys(u.session).sort()).toEqual(Object.keys(fixtures.loginResponse.data).sort());
    expect(Object.keys(me).sort()).toEqual(Object.keys(fixtures.meResponse.data).sort());
    expect(Object.keys(u.challenge).sort()).toEqual(Object.keys(fixtures.registrationResponse.data).sort());
    expect(spec.components.schemas.RegisterDto.required).toContain('confirmPassword');
  });
});
