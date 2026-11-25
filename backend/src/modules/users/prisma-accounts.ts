import { Inject, Injectable } from '@nestjs/common';
import type { Prisma } from '../../generated/prisma/client.js';
import { PrismaService } from '../../infrastructure/database/prisma.service.js';
import { AccountUnitOfWork } from './account.port.js';
import type { Accounts, Identity } from './account.port.js';

class PrismaAccounts implements Accounts {
  constructor(private readonly db: Prisma.TransactionClient) {}
  find(identity: Identity) {
    return this.db.user.findFirst({ where: identity, include: { credential: true } });
  }
  get(id: string) {
    return this.db.user.findUnique({ where: { id }, include: { credential: true } });
  }
  create(input: Identity & { fullName: string; passwordHash: string }) {
    const { passwordHash, ...identity } = input;
    return this.db.user.create({ data: { ...identity, credential: { create: { passwordHash } } }, include: { credential: true } });
  }
  async activate(id: string, channel: 'EMAIL' | 'SMS', now: Date) {
    await this.db.user.update({ where: { id }, data: { status: 'ACTIVE', ...(channel === 'EMAIL' ? { emailVerifiedAt: now } : { phoneVerifiedAt: now }) } });
  }
  async failures(userId: string, failedLoginCount: number, failedLoginWindowAt: Date | null, lockedUntil: Date | null) {
    await this.db.userCredential.update({ where: { userId }, data: { failedLoginCount, failedLoginWindowAt, lockedUntil } });
  }
  async password(userId: string, passwordHash: string, passwordChangedAt: Date) {
    await this.db.userCredential.update({ where: { userId }, data: { passwordHash, passwordChangedAt, failedLoginCount: 0, failedLoginWindowAt: null, lockedUntil: null } });
  }
  rename(id: string, fullName: string) {
    return this.db.user.update({ where: { id }, data: { fullName }, include: { credential: true } });
  }
}

@Injectable()
export class PrismaAccountUnitOfWork extends AccountUnitOfWork {
  constructor(@Inject(PrismaService) private readonly prisma: PrismaService) { super(); }
  read<T>(work: (accounts: Accounts, scope: object) => Promise<T>): Promise<T> {
    return work(new PrismaAccounts(this.prisma), this.prisma);
  }
  run<T>(key: string, work: (accounts: Accounts, scope: object) => Promise<T>): Promise<T> {
    return this.prisma.$transaction(async (tx) => {
      // READ COMMITTED queries after this lock see the latest committed state.
      // Every auth mutation for one account uses the same server-derived key.
      await tx.$executeRaw`SELECT pg_advisory_xact_lock(hashtextextended(${key}, 0))`;
      return work(new PrismaAccounts(tx), tx);
    }, { isolationLevel: 'ReadCommitted', maxWait: 5_000, timeout: 15_000 });
  }
}
