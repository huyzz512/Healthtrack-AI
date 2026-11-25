import { randomUUID } from 'node:crypto';
import { HttpException, Inject, Injectable } from '@nestjs/common';
import { isEmail } from 'class-validator';
import { OTP_PROVIDER } from '../../infrastructure/providers/provider.tokens.js';
import type { OTPProvider } from '../../infrastructure/providers/otp/otp-provider.js';
import { AccountUnitOfWork } from '../users/account.port.js';
import type { Account, Accounts, Identity } from '../users/account.port.js';
import { AuthClock, AuthRepository } from './auth.port.js';
import type { AuthRecords, Challenge } from './auth.port.js';
import { ACCESS_MS, AuthCrypto, authError, OTP_MS, REFRESH_MS } from './auth.crypto.js';
import type { Principal } from './auth.crypto.js';

type Registration = Identity & { fullName: string; password: string; confirmPassword: string };
type ChallengeResult = { challengeId: string; expiresAt: string; resendAvailableAt: string; resendsRemaining: number };

@Injectable()
export class SystemAuthClock extends AuthClock { now(): Date { return new Date(); } }

function unwrap<T>(result: T | HttpException): T {
  if (result instanceof HttpException) throw result;
  return result;
}

@Injectable()
export class AuthApplicationService {
  constructor(
    @Inject(AccountUnitOfWork) private readonly uow: AccountUnitOfWork,
    @Inject(AuthRepository) private readonly repository: AuthRepository,
    @Inject(AuthCrypto) private readonly crypto: AuthCrypto,
    @Inject(AuthClock) private readonly clock: AuthClock,
    @Inject(OTP_PROVIDER) private readonly otp: OTPProvider,
  ) {}

  private identity(input: Identity): Identity {
    if ((input.email !== undefined && typeof input.email !== 'string') || (input.phone !== undefined && typeof input.phone !== 'string')) throw authError(400, 'VALIDATION_FAILED');
    if ((typeof input.email === 'string') === (typeof input.phone === 'string')) throw authError(400, 'VALIDATION_FAILED');
    if (input.email !== undefined) {
      const email = input.email.trim().toLowerCase();
      if (email.length > 320 || !isEmail(email)) throw authError(400, 'VALIDATION_FAILED');
      return { email };
    }
    const phone = input.phone?.trim() ?? '';
    if (!/^0[0-9]{9}$/.test(phone)) throw authError(400, 'VALIDATION_FAILED');
    return { phone };
  }
  private name(value: string): string {
    const name = value.trim();
    if ([...name].length < 2 || [...name].length > 80) throw authError(400, 'VALIDATION_FAILED');
    return name;
  }
  private password(value: string, confirm: string): void {
    if (value !== confirm || [...value].length < 8 || [...value].length > 30 || !/[A-Z]/.test(value) || !/[a-z]/.test(value) || !/[0-9]/.test(value) || !/[^A-Za-z0-9\s]/u.test(value)) throw authError(400, 'VALIDATION_FAILED');
  }
  private viewChallenge(challenge: Challenge): ChallengeResult {
    return { challengeId: challenge.id, expiresAt: challenge.expiresAt.toISOString(), resendAvailableAt: new Date(challenge.expiresAt.getTime() - OTP_MS + 60_000).toISOString(), resendsRemaining: Math.max(0, 3 - challenge.resendCount) };
  }
  private makeChallenge(account: Account, purpose: Challenge['purpose'], now: Date) {
    const id = randomUUID();
    const code = this.crypto.newCode();
    const challenge: Challenge = { id, userId: account.id, channel: account.email ? 'EMAIL' : 'SMS', purpose, codeHash: this.crypto.codeHash(id, code), failedAttempts: 0, resendCount: 0, expiresAt: new Date(now.getTime() + OTP_MS), consumedAt: null, invalidatedAt: null, createdAt: now };
    return { challenge, code, recipient: account.email ?? account.phone! };
  }
  private async deliver(pending: { challenge: Challenge; code: string; recipient: string }, timeoutMs = 5_000): Promise<boolean> {
    let timer: ReturnType<typeof setTimeout> | undefined;
    try {
      const result = await Promise.race([
        this.otp.deliver({ challengeId: pending.challenge.id, channel: pending.challenge.channel === 'EMAIL' ? 'email' : 'sms', recipient: pending.recipient, code: pending.code, expiresAt: pending.challenge.expiresAt }),
        new Promise<never>((_, reject) => { timer = setTimeout(() => reject(new Error('OTP timeout')), timeoutMs); }),
      ]);
      if (result.accepted) return true;
    } catch { /* The public failure is deliberately independent of provider details. */ }
    finally { if (timer) clearTimeout(timer); }
    await this.uow.run(`account:${pending.challenge.userId}`, async (_accounts, scope) => {
      const records = this.repository.in(scope);
      const current = await records.challenge(pending.challenge.id);
      if (current?.codeHash === pending.challenge.codeHash && !current.consumedAt) await records.updateChallenge(current.id, { invalidatedAt: this.clock.now() });
    });
    return false;
  }

