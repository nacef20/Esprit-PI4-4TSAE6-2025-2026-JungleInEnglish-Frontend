import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Quiz, QuizStatus } from '../../models/quiz.model';
import { QuizService } from '../../services/quiz.service';
import { AttemptService } from '../../services/attempt.service';

@Component({
  selector: 'app-quiz-student-list',
  templateUrl: './quiz-student-list.component.html',
  styleUrls: ['./quiz-student-list.component.scss']
})
export class QuizStudentListComponent implements OnInit {

  quizzes: Quiz[] = [];
  attemptCounts: Map<number, number> = new Map();
  studentId = 1; // placeholder — no auth

  // Pagination (server-side)
  currentPage = 1;
  pageSize = 6;
  totalElements = 0;
  totalPages = 0;

  constructor(
    private quizService: QuizService,
    private attemptService: AttemptService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPage();
  }

  loadPage(): void {
    this.quizService.getAvailablePaginated(this.currentPage - 1, this.pageSize).subscribe(page => {
      this.quizzes = page.content;
      this.totalElements = page.totalElements;
      this.totalPages = page.totalPages;
      // Load attempt counts for each quiz on this page
      this.quizzes.forEach(quiz => {
        if (quiz.id) {
          this.attemptService.countByQuizAndStudent(quiz.id, this.studentId).subscribe(count => {
            this.attemptCounts.set(quiz.id!, count);
          });
        }
      });
    });
  }

  canTakeQuiz(quiz: Quiz): boolean {
    if (quiz.status === 'CLOSED') return false;
    if (quiz.maxAttempts && quiz.maxAttempts > 0) {
      const count = this.attemptCounts.get(quiz.id!) || 0;
      return count < quiz.maxAttempts;
    }
    return true;
  }

  getQuizBlockReason(quiz: Quiz): string {
    if (quiz.status === 'CLOSED') return 'Quiz fermé';
    if (quiz.maxAttempts && quiz.maxAttempts > 0) {
      const count = this.attemptCounts.get(quiz.id!) || 0;
      if (count >= quiz.maxAttempts) return 'Nombre max de tentatives atteint';
    }
    return '';
  }

  startQuiz(quiz: Quiz): void {
    if (!this.canTakeQuiz(quiz)) return;
    this.router.navigate(['/front/quiz', quiz.id]);
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
