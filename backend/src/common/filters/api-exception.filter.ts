import { randomUUID } from 'node:crypto';

import {
  Catch,
  HttpException,
  HttpStatus,
} from '@nestjs/common';
import type {
  ArgumentsHost,
  ConsoleLogger,
  ExceptionFilter,
} from '@nestjs/common';
import type { Response } from 'express';

import type { RequestWithId } from '../types/request-with-id.js';

interface ExceptionPayload {
  code?: string;
  details?: unknown;
  message?: string | string[];
}

@Catch()
export class ApiExceptionFilter implements ExceptionFilter {
  constructor(private readonly logger: ConsoleLogger) {}

  catch(exception: unknown, host: ArgumentsHost): void {
    const context = host.switchToHttp();
    const request = context.getRequest<RequestWithId>();
    const response = context.getResponse<Response>();
    const traceId = request.requestId ?? randomUUID();
    const status =
      exception instanceof HttpException
        ? exception.getStatus()
        : HttpStatus.INTERNAL_SERVER_ERROR;
    const payload = this.getPayload(exception);
    const code = payload.code ?? this.defaultCode(status);
    const details = Array.isArray(payload.message)
      ? { validationErrors: payload.message }
      : (payload.details ?? null);
    const message =
      status === HttpStatus.INTERNAL_SERVER_ERROR
        ? 'An unexpected error occurred.'
        : this.publicMessage(payload.message, status);

    this.logger.error({
      code,
      method: request.method,
      // Query strings may contain secrets even when an endpoint rejects them.
      path: request.path,
      status,
      traceId,
    });

    if (status === HttpStatus.TOO_MANY_REQUESTS && details && typeof details === 'object' && 'retryAfterSeconds' in details && typeof details.retryAfterSeconds === 'number') {
      response.setHeader('Retry-After', String(details.retryAfterSeconds));
    }
    response.status(status).json({
      error: {
        code,
        message,
        details,
        traceId,
      },
    });
  }

  private getPayload(exception: unknown): ExceptionPayload {
    if (!(exception instanceof HttpException)) {
      return {};
    }

    const response = exception.getResponse();
    return typeof response === 'string' ? { message: response } : response;
  }

  private publicMessage(
    message: string | string[] | undefined,
    status: number,
  ): string {
    if (typeof message === 'string') {
      return message;
    }
    if (Array.isArray(message)) {
      return 'Request validation failed.';
    }
    return HttpStatus[status] ?? 'Request failed.';
  }

  private defaultCode(status: number): string {
    if (status === HttpStatus.BAD_REQUEST) return 'VALIDATION_FAILED';
    if (status === HttpStatus.NOT_FOUND) return 'RESOURCE_NOT_FOUND';
    if (status === HttpStatus.FORBIDDEN) return 'FORBIDDEN_RESOURCE';
    if (status === HttpStatus.INTERNAL_SERVER_ERROR) return 'INTERNAL_SERVER_ERROR';
    return `HTTP_${status}`;
  }
}
