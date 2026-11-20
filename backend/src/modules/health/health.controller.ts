import { Controller, Get, Inject } from '@nestjs/common';
import { ApiOkResponse, ApiOperation, ApiServiceUnavailableResponse, ApiTags } from '@nestjs/swagger';

import { HealthService } from './health.service.js';
import type { HealthStatus } from './health.service.js';
import { ApiErrorEnvelopeDto, HealthSuccessEnvelopeDto } from './health.dto.js';

@ApiTags('health')
@Controller('health')
export class HealthController {
  constructor(@Inject(HealthService) private readonly healthService: HealthService) {}

  @Get()
  @ApiOperation({ summary: 'Check application and database health' })
  @ApiOkResponse({
    description: 'Application and database are available.',
    type: HealthSuccessEnvelopeDto,
  })
  @ApiServiceUnavailableResponse({
    description: 'Database is unavailable.',
    type: ApiErrorEnvelopeDto,
  })
  getHealth(): Promise<HealthStatus> {
    return this.healthService.getStatus();
  }
}
