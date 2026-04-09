export type ResponseAiAssistMode = 'GENERATE' | 'IMPROVE';

export interface ResponseAiAssistRequest {
  complaintTitle: string;
  complaintDescription: string;
  draftResponse: string;
  mode: ResponseAiAssistMode;
}

export interface ResponseAiAssistResponse {
  suggestedMessage: string;
}
