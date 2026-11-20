import { ApiProperty } from '@nestjs/swagger';

export class HealthStatusDto {
  @ApiProperty({ enum: ['ok'], example: 'ok' })
  status!: 'ok';

  @ApiProperty({ enum: ['connected'], example: 'connected' })
  database!: 'connected';

  @ApiProperty({ example: 'foundation-v0.1' })
  version!: string;
}

export class HealthSuccessEnvelopeDto {
  @ApiProperty({ type: HealthStatusDto })
  data!: HealthStatusDto;
}

export class ApiErrorDto {
  @ApiProperty({ example: 'DATABASE_UNAVAILABLE' })
  code!: string;

  @ApiProperty({ example: 'Database connectivity check failed.' })
  message!: string;

  @ApiProperty({ nullable: true, type: Object })
  details!: Record<string, unknown> | null;

  @ApiProperty({ example: 'request-correlation-id' })
  traceId!: string;
}

export class ApiErrorEnvelopeDto {
  @ApiProperty({ type: ApiErrorDto })
  error!: ApiErrorDto;
}
