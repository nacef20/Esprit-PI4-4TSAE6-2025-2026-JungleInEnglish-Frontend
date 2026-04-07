import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Quiz, QuizStatus } from '../../models/quiz.model';
import { QuizService } from '../../services/quiz.service';
import { AttemptService } from '../../services/attempt.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-quiz-student-list',
  templateUrl: './quiz-student-list.component.html',
  styleUrls: ['./quiz-student-list.component.scss']
})
export class QuizStudentListComponent implements OnInit, OnDestroy {

  quizzes: Quiz[] = [];
  attemptCounts: Map<number, number> = new Map();
  studentId = 1; // placeholder — no auth
  searchTerm = '';
  activeFilter = 'all';
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

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

  onFilter(filter: string): void {
    this.activeFilter = filter;
    this.currentPage = 1;
    this.loadPage();
  }

  loadPage(): void {
    const title = this.searchTerm.trim() || undefined;
    const filter = this.activeFilter !== 'all' ? this.activeFilter : undefined;
    this.quizService.getAvailablePaginated(this.currentPage - 1, this.pageSize, title, filter, this.studentId).subscribe(page => {
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
    const count = this.attemptCounts.get(quiz.id!) || 0;
    return count < 1;
  }

  getQuizBlockReason(quiz: Quiz): string {
    if (quiz.status === 'CLOSED') return 'Quiz closed';
    const count = this.attemptCounts.get(quiz.id!) || 0;
    if (count >= 1) return 'Already taken';
    return '';
  }

  startQuiz(quiz: Quiz): void {
    if (!this.canTakeQuiz(quiz)) return;
    this.router.navigate(['/back/student-quizzes/pass', quiz.id]);
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
