import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Quiz } from '../../models/quiz.model';
import { Question } from '../../models/question.model';
import { Choice } from '../../models/choice.model';
import { QuizService } from '../../services/quiz.service';
import { QuestionService } from '../../services/question.service';
import { ChoiceService } from '../../services/choice.service';
import { AttemptService } from '../../services/attempt.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-quiz-pass',
  templateUrl: './quiz-pass.component.html',
  styleUrls: ['./quiz-pass.component.scss']
})
export class QuizPassComponent implements OnInit {

  quiz?: Quiz;
  questions: Question[] = [];
  choicesByQuestion: Map<number, Choice[]> = new Map();
  selectedChoices: Map<number, number> = new Map(); // questionId -> choiceId
  submitted = false;
  score = 0;
  totalPoints = 0;
  loading = true;
  blocked = false;
  blockReason = '';

  constructor(
    private quizService: QuizService,
    private questionService: QuestionService,
    private choiceService: ChoiceService,
    private attemptService: AttemptService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const quizId = +this.route.snapshot.params['id'];
    const studentId = 1; // placeholder — no auth

    this.quizService.getById(quizId).subscribe(quiz => {
      this.quiz = quiz;

      // Check if quiz is closed
      if (quiz.status === 'CLOSED') {
        this.blocked = true;
        this.blockReason = 'Ce quiz est ferm\u00e9. Vous ne pouvez plus le passer.';
        this.loading = false;
        return;
      }

      // Check max attempts
      if (quiz.maxAttempts && quiz.maxAttempts > 0) {
        this.attemptService.countByQuizAndStudent(quizId, studentId).subscribe(count => {
          if (count >= quiz.maxAttempts!) {
            this.blocked = true;
            this.blockReason = `Vous avez atteint le nombre maximum de tentatives (${quiz.maxAttempts}).`;
            this.loading = false;
          } else {
            this.loadQuestions(quizId);
          }
        });
      } else {
        this.loadQuestions(quizId);
      }
    });
  }

  private loadQuestions(quizId: number): void {
    this.questionService.getByQuizId(quizId).subscribe({
      next: (questions) => {
        this.questions = questions;
        this.totalPoints = this.questions.reduce((sum, q) => sum + q.points, 0);

        if (this.questions.length === 0) {
          this.loading = false;
          return;
        }

        // Load choices for each question using dedicated endpoints
        const choiceRequests = this.questions.map(q =>
          this.choiceService.getByQuestionId(q.id!)
        );

        forkJoin(choiceRequests).subscribe({
          next: (choicesArrays) => {
            choicesArrays.forEach((choices, index) => {
              this.choicesByQuestion.set(this.questions[index].id!, choices);
            });
            this.loading = false;
          },
          error: () => this.loading = false
        });
      },
      error: () => this.loading = false
    });
  }

  selectChoice(questionId: number, choiceId: number): void {
    this.selectedChoices.set(questionId, choiceId);
  }

  isSelected(questionId: number, choiceId: number): boolean {
    return this.selectedChoices.get(questionId) === choiceId;
  }

  submit(): void {
    if (this.submitted) return;

    // Calculate score
    this.score = 0;
    this.questions.forEach(q => {
      const selectedId = this.selectedChoices.get(q.id!);
      if (selectedId) {
        const choices = this.choicesByQuestion.get(q.id!) || [];
        const selected = choices.find(c => c.id === selectedId);
        if (selected?.isCorrect) {
          this.score += q.points;
        }
      }
    });

    // Save attempt
    const attempt = {
      score: this.score,
      totalPoints: this.totalPoints,
      startedAt: new Date().toISOString(),
      submittedAt: new Date().toISOString(),
      quiz: { id: this.quiz?.id } as Quiz,
      studentId: 1 // placeholder — no auth
    };

    this.attemptService.create(attempt).subscribe();
    this.submitted = true;
  }

  goBack(): void {
    this.router.navigate(['/front/quizzes']);
  }

  isCorrectChoice(questionId: number, choiceId: number): boolean {
    const choices = this.choicesByQuestion.get(questionId) || [];
    return choices.find(c => c.id === choiceId)?.isCorrect === true;
  }
}
