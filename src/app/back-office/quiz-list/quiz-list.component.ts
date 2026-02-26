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

  // Pagination (server-side)
  currentPage = 1;
  pageSize = 6;
  totalElements = 0;
  totalPages = 0;

  constructor(private quizService: QuizService, private router: Router) {}

  ngOnInit(): void {
    this.loadPage();
  }

  loadPage(): void {
    this.quizService.getAllPaginated(this.currentPage - 1, this.pageSize).subscribe(page => {
      this.quizzes = page.content;
      this.totalElements = page.totalElements;
      this.totalPages = page.totalPages;
    });
  }

  addQuiz(): void {
    this.router.navigate(['/back/quizzes/new']);
  }

  editQuiz(id: number): void {
    this.router.navigate(['/back/quizzes/edit', id]);
  }

  deleteQuiz(id: number): void {
    if (confirm('Supprimer ce quiz ?')) {
      this.quizService.delete(id).subscribe(() => this.loadPage());
    }
  }

  manageQuestions(quizId: number): void {
    this.router.navigate(['/back/quizzes', quizId, 'questions']);
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    for (let i = 1; i <= this.totalPages; i++) {
      pages.push(i);
    }
    return pages;
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadPage();
    }
  }
}