  async register(input: Registration): Promise<ChallengeResult> {
    this.crypto.ready();
    const identity = this.identity(input);
    const fullName = this.name(input.fullName);
    this.password(input.password, input.confirmPassword);
    const passwordHash = await this.crypto.hashPassword(input.password);
    const pending = unwrap(await this.uow.run(`identity:${identity.email ?? identity.phone}`, async (accounts, scope) => {
      const existing = await accounts.find(identity);
      if (existing) return authError(409, 'AUTH_IDENTITY_UNAVAILABLE');
      const account = await accounts.create({ ...identity, fullName, passwordHash });
      const pending = this.makeChallenge(account, 'REGISTRATION', this.clock.now());
      await this.repository.in(scope).createChallenge(pending.challenge);
      return pending;
    }));
    if (!await this.deliver(pending)) throw authError(503, 'AUTH_OTP_UNAVAILABLE', { challengeId: pending.challenge.id });
    return this.viewChallenge(pending.challenge);
  }

  private otpFailure(challenge: Challenge, code: string, now: Date): HttpException | null {
    if (challenge.consumedAt || challenge.invalidatedAt) return authError(422, 'AUTH_OTP_INVALID');
    if (challenge.expiresAt <= now) return authError(422, 'AUTH_OTP_EXPIRED');
    if (challenge.failedAttempts >= 5) return authError(422, 'AUTH_OTP_ATTEMPTS_EXCEEDED');
    if (!this.crypto.codeMatches(challenge.id, code, challenge.codeHash)) return authError(422, 'AUTH_OTP_INVALID', { attemptsRemaining: Math.max(0, 4 - challenge.failedAttempts) });
    return null;
  }

  async verifyOtp(challengeId: string, code: string) {
    const known = await this.uow.read(async (_accounts, scope) => this.repository.in(scope).challenge(challengeId));
    if (!known) throw authError(422, 'AUTH_OTP_INVALID');
    return unwrap(await this.uow.run(`account:${known.userId}`, async (accounts, scope) => {
      const records = this.repository.in(scope);
      const challenge = await records.challenge(challengeId);
      const account = await accounts.get(known.userId);
      if (!challenge || challenge.purpose !== 'REGISTRATION' || !account || account.deletedAt || account.status !== 'PENDING_VERIFICATION') return authError(422, 'AUTH_OTP_INVALID');
      const now = this.clock.now();
      const failure = this.otpFailure(challenge, code, now);
      if (failure) {
        if (!challenge.consumedAt && !challenge.invalidatedAt && challenge.expiresAt > now && challenge.failedAttempts < 5) await records.updateChallenge(challenge.id, { failedAttempts: challenge.failedAttempts + 1 });
        return failure;
      }
      await accounts.activate(account.id, challenge.channel, now);
      await records.updateChallenge(challenge.id, { consumedAt: now });
      return { verified: true };
    }));
  }

