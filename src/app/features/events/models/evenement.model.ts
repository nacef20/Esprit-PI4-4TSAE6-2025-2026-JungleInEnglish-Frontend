export interface Evenement {
  eventId?: number;
  title: string;
  description?: string;
  startTime: string;
  endTime: string;
  location?: string;
  capacity: number;
  organizerId: number;
  reminderSent?: boolean;
}
