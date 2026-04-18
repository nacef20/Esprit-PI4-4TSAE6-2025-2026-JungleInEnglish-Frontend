export type ComplaintTone = 'POSITIVE' | 'NEUTRAL' | 'NEGATIVE';

export interface ComplaintSentimentRequest {
  complaintId?: number;
  title: string;
  description: string;
}

export interface ComplaintSentimentResponse {
  complaintId?: number;
  tone: ComplaintTone;
  urgent: boolean;
  reason: string;
}
