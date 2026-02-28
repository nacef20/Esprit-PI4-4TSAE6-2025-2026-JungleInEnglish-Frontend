import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Quiz } from '../../models/quiz.model';
import { QuizService } from '../../services/quiz.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-quiz-list',
  templateUrl: './quiz-list.component.html',
  styleUrls: ['./quiz-list.component.scss']
})
export class QuizListComponent implements OnInit, OnDestroy {

  quizzes: Quiz[] = [];
  searchTerm = '';
  statusFilter = '';
  statusOptions = ['', 'DRAFT', 'PUBLISHED', 'CLOSED'];
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  // Pagination (server-side)
  currentPage = 1;
  pageSize = 6;
  totalElements = 0;
  totalPages = 0;

  constructor(private quizService: QuizService, private router: Router) {}

  ngOnInit(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(term => {
      this.searchTerm = term;
      this.currentPage = 1;
      this.loadPage();
    });
    this.loadPage();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearch(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchSubject.next(value);
  }

  onFilterStatus(status: string): void {
    this.statusFilter = status;
    this.currentPage = 1;
    this.loadPage();
  }

  loadPage(): void {
    const title = this.searchTerm.trim() || undefined;
    const status = this.statusFilter || undefined;
    this.quizService.getAllPaginated(this.currentPage - 1, this.pageSize, status, title).subscribe(page => {
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
    if (confirm('Delete this quiz?')) {
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
