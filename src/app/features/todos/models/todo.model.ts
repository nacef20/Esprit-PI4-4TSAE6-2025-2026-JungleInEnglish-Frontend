export type TodoStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Reminder {
  id?: number;
  reminderDateTime: string;  // LocalDateTime serialized as ISO string
  message?: string;
  isTriggered?: boolean;
}

export interface Todo {
  id?: number;
  title: string;
  description?: string;
  status: TodoStatus;
  priority: Priority;
  checked: boolean;
  dueDate?: string;     // add this
  createdAt?: string;
  reminders?: Reminder[];
  notificationEmail?: string;
  level?: string;
}

export interface TodoStats {
  totalTodos: number;
  completedTodos: number;
  pendingTodos: number;
  highPriorityTodos: number;
  statusBreakdown: Record<string, number>;
  priorityBreakdown: Record<string, number>;
}
