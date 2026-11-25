import { Module } from '@nestjs/common';
import { UsersModule } from '../users/users.module.js';
import { AuthClock, AuthRepository } from './auth.port.js';
import { AuthCrypto } from './auth.crypto.js';
import { AuthController, UsersController } from './auth.controller.js';
import { AccessGuard, AuthThrottleGuard } from './auth.guard.js';
import { AuthApplicationService, SystemAuthClock } from './auth.service.js';
import { PrismaAuthRepository } from './prisma-auth.repository.js';

@Module({
  imports: [UsersModule],
  controllers: [AuthController, UsersController],
  providers: [AuthApplicationService, AuthCrypto, AccessGuard, AuthThrottleGuard,
    { provide: AuthClock, useClass: SystemAuthClock },
    { provide: AuthRepository, useClass: PrismaAuthRepository },
  ],
  exports: [AccessGuard, AuthApplicationService],
})
export class AuthModule {}
