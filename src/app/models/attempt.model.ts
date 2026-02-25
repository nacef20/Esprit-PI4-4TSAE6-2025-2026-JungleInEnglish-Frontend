import { Quiz } from './quiz.model';

export interface Attempt {
  id?: number;
  score?: number;
  startedAt?: string;
  submittedAt?: string;
  quiz?: Quiz;
  studentId?: number;
}
