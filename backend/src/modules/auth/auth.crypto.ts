import { createCipheriv, createDecipheriv, createHash, createHmac, randomBytes, randomInt, timingSafeEqual } from 'node:crypto';
import { HttpException, Inject, Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import * as argon2 from 'argon2';

export const ACCESS_MS = 15 * 60_000;
export const REFRESH_MS = 30 * 24 * 60 * 60_000;
export const OTP_MS = 5 * 60_000;
export interface Principal { userId: string; familyId: string }

export function authError(status: number, code: string, details: Record<string, unknown> | null = null): HttpException {
  // Stable public messages never include caller-supplied values or internal errors.
  return new HttpException({ code, message: code.replaceAll('_', ' ').toLowerCase(), details }, status);
}

@Injectable()
export class AuthCrypto {
  private readonly jwt = new JwtService();
  constructor(@Inject(ConfigService) private readonly config: ConfigService) {}
  private secret(name: string): string {
    const value = this.config.get<string>(name) ?? '';
    if (Buffer.byteLength(value) < 32 || value.startsWith('replace-')) {
      throw authError(503, 'AUTH_CONFIGURATION_UNAVAILABLE');
    }
    return value;
  }
  ready(): void {
    if (this.secret('JWT_ACCESS_SECRET') === this.secret('JWT_REFRESH_SECRET')) throw authError(503, 'AUTH_CONFIGURATION_UNAVAILABLE');
  }
  hashPassword(password: string): Promise<string> {
    return argon2.hash(password, { type: argon2.argon2id, memoryCost: 19_456, timeCost: 2, parallelism: 1 });
  }
  async passwordMatches(hash: string, password: string): Promise<boolean> {
    try { return await argon2.verify(hash, password); } catch { return false; }
  }
  newCode(): string { return randomInt(0, 1_000_000).toString().padStart(6, '0'); }
  codeHash(id: string, code: string): string {
    return createHmac('sha256', this.secret('JWT_REFRESH_SECRET')).update(`otp:${id}:${code}`).digest('hex');
  }
  codeMatches(id: string, code: string, expected: string): boolean {
    const actual = Buffer.from(this.codeHash(id, code), 'hex');
    const wanted = Buffer.from(expected, 'hex');
    return actual.length === wanted.length && timingSafeEqual(actual, wanted);
  }
  newRefresh(): string { return randomBytes(32).toString('base64url'); }
  refreshHash(token: string): string { return createHash('sha256').update(token).digest('hex'); }
  access(principal: Principal, now: Date): string {
    return this.jwt.sign({ sub: principal.userId, sid: principal.familyId, iat: Math.floor(now.getTime() / 1000), exp: Math.floor((now.getTime() + ACCESS_MS) / 1000) }, {
      secret: this.secret('JWT_ACCESS_SECRET'), algorithm: 'HS256', issuer: 'healthtrack-backend', audience: 'healthtrack-app',
    });
  }
  verifyAccess(token: string, now: Date): Principal {
    const secret = this.secret('JWT_ACCESS_SECRET');
    try {
      const claims = this.jwt.verify<Record<string, unknown>>(token, { secret, algorithms: ['HS256'], issuer: 'healthtrack-backend', audience: 'healthtrack-app', clockTimestamp: Math.floor(now.getTime() / 1000) });
      const uuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
      if (typeof claims.sub !== 'string' || !uuid.test(claims.sub) || typeof claims.sid !== 'string' || !uuid.test(claims.sid) || typeof claims.exp !== 'number') throw new Error('Invalid claims');
      return { userId: claims.sub, familyId: claims.sid };
    } catch (error) {
      throw authError(401, error instanceof Error && error.name === 'TokenExpiredError' ? 'AUTH_TOKEN_EXPIRED' : 'AUTH_TOKEN_INVALID');
    }
  }
  // Opaque encrypted recovery handles conceal whether there is a database row.
  // No schema change or placeholder user is needed for unknown identities.
  sealRecovery(id: string, expiresAt: Date): string {
    const iv = randomBytes(12);
    const key = createHash('sha256').update(`recovery:${this.secret('JWT_REFRESH_SECRET')}`).digest();
    const cipher = createCipheriv('aes-256-gcm', key, iv);
    const body = Buffer.concat([cipher.update(JSON.stringify({ id, exp: expiresAt.getTime() }), 'utf8'), cipher.final()]);
    return Buffer.concat([iv, cipher.getAuthTag(), body]).toString('base64url');
  }
  openRecovery(value: string, now: Date): string | null {
    const key = createHash('sha256').update(`recovery:${this.secret('JWT_REFRESH_SECRET')}`).digest();
    try {
      const bytes = Buffer.from(value, 'base64url');
      const decipher = createDecipheriv('aes-256-gcm', key, bytes.subarray(0, 12));
      decipher.setAuthTag(bytes.subarray(12, 28));
      const decoded: unknown = JSON.parse(Buffer.concat([decipher.update(bytes.subarray(28)), decipher.final()]).toString('utf8'));
      if (!decoded || typeof decoded !== 'object' || !('id' in decoded) || !('exp' in decoded) || typeof decoded.id !== 'string' || !/^[0-9a-f-]{36}$/i.test(decoded.id) || typeof decoded.exp !== 'number' || decoded.exp <= now.getTime()) return null;
      return decoded.id;
    } catch { return null; }
  }
}
