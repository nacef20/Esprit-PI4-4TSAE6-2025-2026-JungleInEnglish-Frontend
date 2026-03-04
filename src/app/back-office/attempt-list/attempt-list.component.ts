import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Attempt } from '../../models/attempt.model';
import { AttemptService } from '../../services/attempt.service';
import { Quiz } from '../../models/quiz.model';
import { QuizService } from '../../services/quiz.service';
import { StatsService } from '../../services/stats.service';
import { DashboardStats, QuizStats, FailedQuestion } from '../../models/dashboard-stats.model';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { ChartConfiguration, ChartData, Chart, registerables } from 'chart.js';

Chart.register(...registerables);

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

  // Pagination
  currentPage = 1;
  pageSize = 5;
  totalElements = 0;
  totalPages = 0;

  // Dashboard stats (server-side)
  stats: DashboardStats | null = null;
  quizStatsList: QuizStats[] = [];
  mostFailedQuestions: FailedQuestion[] = [];
  exporting = false;
  exportingExcel = false;

  // Certificate status per attempt
  certificateMap: Map<number, { exists: boolean; grade?: string; certificateId?: number }> = new Map();

  // Chart data
  avgScoreChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  avgScoreChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx) => `${ctx.parsed.y}%`
        }
      }
    },
    scales: {
      y: { beginAtZero: true, max: 100, ticks: { callback: (v) => v + '%' } }
    }
  };

  passRateChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  passRateChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'y',
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx) => `${ctx.parsed.x}%`
        }
      }
    },
    scales: {
      x: { beginAtZero: true, max: 100, ticks: { callback: (v) => v + '%' } }
    }
  };

  gradeChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  gradeChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { padding: 16, usePointStyle: true } }
    }
  };

  constructor(
    private attemptService: AttemptService,
    private quizService: QuizService,
    private statsService: StatsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.quizService.getAll().subscribe(data => this.quizzes = data);
    this.loadDashboardStats();
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

  loadDashboardStats(): void {
    this.statsService.getDashboardStats().subscribe(data => {
      this.stats = data;
      this.quizStatsList = data.quizStats || [];
      this.mostFailedQuestions = data.mostFailedQuestions || [];
      this.buildCharts(data);
    });
  }

  buildCharts(data: DashboardStats): void {
    const quizLabels = data.quizStats.map(q => q.quizTitle.length > 15 ? q.quizTitle.substring(0, 15) + '…' : q.quizTitle);

    // Average Score per Quiz (bar chart)
    this.avgScoreChartData = {
      labels: quizLabels,
      datasets: [{
        data: data.quizStats.map(q => q.avgPercentage),
        backgroundColor: data.quizStats.map(q =>
          q.avgPercentage >= 80 ? '#10b981' : q.avgPercentage >= 50 ? '#f59e0b' : '#ef4444'
        ),
        borderRadius: 6,
        barThickness: 32
      }]
    };

    // Pass Rate per Quiz (horizontal bar)
    this.passRateChartData = {
      labels: quizLabels,
      datasets: [{
        data: data.quizStats.map(q => q.passRate),
        backgroundColor: data.quizStats.map(q =>
          q.passRate >= 70 ? '#10b981' : q.passRate >= 40 ? '#f59e0b' : '#ef4444'
        ),
        borderRadius: 6,
        barThickness: 24
      }]
    };

    // Grade Distribution (doughnut)
    const gd = data.gradeDistribution;
    this.gradeChartData = {
      labels: ['Excellent (≥80%)', 'Good (60-79%)', 'Average (50-59%)', 'Failed (<50%)'],
      datasets: [{
        data: [gd.excellent, gd.good, gd.average, gd.failed],
        backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#ef4444'],
        hoverOffset: 8,
        borderWidth: 2,
        borderColor: '#fff'
      }]
    };
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
      this.loadCertificateStatuses();
    });
  }

  private loadCertificateStatuses(): void {
    this.certificateMap.clear();
    for (const attempt of this.attempts) {
      if (attempt.id) {
        this.attemptService.getCertificateStatus(attempt.id).subscribe({
          next: (status) => {
            this.certificateMap.set(attempt.id!, {
              exists: status.exists,
              grade: status.grade,
              certificateId: status.certificateId
            });
          },
          error: () => {
            this.certificateMap.set(attempt.id!, { exists: false });
          }
        });
      }
    }
  }

  hasCertificate(attemptId: number): boolean {
    return this.certificateMap.get(attemptId)?.exists === true;
  }

  getCertificateGrade(attemptId: number): string {
    return this.certificateMap.get(attemptId)?.grade || '';
  }

  openCertificate(attemptId: number, event: Event): void {
    event.stopPropagation();
    const cert = this.certificateMap.get(attemptId);
    if (cert?.certificateId) {
      const url = this.attemptService.getCertificateViewUrl(cert.certificateId);
      window.open(url, '_blank');
    }
  }

  applyFilters(): void {
    this.currentPage = 1;
    this.loadPage();
  }

  getPercentage(attempt: Attempt): number {
    if (!attempt.score && attempt.score !== 0) return 0;
    if (!attempt.totalPoints || attempt.totalPoints === 0) return 0;
    return Math.round((attempt.score / attempt.totalPoints) * 100);
  }

  deleteAttempt(id: number): void {
    if (confirm('Delete this attempt?')) {
      this.attemptService.delete(id).subscribe({
        next: () => {
          this.loadPage();
          this.loadDashboardStats();
        },
        error: (err) => {
          console.error('Delete failed:', err);
          alert('Failed to delete attempt. Please check that the backend is running.');
        }
      });
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
    return this.getPercentage(attempt) >= 50 ? 'Passed' : 'Failed';
  }

  getPassBadgeClasses(attempt: Attempt): string {
    return this.getPercentage(attempt) >= 50
      ? 'bg-green-100 text-green-700'
      : 'bg-red-100 text-red-700';
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', {
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
    for (let i = 1; i <= this.totalPages; i++) pages.push(i);
    return pages;
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadPage();
    }
  }

  getFailBarWidth(failRate: number): string {
    return Math.min(failRate, 100) + '%';
  }

  getFailBarColor(failRate: number): string {
    if (failRate >= 70) return 'bg-red-500';
    if (failRate >= 40) return 'bg-yellow-500';
    return 'bg-green-500';
  }

  exportPdf(): void {
    if (this.exporting) return;
    this.exporting = true;
    this.attemptService.exportListPdf(this.selectedQuizId, this.searchQuizTitle, this.searchStudentName).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'attempts-report.pdf';
        a.click();
        window.URL.revokeObjectURL(url);
        this.exporting = false;
      },
      error: () => {
        this.exporting = false;
      }
    });
  }

  exportExcel(): void {
    if (this.exportingExcel) return;
    this.exportingExcel = true;
    this.attemptService.exportListExcel(this.selectedQuizId, this.searchQuizTitle, this.searchStudentName).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'attempts-report.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
        this.exportingExcel = false;
      },
      error: () => {
        this.exportingExcel = false;
      }
    });
  }
}
