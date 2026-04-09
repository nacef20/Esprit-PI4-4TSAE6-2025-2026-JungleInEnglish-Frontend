export type ComplaintAiAssistMode = 'GENERATE' | 'IMPROVE';

export interface ComplaintAiAssistRequest {
  title: string;
  description: string;
  mode: ComplaintAiAssistMode;
}

export interface ComplaintAiAssistResponse {
  suggestedTitle: string;
  suggestedDescription: string;
}
