import { Attempt } from './attempt.model';
import { Question } from './question.model';
import { Choice } from './choice.model';

export interface AttemptAnswer {
  id?: number;
  attempt?: Attempt;
  question?: Question;
  selectedChoice?: Choice;
}
