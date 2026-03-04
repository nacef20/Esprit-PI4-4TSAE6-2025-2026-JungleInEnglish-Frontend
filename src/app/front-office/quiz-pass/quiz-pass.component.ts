import { Component, OnInit, OnDestroy } from '@angular/core';
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
export class QuizPassComponent implements OnInit, OnDestroy {

  quiz?: Quiz;
  questions: Question[] = [];
  choicesByQuestion: Map<number, Choice[]> = new Map();
  selectedChoices: Map<number, number> = new Map(); // questionId -> choiceId
  submitted = false;
  submitting = false;
  score = 0;
  totalPoints = 0;
  loading = true;
  blocked = false;
  blockReason = '';
  timeExpiredOnSubmit = false;

  // Timer state
  attemptId: number | null = null;
  remainingSeconds = 0;
  timerInterval: any = null;
  timerWarning = false; // true when < 60s

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

    this.quizService.getById(quizId).subscribe(quiz => {
      this.quiz = quiz;

      // Check if quiz is closed
      if (quiz.status === 'CLOSED') {
        this.blocked = true;
        this.blockReason = 'This quiz is closed. You can no longer take it.';
        this.loading = false;
        return;
      }

      // Start attempt on server (handles already-completed / resume logic)
      this.attemptService.startAttempt(quizId, 1, 'Student 1').subscribe({
        next: (res) => {
          this.attemptId = res.attemptId;
          this.remainingSeconds = res.remainingSeconds;
          this.startTimer();
          this.loadQuestions(quizId);
        },
        error: (err) => {
          const errorMsg = err.error?.error || '';
          if (errorMsg === 'ALREADY_COMPLETED') {
            this.blocked = true;
            this.blockReason = 'You have already taken this quiz.';
          } else if (errorMsg === 'QUIZ_CLOSED') {
            this.blocked = true;
            this.blockReason = 'This quiz is closed. You can no longer take it.';
          } else {
            this.blocked = true;
            this.blockReason = 'Unable to start quiz. Please try again later.';
          }
          this.loading = false;
        }
      });
    });
  }

  ngOnDestroy(): void {
    this.clearTimer();
  }

  private startTimer(): void {
    this.timerWarning = this.remainingSeconds <= 60;
    this.timerInterval = setInterval(() => {
      this.remainingSeconds--;
      this.timerWarning = this.remainingSeconds <= 60;

      if (this.remainingSeconds <= 0) {
        this.remainingSeconds = 0;
        this.clearTimer();
        // Auto-submit when time runs out
        if (!this.submitted && !this.submitting) {
          this.submit(true);
        }
      }
    }, 1000);
  }

  private clearTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  get timerDisplay(): string {
    const m = Math.floor(this.remainingSeconds / 60);
    const s = this.remainingSeconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
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

  submit(autoSubmitted = false): void {
    if (this.submitted || this.submitting || !this.attemptId) return;
    this.submitting = true;
    this.clearTimer();

    // Build answers array from selected choices
    const answers: { questionId: number; choiceId: number }[] = [];
    this.selectedChoices.forEach((choiceId, questionId) => {
      answers.push({ questionId, choiceId });
    });

    this.attemptService.submitAttempt(this.attemptId, answers).subscribe({
      next: (res) => {
        this.score = res.score;
        this.totalPoints = res.totalPoints;
        this.timeExpiredOnSubmit = res.timeExpired;
        this.submitted = true;
        this.submitting = false;
      },
      error: () => {
        // If submit fails (already submitted), show as completed
        this.submitted = true;
        this.submitting = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/back/student-quizzes']);
  }

  isCorrectChoice(questionId: number, choiceId: number): boolean {
    const choices = this.choicesByQuestion.get(questionId) || [];
    return choices.find(c => c.id === choiceId)?.isCorrect === true;
  }
}
