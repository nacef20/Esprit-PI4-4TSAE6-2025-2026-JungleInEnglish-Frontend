export enum TodoStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED'
}

export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH'
}

export enum Level {
  A1 = 'A1',
  A2 = 'A2',
  B1 = 'B1',
  B2 = 'B2',
  C1 = 'C1',
  C2 = 'C2'
}

export interface Todo {
  id?: number;
  title: string;
  description?: string;
  status: TodoStatus;
  priority: Priority;
  dueDate?: string; // LocalDateTime ISO
  createdAt?: string;
  updatedAt?: string;
  checked: boolean;
  notificationEmail?: string;
  level?: Level;
  userId?: string;
}

export interface Reminder {
  id?: number;
  todoId: number;
  reminderDateTime: string;
  message: string;
  isTriggered: boolean;
}

export interface TodoStats {
  total: number;
  completedPercentage: number;
  overduePercentage: number;
  highPriorityCount: number;
  pendingCount: number;
  statusBreakdown: { [key: string]: number };
  priorityDistribution: { [key: string]: number };
}

export interface AnalyticsStudent {
  totalTodos: number;
  completedTodos: number;
  pendingTodos: number;
  completionRate: number;
}

export interface AnalyticsTeacher {
  totalStudents: number;
  averageCompletionRate: number;
  totalCompletedTodos: number;
  totalPendingTodos: number;
}
