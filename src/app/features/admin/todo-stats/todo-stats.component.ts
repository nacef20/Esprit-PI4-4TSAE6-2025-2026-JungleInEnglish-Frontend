import { Component, OnInit, inject, signal, computed, DestroyRef } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { AnalyticsService, AnalyticsStudentDTO, AnalyticsTeacherDTO } from '../../../core/services/analytics.service';
import { AuthService } from '../../../core/services/auth.service';
import { toObservable } from '@angular/core/rxjs-interop';
import { skipWhile, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-todo-stats',
  standalone: true,
  imports: [RouterLink, CommonModule, BaseChartDirective],
  template: `
    <div class="stats-page">
      <header class="stats-header">
        <a [routerLink]="isTeacher() ? '/admin/todos' : '/todos'" class="back-link">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back to {{ isTeacher() ? 'Backoffice' : 'My Todos' }}
        </a>
        <h1>{{ isTeacher() ? 'Global Analytics' : 'My Productivity' }}</h1>
        <p class="subtitle">Advanced insights and performance data</p>
      </header>

      @if (loading()) {
        <div class="loading-state">
          <div class="spinner"></div>
          <p>Analyzing statistics...</p>
        </div>
      } @else if (error()) {
        <div class="error-state">
          <div class="error-icon">⚠️</div>
          <h2>Could not load analytics</h2>
          <p>Verify your connection and role permissions.</p>
          <div class="error-actions">
            <button (click)="loadData()" class="btn-retry">Retry</button>
            <button (click)="injectDemoData()" class="btn-demo">Check Demo View</button>
          </div>
        </div>
      } @else {
        <!-- Demo Warning -->
        <div *ngIf="isDemoMode" class="demo-banner">
          <span>Currently viewing <strong>Demo Data</strong> because the backend returned no records.</span>
        </div>
        <!-- Student Dashboard -->
        <ng-container *ngIf="!isTeacher()">
          <div *ngIf="studentData() as data" class="dashboard-grid">
            <div *ngIf="!isDemoMode && (data.personalCompleted === 0 || !data.personalCompleted) && (data.personalPending === 0 || !data.personalPending)" class="empty-data-hint">
              <p>No personal task history found. Start completing tasks to see your productivity trends!</p>
              <button (click)="injectDemoData()" class="btn-demo-inline">View Demo Version</button>
            </div>
            <div class="stat-highlights">
              <div class="stat-card minimal">
                <span class="stat-label">Avg. Completion Time</span>
                <span class="stat-value">{{ data.averageCompletionTimeHours.toFixed(1) }}h</span>
              </div>
              <div class="stat-card minimal">
                <span class="stat-label">Completion Rate</span>
                <span class="stat-value">{{ data.personalCompletionRate.toFixed(1) }}%</span>
              </div>
            </div>

            <div class="charts-row">
              <div class="chart-card">
                <h3>Personal Task Status</h3>
                <div class="chart-container">
                  <canvas baseChart
                    [data]="studentPieData()"
                    [options]="pieOptions"
                    [type]="'pie'">
                  </canvas>
                </div>
              </div>
              <div class="chart-card">
                <h3>Completion History (Activity)</h3>
                <div class="chart-container">
                  <canvas baseChart
                    [data]="studentLineData()"
                    [options]="lineOptions"
                    [type]="'line'">
                  </canvas>
                </div>
              </div>
            </div>
          </div>
        </ng-container>

        <!-- Teacher Dashboard -->
        <ng-container *ngIf="isTeacher()">
          <div *ngIf="teacherData() as data" class="dashboard-grid">
            <div *ngIf="!isDemoMode && (!data.studentEngagement || data.studentEngagement.length === 0)" class="empty-data-hint">
              <p>No student data found for the current period. Ensure students are active and completing their levels.</p>
              <button (click)="injectDemoData()" class="btn-demo-inline">View Demo Version</button>
            </div>
            <div class="charts-row">
              <div class="chart-card">
                <h3>Student Completion Rates</h3>
                <div class="chart-container">
                  <canvas baseChart
                    [data]="teacherBarData()"
                    [options]="barOptions"
                    [type]="'bar'">
                  </canvas>
                </div>
              </div>
              <div class="chart-card">
                <h3>Level Distribution</h3>
                <div class="chart-container">
                  <canvas baseChart
                    [data]="teacherPieData()"
                    [options]="pieOptions"
                    [type]="'pie'">
                  </canvas>
                </div>
              </div>
            </div>

            <div class="engagement-card">
              <h3>Student Overdue Engagement</h3>
              <div class="engagement-table-wrapper">
                <table class="engagement-table">
                  <thead>
                    <tr>
                      <th>Student</th>
                      <th>Overdue Tasks</th>
                      <th>Engagement Score</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let student of (data.studentEngagement ?? [])">
                      <td><strong>{{ student.username }}</strong></td>
                      <td>{{ student.overdueCount }}</td>
                      <td>
                        <div class="score-bar-container">
                          <div class="score-bar" [style.width.%]="student.engagementScore" 
                               [style.background-color]="getScoreColor(student.engagementScore)"></div>
                        </div>
                      </td>
                      <td>
                        <span class="status-badge" [class]="getEngagementClass(student.engagementScore)">
                          {{ student.engagementScore > 80 ? 'High' : (student.engagementScore > 40 ? 'Medium' : 'Low') }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </ng-container>
      }
    </div>
  `,
  styles: `
    .stats-page {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem 1rem;
    }
    .stats-header { margin-bottom: 2.5rem; }
    .back-link {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      color: #0ea37a;
      text-decoration: none;
      font-weight: 600;
      margin-bottom: 1rem;
    }
    .back-link svg { width: 18px; height: 18px; }
    .stats-header h1 { font-size: 2.25rem; font-weight: 800; color: #1e293b; margin: 0; }
    .subtitle { color: #64748b; font-size: 1.1rem; }

    .dashboard-grid { display: grid; gap: 2rem; }
    .stat-highlights { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem; }
    .stat-card.minimal {
      background: white; border-radius: 16px; padding: 1.5rem;
      border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
    }
    .stat-label { color: #64748b; font-size: 0.85rem; font-weight: 700; text-transform: uppercase; }
    .stat-value { display: block; font-size: 1.75rem; font-weight: 800; color: #0f172a; margin-top: 0.25rem; }

    .charts-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); gap: 2rem; }
    .chart-card {
      background: white; border-radius: 20px; padding: 1.5rem;
      border: 1px solid #e2e8f0; min-height: 400px;
      box-shadow: 0 10px 15px -3px rgb(0 0 0 / 0.1);
    }
    .chart-card h3 { font-size: 1.1rem; color: #1e293b; margin-bottom: 1.5rem; font-weight: 700; }
    .chart-container { height: 300px; position: relative; }

    .engagement-card {
      background: white; border-radius: 20px; padding: 2rem;
      border: 1px solid #e2e8f0; box-shadow: 0 10px 15px -3px rgb(0 0 0 / 0.1);
    }
    .engagement-card h3 { margin-bottom: 1.5rem; }
    .engagement-table-wrapper { overflow-x: auto; }
    .engagement-table { width: 100%; border-collapse: collapse; }
    .engagement-table th { text-align: left; padding: 1rem; border-bottom: 2px solid #f1f5f9; color: #64748b; font-size: 0.85rem; }
    .engagement-table td { padding: 1.25rem 1rem; border-bottom: 1px solid #f1f5f9; }
    
    .score-bar-container { width: 100px; height: 8px; background: #f1f5f9; border-radius: 4px; }
    .score-bar { height: 100%; border-radius: 4px; transition: width 1s ease-in-out; }
    
    .status-badge { padding: 0.25rem 0.75rem; border-radius: 20px; font-size: 0.75rem; font-weight: 700; }
    .status-badge.high { background: #dcfce7; color: #166534; }
    .status-badge.medium { background: #fef9c3; color: #854d0e; }
    .status-badge.low { background: #fee2e2; color: #991b1b; }

    .loading-state, .error-state { text-align: center; padding: 5rem 2rem; }
    .spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #0ea37a; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 1.5rem; }
    @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

    .demo-banner {
      background: #fffbeb;
      border: 1px solid #fde68a;
      color: #92400e;
      padding: 1rem;
      border-radius: 12px;
      margin-bottom: 2rem;
      text-align: center;
      font-weight: 500;
      animation: slideDown 0.5s ease-out;
    }
    .error-actions {
      display: flex;
      gap: 1rem;
      justify-content: center;
      margin-top: 1.5rem;
    }
    .btn-demo {
      background: #f1f5f9;
      color: #475569;
      border: none;
      padding: 0.75rem 2rem;
      border-radius: 12px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.2s;
    }
    .btn-demo:hover {
      background: #e2e8f0;
      color: #1e293b;
    }
    @keyframes slideDown {
      from { transform: translateY(-20px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }

    .empty-data-hint {
      grid-column: 1 / -1;
      background: #f8fafc;
      border: 2px dashed #e2e8f0;
      padding: 2rem;
      border-radius: 20px;
      text-align: center;
      color: #64748b;
    }
    .btn-demo-inline {
      margin-top: 1rem;
      background: #0ea37a;
      color: white;
      border: none;
      padding: 0.5rem 1.5rem;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
    }
  `
})
export class TodoStatsComponent implements OnInit {
  private analyticsService = inject(AnalyticsService);
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  studentData = signal<AnalyticsStudentDTO | null>(null);
  teacherData = signal<AnalyticsTeacherDTO | null>(null);
  loading = signal(true);
  error = signal(false);

