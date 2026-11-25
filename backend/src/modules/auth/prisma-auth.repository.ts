import { Injectable } from '@nestjs/common';
import type { Prisma } from '../../generated/prisma/client.js';
import { AuthRepository } from './auth.port.js';
import type { AuthRecords, Challenge, Refresh } from './auth.port.js';

class PrismaAuthRecords implements AuthRecords {
  constructor(private readonly db: Prisma.TransactionClient) {}
  challenge(id: string) { return this.db.otpChallenge.findUnique({ where: { id } }); }
  latest(userId: string, purpose: Challenge['purpose']) {
    return this.db.otpChallenge.findFirst({ where: { userId, purpose }, orderBy: { createdAt: 'desc' } });
  }
  async createChallenge(value: Challenge) { await this.db.otpChallenge.create({ data: value }); }
  async updateChallenge(id: string, values: Partial<Omit<Challenge, 'id' | 'userId'>>) {
    await this.db.otpChallenge.update({ where: { id }, data: values });
  }
  async invalidateChallenges(userId: string, purpose: Challenge['purpose'], now: Date) {
    await this.db.otpChallenge.updateMany({ where: { userId, purpose, consumedAt: null, invalidatedAt: null }, data: { invalidatedAt: now } });
  }
  refresh(tokenHash: string) { return this.db.refreshToken.findUnique({ where: { tokenHash } }); }
  async createRefresh(value: Refresh) { await this.db.refreshToken.create({ data: value }); }
  async revokeToken(id: string, now: Date) { await this.db.refreshToken.update({ where: { id }, data: { revokedAt: now } }); }
  async revokeFamily(userId: string, familyId: string, now: Date) {
    await this.db.refreshToken.updateMany({ where: { userId, familyId, revokedAt: null }, data: { revokedAt: now } });
  }
  async revokeAll(userId: string, now: Date) {
    await this.db.refreshToken.updateMany({ where: { userId, revokedAt: null }, data: { revokedAt: now } });
  }
  async liveFamily(userId: string, familyId: string, now: Date) {
    return (await this.db.refreshToken.count({ where: { userId, familyId, revokedAt: null, expiresAt: { gt: now } } })) > 0;
  }
}

@Injectable()
export class PrismaAuthRepository extends AuthRepository {
  in(scope: object): AuthRecords { return new PrismaAuthRecords(scope as Prisma.TransactionClient); }
}
