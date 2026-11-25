import { Module } from '@nestjs/common';
import { AccountUnitOfWork } from './account.port.js';
import { PrismaAccountUnitOfWork } from './prisma-accounts.js';

@Module({
  providers: [{ provide: AccountUnitOfWork, useClass: PrismaAccountUnitOfWork }],
  exports: [AccountUnitOfWork],
})
export class UsersModule {}
