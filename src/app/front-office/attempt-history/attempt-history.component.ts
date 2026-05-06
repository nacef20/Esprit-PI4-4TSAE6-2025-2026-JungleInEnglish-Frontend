import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Attempt } from '../../models/attempt.model';
import { AttemptService } from '../../services/attempt.service';

@Component({
  selector: 'app-attempt-history',
  templateUrl: './attempt-history.component.html',
  styleUrls: ['./attempt-history.component.scss']
})
export class AttemptHistoryComponent implements OnInit {

  attempts: Attempt[] = [];
  studentId = 1; // placeholder — no auth

  // Pagination (server-side)
  currentPage = 1;
  pageSize = 3;
  totalElements = 0;
  totalPages = 0;

  // Stats
  totalAttempts = 0;
  avgScore = 0;
  bestScore = 0;
  passRate = 0;

  constructor(
    private attemptService: AttemptService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPage();
    this.loadStats();
  }

  loadPage(): void {
    this.attemptService.getByStudentPaginated(this.studentId, this.currentPage - 1, this.pageSize)
      .subscribe(page => {
        this.attempts = page.content;
        this.totalElements = page.totalElements;
        this.totalPages = page.totalPages;
      });
  }

  loadStats(): void {
    this.attemptService.getStudentStats(this.studentId).subscribe(stats => {
      this.totalAttempts = stats.totalAttempts;
      this.avgScore = stats.avgScore;
      this.bestScore = stats.bestScore;
      this.passRate = stats.passRate;
    });
  }

  getPercentage(attempt: Attempt): number {
    if (!attempt.score && attempt.score !== 0) return 0;
    if (!attempt.totalPoints || attempt.totalPoints === 0) return 0;
    return Math.round((attempt.score / attempt.totalPoints) * 100);
  }

  getScoreColor(attempt: Attempt): string {
    const pct = this.getPercentage(attempt);
    if (pct >= 80) return 'text-green-600';
    if (pct >= 50) return 'text-yellow-600';
    return 'text-red-600';
  }

  getScoreBg(attempt: Attempt): string {
    const pct = this.getPercentage(attempt);
    if (pct >= 80) return 'bg-green-50 border-green-200';
    if (pct >= 50) return 'bg-yellow-50 border-yellow-200';
    return 'bg-red-50 border-red-200';
  }

  getPassLabel(attempt: Attempt): string {
    return this.getPercentage(attempt) >= 50 ? 'Passed' : 'Failed';
  }

  getPassBadge(attempt: Attempt): string {
    return this.getPercentage(attempt) >= 50
      ? 'bg-green-100 text-green-700'
      : 'bg-red-100 text-red-700';
  }

  getProgressBarColor(attempt: Attempt): string {
    const pct = this.getPercentage(attempt);
    if (pct >= 80) return 'bg-green-500';
    if (pct >= 50) return 'bg-yellow-500';
    return 'bg-red-500';
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  viewDetail(attemptId: number): void {
    this.router.navigate(['/back/student-attempts', attemptId]);
  }

  goToQuizzes(): void {
    this.router.navigate(['/back/student-quizzes']);
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
