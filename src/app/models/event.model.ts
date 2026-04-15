export interface Evenement {
  eventId?: number;
  title: string;
  description: string;
  startTime: string; // LocalDateTime string iso
  endTime: string;   // LocalDateTime string iso
  location: string;
  capacity: number;
  availableSeats?: number;
  organizerId: number | string;
  imageUrl?: string;
  level: string;
  reminderSent?: boolean;
}

export interface Participation {
  participantId?: number;
  userId: string;
  event?: Evenement;
  registrationDate?: string;
  present?: boolean;
  qrCodeUrl?: string;
  publicToken?: string;
}

export interface Feedback {
  id?: number;
  evenementId: number;
  userId: number;
  note: number;
  commentaire: string;
  dateFeedback: string;
}
