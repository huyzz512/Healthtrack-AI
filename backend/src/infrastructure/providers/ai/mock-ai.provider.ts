import { Injectable } from '@nestjs/common';

import type {
  AICompletionRequest,
  AICompletionResponse,
  AIProvider,
} from './ai-provider.js';

@Injectable()
export class MockAIProvider implements AIProvider {
  complete(request: AICompletionRequest): Promise<AICompletionResponse> {
    void request;

    return Promise.resolve({
      content:
        'Mock AI response: no external model was called and no health data left the local application.',
      model: 'foundation-mock',
      provider: 'mock',
    });
  }
}
