export enum QuizStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  CLOSED = 'CLOSED'
}

export interface Quiz {
  id?: number;
  title: string;
  description: string;
  duration: number;
  status: QuizStatus;
  teacherId?: number;
  courseId?: number;
}
