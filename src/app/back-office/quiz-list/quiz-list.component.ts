import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Quiz } from '../../models/quiz.model';
import { QuizService } from '../../services/quiz.service';

@Component({
  selector: 'app-quiz-list',
  templateUrl: './quiz-list.component.html',
  styleUrls: ['./quiz-list.component.scss']
})
export class QuizListComponent implements OnInit {

  quizzes: Quiz[] = [];

  constructor(private quizService: QuizService, private router: Router) {}

  ngOnInit(): void {
    this.loadQuizzes();
  }

  loadQuizzes(): void {
    this.quizService.getAll().subscribe(data => this.quizzes = data);
  }

  addQuiz(): void {
    this.router.navigate(['/back/quizzes/new']);
  }

  editQuiz(id: number): void {
    this.router.navigate(['/back/quizzes/edit', id]);
  }

  deleteQuiz(id: number): void {
    if (confirm('Supprimer ce quiz ?')) {
      this.quizService.delete(id).subscribe(() => this.loadQuizzes());
    }
  }

  manageQuestions(quizId: number): void {
    this.router.navigate(['/back/quizzes', quizId, 'questions']);
  }
}