  async resendOtp(challengeId: string): Promise<ChallengeResult> {
    this.crypto.ready();
    const known = await this.uow.read(async (_accounts, scope) => this.repository.in(scope).challenge(challengeId));
    if (!known) throw authError(422, 'AUTH_OTP_INVALID');
    const pending = unwrap(await this.uow.run(`account:${known.userId}`, async (accounts, scope) => {
      const records = this.repository.in(scope);
      const old = await records.challenge(challengeId);
      const account = await accounts.get(known.userId);
      if (!old || old.purpose !== 'REGISTRATION' || old.consumedAt || !account || account.deletedAt || account.status !== 'PENDING_VERIFICATION') return authError(422, 'AUTH_OTP_INVALID');
      const now = this.clock.now();
      if (old.resendCount >= 3) return authError(422, 'AUTH_OTP_RESEND_LIMIT');
      const wait = old.expiresAt.getTime() - OTP_MS + 60_000 - now.getTime();
      if (wait > 0) return authError(429, 'AUTH_RATE_LIMITED', { retryAfterSeconds: Math.ceil(wait / 1000) });
      const code = this.crypto.newCode();
      const challenge = { ...old, codeHash: this.crypto.codeHash(old.id, code), failedAttempts: 0, resendCount: old.resendCount + 1, expiresAt: new Date(now.getTime() + OTP_MS), invalidatedAt: null };
      await records.updateChallenge(old.id, { codeHash: challenge.codeHash, failedAttempts: 0, resendCount: challenge.resendCount, expiresAt: challenge.expiresAt, invalidatedAt: null });
      return { challenge, code, recipient: account.email ?? account.phone! };
    }));
    if (!await this.deliver(pending)) throw authError(503, 'AUTH_OTP_UNAVAILABLE', { challengeId });
    return this.viewChallenge(pending.challenge);
  }

  private accountFailure(account: Account | null): HttpException | null {
    if (!account || account.deletedAt) return authError(401, 'AUTH_TOKEN_INVALID');
    if (account.status === 'LOCKED') return authError(403, 'AUTH_ACCOUNT_LOCKED');
    if (account.status === 'DISABLED') return authError(403, 'AUTH_ACCOUNT_DISABLED');
    if (account.status !== 'ACTIVE') return authError(403, 'AUTH_ACCOUNT_UNVERIFIED');
    return null;
  }
  private async issue(records: AuthRecords, account: Account, now: Date, familyId: string = randomUUID(), parentTokenId: string | null = null) {
    const accessToken = this.crypto.access({ userId: account.id, familyId }, now);
    const refreshToken = this.crypto.newRefresh();
    const expiresAt = new Date(now.getTime() + REFRESH_MS);
    await records.createRefresh({ id: randomUUID(), userId: account.id, tokenHash: this.crypto.refreshHash(refreshToken), familyId, parentTokenId, expiresAt, revokedAt: null, createdAt: now });
    return { accessToken, refreshToken, tokenType: 'Bearer' as const, accessTokenExpiresAt: new Date(now.getTime() + ACCESS_MS).toISOString(), refreshTokenExpiresAt: expiresAt.toISOString() };
  }

  async login(input: Identity & { password: string }) {
    this.crypto.ready();
    const identity = this.identity(input);
    const known = await this.uow.read((accounts) => accounts.find(identity));
    if (!known) {
      // Perform comparable password work without logging or persisting input.
      await this.crypto.hashPassword(input.password);
      throw authError(401, 'AUTH_INVALID_CREDENTIALS');
    }
    return unwrap(await this.uow.run(`account:${known.id}`, async (accounts, scope) => {
      const account = await accounts.get(known.id);
      if (!account?.credential || account.deletedAt) return authError(401, 'AUTH_INVALID_CREDENTIALS');
      const now = this.clock.now();
      const cred = account.credential;
      if (cred.lockedUntil && cred.lockedUntil > now) return authError(403, 'AUTH_ACCOUNT_LOCKED', { retryAfterSeconds: Math.ceil((cred.lockedUntil.getTime() - now.getTime()) / 1000) });
      if (!await this.crypto.passwordMatches(cred.passwordHash, input.password)) {
        const within = cred.failedLoginWindowAt && now.getTime() - cred.failedLoginWindowAt.getTime() < 15 * 60_000;
        const count = within ? cred.failedLoginCount + 1 : 1;
        await accounts.failures(account.id, count, within ? cred.failedLoginWindowAt : now, count >= 5 ? new Date(now.getTime() + 15 * 60_000) : null);
        return count >= 5 ? authError(403, 'AUTH_ACCOUNT_LOCKED', { retryAfterSeconds: 900 }) : authError(401, 'AUTH_INVALID_CREDENTIALS');
      }
      const failure = this.accountFailure(account);
      if (failure) {
        // Password proof lets the owner recover a lost registration challenge.
        if (account.status === 'PENDING_VERIFICATION') {
          const challenge = await this.repository.in(scope).latest(account.id, 'REGISTRATION');
          return authError(403, 'AUTH_ACCOUNT_UNVERIFIED', challenge ? this.viewChallenge(challenge) : null);
        }
        return failure;
      }
      await accounts.failures(account.id, 0, null, null);
      return this.issue(this.repository.in(scope), account, now);
    }));
  }

