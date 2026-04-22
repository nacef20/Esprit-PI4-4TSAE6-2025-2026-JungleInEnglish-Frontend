import { ComplaintResponse } from './response.model';

export enum ComplaintStatus {
  EN_ATTENTE = 'EN_ATTENTE',
  TRAITEE = 'TRAITEE',
  REJETEE = 'REJETEE'
}

export interface Complaint {
  id?: number;
  title: string;
  description: string;
  status: ComplaintStatus;
  createdAt?: string;
  updatedAt?: string;
  attachmentFileName?: string;
  attachmentContentType?: string;
  attachmentSize?: number;
  responses?: ComplaintResponse[];
}
