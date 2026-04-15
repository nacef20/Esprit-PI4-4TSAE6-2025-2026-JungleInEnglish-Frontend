import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, ChartConfiguration, ChartData, registerables } from 'chart.js';
import { NgChartsModule } from 'ng2-charts';
import { ComplaintDashboardStats, ComplaintStatsPeriod } from '../../models/complaint-dashboard-stats.model';
import { ComplaintService } from '../../services/complaint.service';

Chart.register(...registerables);

@Component({
  selector: 'app-complaint-stats-dashboard',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
  templateUrl: './complaint-stats-dashboard.component.html',
  styleUrls: ['./complaint-stats-dashboard.component.scss']
})
export class ComplaintStatsDashboardComponent implements OnInit {

  stats: ComplaintDashboardStats | null = null;
  loading = false;
  exportingPdf = false;
  exportingExcel = false;
  selectedPeriod: ComplaintStatsPeriod = 'MONTH';

  statusChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  periodChartData: ChartData<'bar'> = { labels: [], datasets: [] };

  statusChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: { usePointStyle: true, padding: 16 }
      }
    }
  };

  periodChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          precision: 0
        }
      }
    }
  };

  constructor(private complaintService: ComplaintService) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading = true;
    this.complaintService.getDashboardStats(this.selectedPeriod).subscribe({
      next: (data) => {
        this.stats = data;
        this.buildCharts(data);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onPeriodChange(period: ComplaintStatsPeriod): void {
    if (this.selectedPeriod === period) {
      return;
    }
    this.selectedPeriod = period;
    this.loadStats();
  }

  exportAdminReportPdf(): void {
    if (this.exportingPdf) {
      return;
    }

    this.exportingPdf = true;
    this.complaintService.downloadAdminReportPdf().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `complaints-admin-report-${new Date().toISOString().slice(0, 10)}.pdf`;
        anchor.click();
        window.URL.revokeObjectURL(url);
        this.exportingPdf = false;
      },
      error: () => {
        this.exportingPdf = false;
      }
    });
  }

  exportAdminReportExcel(): void {
    if (this.exportingExcel) {
      return;
    }

    this.exportingExcel = true;
    this.complaintService.downloadAdminReportExcel().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `complaints-admin-report-${new Date().toISOString().slice(0, 10)}.xlsx`;
        anchor.click();
        window.URL.revokeObjectURL(url);
        this.exportingExcel = false;
      },
      error: () => {
        this.exportingExcel = false;
      }
    });
  }

  getStatusCount(status: 'EN_ATTENTE' | 'TRAITEE' | 'REJETEE'): number {
    return this.stats?.complaintsByStatus?.[status] ?? 0;
  }

  private buildCharts(data: ComplaintDashboardStats): void {
    this.statusChartData = {
      labels: ['Pending', 'Resolved', 'Rejected'],
      datasets: [{
        data: [
          data.complaintsByStatus?.EN_ATTENTE ?? 0,
          data.complaintsByStatus?.TRAITEE ?? 0,
          data.complaintsByStatus?.REJETEE ?? 0
        ],
        backgroundColor: ['#f59e0b', '#10b981', '#ef4444'],
        borderWidth: 2,
        borderColor: '#ffffff'
      }]
    };

    this.periodChartData = {
      labels: data.complaintsByPeriod.map(item => item.period),
      datasets: [{
        data: data.complaintsByPeriod.map(item => item.count),
        backgroundColor: '#0ea37a',
        borderRadius: 6,
        barThickness: 28
      }]
    };
  }
}
