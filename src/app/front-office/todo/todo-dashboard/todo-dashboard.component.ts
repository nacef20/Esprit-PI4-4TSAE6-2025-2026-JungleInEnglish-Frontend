import { Component, OnInit } from '@angular/core';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { TodoService } from '../../../services/todo.service';
import { AnalyticsStudent } from '../../../models/todo.model';

@Component({
  selector: 'app-todo-dashboard',
  templateUrl: './todo-dashboard.component.html',
  styleUrls: ['./todo-dashboard.component.scss']
})
export class TodoDashboardComponent implements OnInit {
  analytics: AnalyticsStudent | null = null;
  isLoading = true;

  // Pie Chart for Completion Rate
  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          color: '#333',
          font: { family: "'Outfit', sans-serif", size: 14 }
        }
      },
      tooltip: {
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        titleColor: '#000',
        bodyColor: '#333',
        borderColor: '#e2e8f0',
        borderWidth: 1,
        padding: 12
      }
    }
  };

  public pieChartData: ChartData<'pie', number[], string | string[]> = {
    labels: ['Completed', 'Pending'],
    datasets: [{
      data: [0, 0],
      backgroundColor: ['#10B981', '#F59E0B'], // emerald-500, amber-500
      hoverBackgroundColor: ['#059669', '#D97706'],
      borderWidth: 0
    }]
  };
  
  public pieChartType: ChartType = 'pie';

  constructor(private todoService: TodoService) { }

  ngOnInit(): void {
    this.fetchAnalytics();
  }

  fetchAnalytics(): void {
    this.isLoading = true;
    this.todoService.getStudentAnalytics().subscribe({
      next: (data: AnalyticsStudent) => {
        this.analytics = data;
        this.pieChartData.datasets[0].data = [data.completedTodos, data.pendingTodos];
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Failed to fetch analytics', err);
        this.isLoading = false;
      }
    });
  }
}
