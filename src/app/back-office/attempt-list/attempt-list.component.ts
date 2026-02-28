import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Attempt } from '../../models/attempt.model';
import { AttemptService } from '../../services/attempt.service';
import { Quiz } from '../../models/quiz.model';
import { QuizService } from '../../services/quiz.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-attempt-list',
  templateUrl: './attempt-list.component.html',
  styleUrls: ['./attempt-list.component.scss']
})
export class AttemptListComponent implements OnInit, OnDestroy {

  attempts: Attempt[] = [];
  quizzes: Quiz[] = [];

  selectedQuizId: number | null = null;
  searchStudentName = '';
  searchQuizTitle = '';
  private searchSubject = new Subject<string>();
  private studentSearchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  // Pagination (server-side)
  currentPage = 1;
  pageSize = 5;
  totalElements = 0;
  totalPages = 0;

  // Stats
  totalAttempts = 0;
  avgScore = 0;
  passRate = 0;
  bestScore = 0;

  constructor(
    private attemptService: AttemptService,
    private quizService: QuizService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.quizService.getAll().subscribe(data => this.quizzes = data);
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(term => {
      this.searchQuizTitle = term;
      this.currentPage = 1;
      this.loadPage();
    });
    this.studentSearchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(term => {
      this.searchStudentName = term;
      this.currentPage = 1;
      this.loadPage();
    });
    this.loadPage();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearchQuizTitle(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchSubject.next(value);
  }

  onSearchStudentName(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.studentSearchSubject.next(value);
  }

  loadPage(): void {
    this.attemptService.getAllPaginated(
      this.currentPage - 1, this.pageSize, this.selectedQuizId, this.searchStudentName, this.searchQuizTitle
    ).subscribe(page => {
      this.attempts = page.content;
      this.totalElements = page.totalElements;
      this.totalPages = page.totalPages;
      this.computeStats();
    });
  }

  applyFilters(): void {
    this.currentPage = 1;
    this.loadPage();
  }

  computeStats(): void {
    const attempts = this.attempts;
    this.totalAttempts = this.totalElements;

    if (attempts.length === 0) {
      this.avgScore = 0;
      this.passRate = 0;
      this.bestScore = 0;
      return;
    }

    const percentages = attempts.map(a => this.getPercentage(a));
    this.avgScore = Math.round(percentages.reduce((s, p) => s + p, 0) / percentages.length);
    this.passRate = Math.round((percentages.filter(p => p >= 50).length / percentages.length) * 100);
    this.bestScore = Math.max(...percentages);
  }

  getPercentage(attempt: Attempt): number {
    if (!attempt.score && attempt.score !== 0) return 0;
    if (!attempt.totalPoints || attempt.totalPoints === 0) return 0;
    return Math.round((attempt.score / attempt.totalPoints) * 100);
  }

  deleteAttempt(id: number): void {
    if (confirm('Supprimer cette tentative ?')) {
      this.attemptService.delete(id).subscribe(() => this.loadPage());
    }
  }

  getScoreColor(attempt: Attempt): string {
    const pct = this.getPercentage(attempt);
    if (pct >= 80) return 'text-green-600';
    if (pct >= 50) return 'text-yellow-600';
    return 'text-red-600';
  }

  getScoreBadgeBg(attempt: Attempt): string {
    const pct = this.getPercentage(attempt);
    if (pct >= 80) return 'bg-green-50';
    if (pct >= 50) return 'bg-yellow-50';
    return 'bg-red-50';
  }

  getPassLabel(attempt: Attempt): string {
    return this.getPercentage(attempt) >= 50 ? 'Réussi' : 'Échoué';
  }

  getPassBadgeClasses(attempt: Attempt): string {
    return this.getPercentage(attempt) >= 50
      ? 'bg-green-100 text-green-700'
      : 'bg-red-100 text-red-700';
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  resetFilters(): void {
    this.selectedQuizId = null;
    this.searchStudentName = '';
    this.searchQuizTitle = '';
    this.applyFilters();
  }

  viewDetail(attemptId: number): void {
    this.router.navigate(['/back/attempts', attemptId]);
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
