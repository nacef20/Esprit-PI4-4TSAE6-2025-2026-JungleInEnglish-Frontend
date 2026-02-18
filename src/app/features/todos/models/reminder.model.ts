export interface Reminder {
  id?: number;
  reminderDateTime: string;
  message?: string;
  isTriggered?: boolean;
}

export interface CreateReminderRequest {
  reminderDateTime: string;
  message?: string;
  isTriggered?: boolean;
}
