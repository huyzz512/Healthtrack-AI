export interface AICompletionRequest {
  readonly prompt: string;
  readonly minimizedContext?: Readonly<Record<string, unknown>>;
  readonly consentReference: string;
}

export interface AICompletionResponse {
  readonly content: string;
  readonly model: string;
  readonly provider: string;
}

export interface AIProvider {
  complete(request: AICompletionRequest): Promise<AICompletionResponse>;
}
