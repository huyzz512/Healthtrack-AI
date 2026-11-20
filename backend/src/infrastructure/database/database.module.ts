import { Global, Module } from '@nestjs/common';

import { DatabaseHealthService } from './database-health.service.js';
import { PrismaService } from './prisma.service.js';

@Global()
@Module({
  exports: [DatabaseHealthService, PrismaService],
  providers: [DatabaseHealthService, PrismaService],
})
export class DatabaseModule {}
