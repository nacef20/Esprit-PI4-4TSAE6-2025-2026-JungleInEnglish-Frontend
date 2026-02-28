import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Attempt } from '../../models/attempt.model';
import { AttemptAnswer } from '../../models/attempt-answer.model';
import { Question } from '../../models/question.model';
import { Choice } from '../../models/choice.model';
import { AttemptService } from '../../services/attempt.service';
import { AttemptAnswerService } from '../../services/attempt-answer.service';
import { QuestionService } from '../../services/question.service';
import { ChoiceService } from '../../services/choice.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-attempt-detail',
  templateUrl: './attempt-detail.component.html',
  styleUrls: ['./attempt-detail.component.scss']
})
export class AttemptDetailComponent implements OnInit {

  attempt?: Attempt;
  answers: AttemptAnswer[] = [];
  questions: Question[] = [];
  choicesByQuestion: Map<number, Choice[]> = new Map();
  answerByQuestion: Map<number, AttemptAnswer> = new Map();
  loading = true;
  downloading = false;

  constructor(
    private attemptService: AttemptService,
    private attemptAnswerService: AttemptAnswerService,
    private questionService: QuestionService,
    private choiceService: ChoiceService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const attemptId = +this.route.snapshot.params['attemptId'];

    this.attemptService.getById(attemptId).subscribe(attempt => {
      this.attempt = attempt;

      if (!attempt.quiz?.id) {
        this.loading = false;
        return;
      }

      // Load answers and questions in parallel
      forkJoin({
        answers: this.attemptAnswerService.getByAttemptId(attemptId),
        questions: this.questionService.getByQuizId(attempt.quiz.id)
      }).subscribe(({ answers, questions }) => {
        this.answers = answers;
        this.questions = questions;

        // Index answers by question ID
        answers.forEach(a => {
          if (a.question?.id) {
            this.answerByQuestion.set(a.question.id, a);
          }
        });

        // Load choices for each question
        if (questions.length === 0) {
          this.loading = false;
          return;
        }

        const choiceRequests = questions.map(q =>
          this.choiceService.getByQuestionId(q.id!)
        );

        forkJoin(choiceRequests).subscribe(choicesArrays => {
          choicesArrays.forEach((choices, index) => {
            this.choicesByQuestion.set(questions[index].id!, choices);
          });
          this.loading = false;
        });
      });
    });
  }

  getPercentage(): number {
    if (!this.attempt?.score && this.attempt?.score !== 0) return 0;
    if (!this.attempt?.totalPoints || this.attempt.totalPoints === 0) return 0;
    return Math.round((this.attempt.score / this.attempt.totalPoints) * 100);
  }

  isPassed(): boolean {
    return this.getPercentage() >= 50;
  }

  isSelectedChoice(questionId: number, choiceId: number): boolean {
    const answer = this.answerByQuestion.get(questionId);
    return answer?.selectedChoice?.id === choiceId;
  }

  isCorrectChoice(choiceId: number, questionId: number): boolean {
    const choices = this.choicesByQuestion.get(questionId) || [];
    return choices.find(c => c.id === choiceId)?.isCorrect === true;
  }

  isQuestionCorrect(questionId: number): boolean {
    const answer = this.answerByQuestion.get(questionId);
    if (!answer?.selectedChoice?.id) return false;
    return this.isCorrectChoice(answer.selectedChoice.id, questionId);
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  goBack(): void {
    this.router.navigate(['/back/attempts']);
  }

  downloadPdf(): void {
    if (!this.attempt?.id || this.downloading) return;
    this.downloading = true;
    this.attemptService.downloadPdf(this.attempt.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `attempt-${this.attempt!.id}-report.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.downloading = false;
      },
      error: () => {
        this.downloading = false;
      }
    });
  }
}
