import { Inject, Injectable } from '@nestjs/common';
import type { CanActivate, ExecutionContext } from '@nestjs/common';
import type { Request, Response } from 'express';
import { authError } from './auth.crypto.js';
import type { Principal } from './auth.crypto.js';
import { AuthApplicationService } from './auth.service.js';
import { AuthClock } from './auth.port.js';

export interface AuthenticatedRequest extends Request { principal: Principal }

@Injectable()
export class AccessGuard implements CanActivate {
  constructor(@Inject(AuthApplicationService) private readonly auth: AuthApplicationService) {}
  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest<AuthenticatedRequest>();
    const authorization = request.headers.authorization ?? '';
    const match = /^Bearer ([^\s]+)$/i.exec(authorization);
    if (!match?.[1]) throw authError(401, 'AUTH_TOKEN_INVALID');
    request.principal = await this.auth.authenticate(match[1]);
    return true;
  }
}

// Bounded per-process abuse throttle for the local Phase 1 deployment. Account
// and OTP counters are persisted separately. A shared limiter is a production
// prerequisite; never trust X-Forwarded-For without an approved proxy policy.
@Injectable()
export class AuthThrottleGuard implements CanActivate {
  private readonly buckets = new Map<string, { count: number; until: number }>();
  constructor(@Inject(AuthClock) private readonly clock: AuthClock) {}
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest<Request>();
    const response = context.switchToHttp().getResponse<Response>();
    response.setHeader('Cache-Control', 'no-store');
    const operation = context.getHandler().name;
    const tight = ['register', 'forgotPassword'].includes(operation);
    const limit = tight ? 5 : operation === 'refresh' ? 60 : 30;
    const windowMs = tight ? 15 * 60_000 : 60_000;
    const now = this.clock.now().getTime();
    for (const [key, bucket] of this.buckets) if (bucket.until <= now) this.buckets.delete(key);
    const key = `${operation}:${request.socket.remoteAddress ?? 'unknown'}`;
    let bucket = this.buckets.get(key);
    if (!bucket) {
      if (this.buckets.size >= 10_000) throw authError(429, 'AUTH_RATE_LIMITED', { retryAfterSeconds: 60 });
      bucket = { count: 0, until: now + windowMs };
      this.buckets.set(key, bucket);
    }
    if (bucket.count >= limit) throw authError(429, 'AUTH_RATE_LIMITED', { retryAfterSeconds: Math.ceil((bucket.until - now) / 1000) });
    bucket.count++;
    return true;
  }
}
