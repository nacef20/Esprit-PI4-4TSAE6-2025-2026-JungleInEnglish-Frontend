import type { Evenement } from './evenement.model';

export interface Participation {
  participantId?: number;
  userId: number;
  event?: Evenement;
  eventId?: number;
  registrationDate?: string;
  checkInTime?: string;
  createdAt?: string;
  updatedAt?: string;
}
