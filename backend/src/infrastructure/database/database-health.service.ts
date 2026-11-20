import { Inject, Injectable } from '@nestjs/common';

import { PrismaService } from './prisma.service.js';

@Injectable()
export class DatabaseHealthService {
  constructor(@Inject(PrismaService) private readonly prisma: PrismaService) {}

  async isConnected(): Promise<boolean> {
    try {
      const result = await this.prisma.$queryRaw<Array<{ connected: number }>>`
        SELECT 1 AS connected
      `;
      return result[0]?.connected === 1;
    } catch {
      return false;
    }
  }
}
