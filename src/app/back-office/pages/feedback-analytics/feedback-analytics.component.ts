import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

interface GlobalIssue {
  keyword: string;
  count: number;
}

@Component({
  selector: 'app-feedback-analytics',
  templateUrl: './feedback-analytics.component.html',
  styleUrls: ['./feedback-analytics.component.scss']
})
export class FeedbackAnalyticsComponent implements OnInit {

  isLoading = true;
  issues: GlobalIssue[] = [];

  // Chart configuration
  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          color: '#333',
          font: { family: "'Inter', sans-serif", size: 14 }
        }
      },
      tooltip: {
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        titleColor: '#000',
        bodyColor: '#333',
        borderColor: '#e2e8f0',
        borderWidth: 1,
        padding: 12,
        boxPadding: 6,
        usePointStyle: true
      }
    }
  };
  
  public pieChartData: ChartData<'pie', number[], string | string[]> = {
    labels: [],
    datasets: [{
      data: [],
      backgroundColor: [
        '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#E7E9ED'
      ],
      hoverBackgroundColor: [
        '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#E7E9ED'
      ],
      borderWidth: 2,
      borderColor: '#ffffff',
      hoverOffset: 10
    }]
  };
  
  public pieChartType: ChartType = 'pie';

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.fetchAnalytics();
  }

  fetchAnalytics() {
    this.isLoading = true;
    this.http.get<GlobalIssue[]>('http://localhost:8222/api/feedbacks/analytics/issues/global').subscribe({
      next: (data) => {
        this.issues = data;
        
        // Update chart data
        if (data && data.length > 0) {
          this.pieChartData.labels = data.map(i => i.keyword);
          this.pieChartData.datasets[0].data = data.map(i => i.count);
          // Optional: Create dynamic colors based on length
          this.generateDynamicColors(data.length);
        }
        
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to fetch global issues', err);
        this.isLoading = false;
      }
    });
  }

  generateDynamicColors(count: number) {
    const defaultColors = ['#F97316', '#3B82F6', '#10B981', '#8B5CF6', '#EC4899', '#EAB308', '#06B6D4'];
    const backgrounds = [];
    
    for (let i = 0; i < count; i++) {
      backgrounds.push(defaultColors[i % defaultColors.length]);
    }
    
    this.pieChartData.datasets[0].backgroundColor = backgrounds;
    this.pieChartData.datasets[0].hoverBackgroundColor = backgrounds;
  }

  getColor(index: number): string {
    const bg = this.pieChartData.datasets[0].backgroundColor;
    if (Array.isArray(bg)) {
      return bg[index] as string;
    }
    return typeof bg === 'string' ? bg : '#3b82f6';
  }
}
