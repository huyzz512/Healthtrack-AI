import { Inject, Injectable, ServiceUnavailableException } from '@nestjs/common';

import { FOUNDATION_VERSION } from '../../common/constants/api.constants.js';
import { DatabaseHealthService } from '../../infrastructure/database/database-health.service.js';

export interface HealthStatus {
  status: 'ok';
  database: 'connected';
  version: string;
}

@Injectable()
export class HealthService {
  constructor(
    @Inject(DatabaseHealthService)
    private readonly databaseHealth: DatabaseHealthService,
  ) {}

  async getStatus(): Promise<HealthStatus> {
    if (!(await this.databaseHealth.isConnected())) {
      throw new ServiceUnavailableException({
        code: 'DATABASE_UNAVAILABLE',
        message: 'Database connectivity check failed.',
      });
    }

    return {
      status: 'ok',
      database: 'connected',
      version: FOUNDATION_VERSION,
    };
  }
}
