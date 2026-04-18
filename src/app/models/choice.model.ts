import { Question } from './question.model';
import { Attempt } from './attempt.model';

export interface Choice {
  id?: number;
  content: string;
  isCorrect?: boolean;
  question?: Question;
  attempt?: Attempt;
}