  isTeacher() { return this.authService.isTeacher(); }

  // Chart Options
  public pieOptions: ChartConfiguration['options'] = { responsive: true, maintainAspectRatio: false };
  public lineOptions: ChartConfiguration['options'] = { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true } } };
  public barOptions: ChartConfiguration['options'] = { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true, max: 100 } } };

  // ================= STUDENT PIE =================
  studentPieData = computed<ChartData<'pie'>>(() => {
    const data = this.studentData();
    return {
      labels: ['Completed', 'Pending'],
      datasets: [{
        data: [data?.personalCompleted ?? 0, data?.personalPending ?? 0],
        backgroundColor: ['#10b981', '#f59e0b'],
        hoverBackgroundColor: ['#059669', '#d97706']
      }]
    };
  });

  // ================= STUDENT LINE =================
  studentLineData = computed<ChartData<'line'>>(() => {
    const history = this.studentData()?.completionHistory ?? {};
    const dates = Object.keys(history).sort();
    return {
      labels: dates,
      datasets: [{
        data: dates.map(d => history[d]),
        label: 'Daily Completions',
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        fill: true,
        tension: 0.4
      }]
    };
  });

  // ================= TEACHER BAR =================
  teacherBarData = computed<ChartData<'bar'>>(() => {
    const rates = this.teacherData()?.studentEngagement ?? [];
    return {
      labels: rates.map(r => r.username),
      datasets: [{
        data: rates.map(r => r.levelCompletionRate),
        label: 'Student Rate %',
        backgroundColor: '#6366f1'
      }]
    };
  });

  // ================= TEACHER PIE =================
  teacherPieData = computed<ChartData<'pie'>>(() => {
    const levels = this.teacherData()?.todosPerLevel ?? {};
    const labels = Object.keys(levels);
    return {
      labels: labels,
      datasets: [{
        data: labels.map(l => levels[l]),
        backgroundColor: ['#f43f5e', '#ec4899', '#d946ef', '#a855f7', '#8b5cf6', '#6366f1']
      }]
    };
  });

  // Reactive data loading: wait for user signal to be ready
  private _dataLoader = toObservable(this.authService.currentUser)
    .pipe(
      skipWhile(user => !user), // Only proceed when user is available
      switchMap(() => {
        this.loading.set(true);
        this.error.set(false);
        this.isDemoMode = false;
        return this.isTeacher()
          ? this.analyticsService.getTeacherStats()
          : this.analyticsService.getStudentStats();
      }),
      takeUntilDestroyed()
    )
    .subscribe({
      next: (data: any) => {
        if (this.isTeacher()) {
          this.teacherData.set(data);
        } else {
          this.studentData.set(data);
        }
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Analytics Loading Error:', err);
        this.error.set(true);
        this.loading.set(false);
      }
    });

  ngOnInit() { }

  loadData() {
    // This exists to trigger a reload manually if needed
    window.location.reload();
  }

  getScoreColor(score: number): string {
    if (score > 80) return '#10b981';
    if (score > 40) return '#f59e0b';
    return '#ef4444';
  }

  getEngagementClass(score: number): string {
    if (score > 80) return 'high';
    if (score > 40) return 'medium';
    return 'low';
  }

  isDemoMode = false;
  injectDemoData() {
    this.isDemoMode = true;
    this.error.set(false);
    if (this.isTeacher()) {
      this.teacherData.set({
        todosPerLevel: { 'A1': 10, 'B1': 5 },
        studentEngagement: [
          { username: 'student1', levelCompletionRate: 75, personalTodosCount: 5, overdueCount: 2, engagementScore: 65 },
          { username: 'student2', levelCompletionRate: 45, personalTodosCount: 2, overdueCount: 5, engagementScore: 30 }
        ],
        globalCompletionTrend: {},
        completionRatePerLevel: {}
      });
    } else {
      this.studentData.set({
        personalTotal: 20,
        personalCompleted: 15,
        personalPending: 5,
        personalCompletionRate: 75,
        levelTotal: 10,
        levelCompleted: 7,
        levelOverdue: 1,
        averageCompletionTimeHours: 4.5,
        personalStatusBreakdown: {},
        personalPriorityBreakdown: {},
        completionHistory: { '2024-01-01': 5, '2024-01-02': 3 }
      });
    }
  }
}

