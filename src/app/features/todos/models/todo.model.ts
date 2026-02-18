export type TodoStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Todo {
  id?: number;
  title: string;
  description?: string;
  status: TodoStatus;
  priority: Priority;
  dueDate?: string;
  createdAt?: string;
  updatedAt?: string;
  ischecked: boolean;
}
