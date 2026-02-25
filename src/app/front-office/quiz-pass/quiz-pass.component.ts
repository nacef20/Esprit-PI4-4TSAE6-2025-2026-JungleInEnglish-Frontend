import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Quiz } from '../../models/quiz.model';
import { Question } from '../../models/question.model';
import { Choice } from '../../models/choice.model';
import { QuizService } from '../../services/quiz.service';
import { QuestionService } from '../../services/question.service';
import { ChoiceService } from '../../services/choice.service';
import { AttemptService } from '../../services/attempt.service';

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
    });

    this.questionService.getAll().subscribe(allQuestions => {
      this.questions = allQuestions.filter(q => q.quiz?.id === quizId);
      this.totalPoints = this.questions.reduce((sum, q) => sum + q.points, 0);

      this.choiceService.getAll().subscribe(allChoices => {
        this.questions.forEach(q => {
          const qChoices = allChoices.filter(c => c.question?.id === q.id);
          this.choicesByQuestion.set(q.id!, qChoices);
        });
      });
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
