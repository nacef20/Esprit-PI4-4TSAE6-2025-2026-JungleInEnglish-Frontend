import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Question } from '../../models/question.model';
import { QuestionService } from '../../services/question.service';

@Component({
  selector: 'app-question-list',
  templateUrl: './question-list.component.html',
  styleUrls: ['./question-list.component.scss']
})
export class QuestionListComponent implements OnInit {

  questions: Question[] = [];
  quizId!: number;

  constructor(
    private questionService: QuestionService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.quizId = +this.route.snapshot.params['quizId'];
    this.loadQuestions();
  }

  loadQuestions(): void {
    this.questionService.getByQuizId(this.quizId).subscribe(data => {
      this.questions = data;
    });
  }

  addQuestion(): void {
    this.router.navigate(['/back/quizzes', this.quizId, 'questions', 'new']);
  }

  editQuestion(questionId: number): void {
    this.router.navigate(['/back/quizzes', this.quizId, 'questions', 'edit', questionId]);
  }

  deleteQuestion(id: number): void {
    if (confirm('Delete this question?')) {
      this.questionService.delete(id).subscribe(() => this.loadQuestions());
    }
  }

  manageChoices(questionId: number): void {
    this.router.navigate(['/back/questions', questionId, 'choices']);
  }

  goBack(): void {
    this.router.navigate(['/back/quizzes']);
  }
}
