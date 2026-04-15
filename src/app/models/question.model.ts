import { Quiz } from './quiz.model';

export interface Question {
  id?: number;
  content: string;
  points: number;
  quiz?: Quiz;
}