  async refresh(refreshToken: string) {
    this.crypto.ready();
    const hash = this.crypto.refreshHash(refreshToken);
    const known = await this.uow.read(async (_accounts, scope) => this.repository.in(scope).refresh(hash));
    if (!known) throw authError(401, 'AUTH_REFRESH_INVALID');
    return unwrap(await this.uow.run(`account:${known.userId}`, async (accounts, scope) => {
      const records = this.repository.in(scope);
      const token = await records.refresh(hash);
      const now = this.clock.now();
      if (!token) return authError(401, 'AUTH_REFRESH_INVALID');
      if (token.revokedAt) {
        await records.revokeFamily(token.userId, token.familyId, now);
        return authError(401, 'AUTH_REFRESH_INVALID');
      }
      const account = await accounts.get(token.userId);
      if (token.expiresAt <= now || this.accountFailure(account)) return authError(401, 'AUTH_REFRESH_INVALID');
      await records.revokeToken(token.id, now);
      return this.issue(records, account!, now, token.familyId, token.id);
    }));
  }

  private async requireSession(accounts: Accounts, records: AuthRecords, principal: Principal): Promise<Account> {
    const account = await accounts.get(principal.userId);
    const failure = this.accountFailure(account);
    if (failure) throw failure;
    if (!await records.liveFamily(principal.userId, principal.familyId, this.clock.now())) throw authError(401, 'AUTH_TOKEN_INVALID');
    return account!;
  }
  async authenticate(token: string): Promise<Principal> {
    const principal = this.crypto.verifyAccess(token, this.clock.now());
    await this.uow.read((accounts, scope) => this.requireSession(accounts, this.repository.in(scope), principal));
    return principal;
  }
  async logout(principal: Principal, scope: 'CURRENT' | 'ALL'): Promise<void> {
    await this.uow.run(`account:${principal.userId}`, async (accounts, transaction) => {
      const records = this.repository.in(transaction);
      await this.requireSession(accounts, records, principal);
      if (scope === 'ALL') await records.revokeAll(principal.userId, this.clock.now());
      else await records.revokeFamily(principal.userId, principal.familyId, this.clock.now());
    });
  }
  private me(account: Account) {
    return { id: account.id, fullName: account.fullName, email: account.email, phone: account.phone, emailVerifiedAt: account.emailVerifiedAt?.toISOString() ?? null, phoneVerifiedAt: account.phoneVerifiedAt?.toISOString() ?? null, createdAt: account.createdAt.toISOString(), updatedAt: account.updatedAt.toISOString() };
  }
  async getMe(principal: Principal) {
    return this.uow.read(async (accounts, scope) => this.me(await this.requireSession(accounts, this.repository.in(scope), principal)));
  }
  async updateMe(principal: Principal, fullName: string) {
    const name = this.name(fullName);
    return this.uow.run(`account:${principal.userId}`, async (accounts, scope) => {
      await this.requireSession(accounts, this.repository.in(scope), principal);
      return this.me(await accounts.rename(principal.userId, name));
    });
  }

