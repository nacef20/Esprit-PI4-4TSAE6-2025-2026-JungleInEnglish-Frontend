import { Component, OnInit } from '@angular/core';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { TodoService } from '../../../services/todo.service';
import { AnalyticsTeacher } from '../../../models/todo.model';

@Component({
  selector: 'app-admin-todo-dashboard',
  templateUrl: './admin-todo-dashboard.component.html',
  styleUrls: ['./admin-todo-dashboard.component.scss']
})
export class AdminTodoDashboardComponent implements OnInit {
  analytics: AnalyticsTeacher | null = null;
  isLoading = true;

  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { position: 'bottom' }
    }
  };

  public pieChartData: ChartData<'pie', number[], string | string[]> = {
    labels: ['Total Completed', 'Total Pending'],
    datasets: [{
      data: [0, 0],
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
    this.todoService.getTeacherAnalytics().subscribe({
      next: (data: AnalyticsTeacher) => {
        this.analytics = data;
        this.pieChartData.datasets[0].data = [data.totalCompletedTodos, data.totalPendingTodos];
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Failed to fetch admin analytics', err);
        this.isLoading = false;
      }
    });
  }
}