  async forgotPassword(input: Identity): Promise<ChallengeResult> {
    this.crypto.ready();
    const identity = this.identity(input);
    const now = this.clock.now();
    const account = await this.uow.read((accounts) => accounts.find(identity));
    let id: string = randomUUID();
    // Both paths return fixed-shape encrypted handles; no eligibility detail.
    // Within cooldown return a decoy handle, never reveal an existing challenge.
    if (account && !account.deletedAt && (account.status === 'ACTIVE' || account.status === 'PENDING_VERIFICATION')) {
      const pending = await this.uow.run(`account:${account.id}`, async (accounts, scope) => {
        const current = await accounts.get(account.id);
        if (!current || current.deletedAt || !['ACTIVE', 'PENDING_VERIFICATION'].includes(current.status)) return null;
        const records = this.repository.in(scope);
        const latest = await records.latest(account.id, 'PASSWORD_RESET');
        if (latest && now.getTime() - latest.createdAt.getTime() < 60_000) return null;
        // Bound valid reset challenges/attempts per account, across client IPs.
        if (latest && now.getTime() - latest.createdAt.getTime() < 15 * 60_000 && latest.resendCount >= 3) return null;
        const pending = this.makeChallenge(current, 'PASSWORD_RESET', now);
        pending.challenge.resendCount = latest && now.getTime() - latest.createdAt.getTime() < 15 * 60_000 ? latest.resendCount + 1 : 0;
        await records.invalidateChallenges(account.id, 'PASSWORD_RESET', now);
        await records.createChallenge(pending.challenge);
        return pending;
      });
      if (pending) {
        id = pending.challenge.id;
        // Provider failures are suppressed only for this enumeration-resistant endpoint.
        await Promise.all([this.deliver(pending, 1_000), new Promise((resolve) => setTimeout(resolve, 1_000))]);
      } else await new Promise((resolve) => setTimeout(resolve, 1_000));
    } else await new Promise((resolve) => setTimeout(resolve, 1_000));
    const expiresAt = new Date(now.getTime() + OTP_MS);
    return { challengeId: this.crypto.sealRecovery(id, expiresAt), expiresAt: expiresAt.toISOString(), resendAvailableAt: new Date(now.getTime() + 60_000).toISOString(), resendsRemaining: 0 };
  }

  async resetPassword(input: { challengeId: string; code: string; password: string; confirmPassword: string }): Promise<void> {
    this.password(input.password, input.confirmPassword);
    const id = this.crypto.openRecovery(input.challengeId, this.clock.now());
    const passwordHash = await this.crypto.hashPassword(input.password);
    const failure = () => authError(422, 'AUTH_OTP_INVALID');
    if (!id) throw failure();
    const known = await this.uow.read(async (_accounts, scope) => this.repository.in(scope).challenge(id));
    if (!known) throw failure();
    unwrap(await this.uow.run(`account:${known.userId}`, async (accounts, scope) => {
      const records = this.repository.in(scope);
      const challenge = await records.challenge(id);
      const account = await accounts.get(known.userId);
      const now = this.clock.now();
      if (!challenge || challenge.purpose !== 'PASSWORD_RESET' || !account || account.deletedAt || !['ACTIVE', 'PENDING_VERIFICATION'].includes(account.status)) return failure();
      if (this.otpFailure(challenge, input.code, now)) {
        if (!challenge.consumedAt && !challenge.invalidatedAt && challenge.expiresAt > now && challenge.failedAttempts < 5) await records.updateChallenge(challenge.id, { failedAttempts: challenge.failedAttempts + 1 });
        return failure();
      }
      await accounts.password(account.id, passwordHash, now);
      await records.updateChallenge(challenge.id, { consumedAt: now });
      await records.invalidateChallenges(account.id, 'PASSWORD_RESET', now);
      await records.revokeAll(account.id, now);
      // Contact proof also recovers an abandoned pending registration. This
      // remains a registration challenge: no session is issued by reset.
      if (account.status === 'PENDING_VERIFICATION') {
        await records.invalidateChallenges(account.id, 'REGISTRATION', now);
        await accounts.activate(account.id, challenge.channel, now);
      }
    }));
  }
}
