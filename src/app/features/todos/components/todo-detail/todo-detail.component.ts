import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { TodoService } from '../../services/todo.service';
import type { Todo } from '../../models/todo.model';

@Component({
  selector: 'app-todo-detail',
  standalone: true,
  imports: [RouterLink, DatePipe],
  template: `
    <div class="todo-detail-page">
      <a routerLink="/todos" class="back-link">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        Back to Todos
      </a>
      @if (todo(); as td) {
        <div class="todo-detail-card" [class.checked]="td.checked">
          <div class="todo-detail-header">
            <div class="todo-checkbox-large" [class.checked]="td.checked">
              @if (td.checked) {
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
                </svg>
              }
            </div>
            <div class="todo-header-info">
              <h1 class="todo-title" [class.checked]="td.checked">{{ td.title }}</h1>
              <div class="todo-badges">
                <span class="todo-status-badge" [class]="'status-' + td.status.toLowerCase()">
                  {{ td.status }}
                </span>
                <span class="todo-priority-badge" [class]="'priority-' + td.priority.toLowerCase()">
                  {{ td.priority }}
                </span>
              </div>
            </div>
          </div>
          @if (td.description) {
            <div class="todo-description-section">
              <h2>Description</h2>
              <p>{{ td.description }}</p>
            </div>
          }
          <div class="todo-details-grid">
            @if (td.dueDate) {
              <div class="detail-item">
                <div class="detail-icon calendar">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <div class="detail-content">
                  <span class="detail-label">Due Date</span>
                  <span class="detail-value">{{ td.dueDate | date:'medium' }}</span>
                </div>
              </div>
            }
            @if (td.createdAt) {
              <div class="detail-item">
                <div class="detail-icon created">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div class="detail-content">
                  <span class="detail-label">Created</span>
                  <span class="detail-value">{{ td.createdAt | date:'medium' }}</span>
                </div>
              </div>
            }
          </div>
        </div>
      } @else if (loading()) {
        <div class="loading-state">
          <div class="spinner"></div>
          <p>Loading todo...</p>
        </div>
      } @else {
        <div class="error-state">
          <h2>Todo not found</h2>
          <p>The todo you're looking for doesn't exist or has been removed.</p>
          <a routerLink="/todos" class="back-link">← Back to Todos</a>
        </div>
      }
    </div>
  `,
  styles: `
    .todo-detail-page {
      max-width: 900px;
      margin: 0 auto;
      padding: 2rem 1rem;
    }
    .back-link {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      color: #0ea37a;
      text-decoration: none;
      font-weight: 600;
      margin-bottom: 1.5rem;
      transition: color 0.2s;
    }
    .back-link:hover {
      color: #0b8a66;
    }
    .back-link svg {
      width: 20px;
      height: 20px;
    }
    .todo-detail-card {
      background: white;
      border-radius: 20px;
      padding: 2.5rem;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      border: 2px solid #e2e8f0;
      transition: all 0.3s ease;
    }
    .todo-detail-card.checked {
      background: #f8fafc;
      opacity: 0.9;
    }
    .todo-detail-header {
      display: flex;
      gap: 1.5rem;
      margin-bottom: 2rem;
      padding-bottom: 2rem;
      border-bottom: 2px solid #f1f5f9;
    }
    .todo-checkbox-large {
      width: 48px;
      height: 48px;
      border: 3px solid #cbd5e1;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      background: white;
      flex-shrink: 0;
    }
    .todo-checkbox-large.checked {
      background: #0ea37a;
      border-color: #0ea37a;
      transform: scale(1.05);
      animation: checkPopLarge 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .todo-checkbox-large svg {
      width: 28px;
      height: 28px;
      color: white;
      stroke-width: 3;
    }
    @keyframes checkPopLarge {
      0% { transform: scale(1); }
      50% { transform: scale(1.15); }
      100% { transform: scale(1.05); }
    }
    .todo-header-info {
      flex: 1;
    }
    .todo-title {
      font-size: 2.5rem;
      font-weight: 700;
      color: #0ea37a;
      margin: 0 0 1rem 0;
      line-height: 1.2;
      transition: all 0.3s;
    }
    .todo-title.checked {
      text-decoration: line-through;
      color: #94a3b8;
    }
    .todo-badges {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
    }
    .todo-status-badge, .todo-priority-badge {
      font-size: 0.85rem;
      font-weight: 600;
      padding: 0.5rem 1rem;
      border-radius: 20px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .status-pending {
      background: rgba(251, 191, 36, 0.15);
      color: #92400e;
    }
    .status-in_progress {
      background: rgba(59, 130, 246, 0.15);
      color: #1e40af;
    }
    .status-completed {
      background: rgba(14, 163, 122, 0.15);
      color: #065f46;
    }
    .status-cancelled {
      background: rgba(156, 163, 175, 0.15);
      color: #4b5563;
    }
    .priority-low {
      background: rgba(14, 163, 122, 0.1);
      color: #065f46;
    }
    .priority-medium {
      background: rgba(251, 191, 36, 0.15);
      color: #854d0e;
    }
    .priority-high {
      background: rgba(239, 68, 68, 0.1);
      color: #991b1b;
    }
    .todo-description-section {
      margin-bottom: 2rem;
    }
    .todo-description-section h2 {
      font-size: 1.25rem;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 0.75rem 0;
    }
    .todo-description-section p {
      font-size: 1.1rem;
      color: #64748b;
      line-height: 1.7;
      margin: 0;
    }
    .todo-details-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1.5rem;
    }
    .detail-item {
      display: flex;
      gap: 1rem;
      align-items: flex-start;
    }
    .detail-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .detail-icon svg {
      width: 24px;
      height: 24px;
    }
    .detail-icon.calendar {
      background: rgba(59, 130, 246, 0.1);
      color: #3b82f6;
    }
    .detail-icon.created {
      background: rgba(14, 163, 122, 0.1);
      color: #0ea37a;
    }
    .detail-content {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      flex: 1;
    }
    .detail-label {
      font-size: 0.875rem;
      color: #64748b;
      font-weight: 500;
    }
    .detail-value {
      font-size: 1rem;
      color: #1e293b;
      font-weight: 600;
    }
    .loading-state, .error-state {
      text-align: center;
      padding: 4rem 2rem;
    }
    .spinner {
      width: 48px;
      height: 48px;
      border: 4px solid #f1f5f9;
      border-top-color: #0ea37a;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin: 0 auto 1rem;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    .error-state h2 {
      color: #1e293b;
      margin-bottom: 0.5rem;
    }
    .error-state p {
      color: #64748b;
      margin-bottom: 1.5rem;
    }
    @media (max-width: 768px) {
      .todo-detail-header {
        flex-direction: column;
      }
      .todo-title {
        font-size: 2rem;
      }
      .todo-detail-card {
        padding: 1.5rem;
      }
    }
  `,
})
export class TodoDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly todoService = inject(TodoService);
  protected readonly todo = signal<Todo | null>(null);
  protected readonly loading = signal(true);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.todoService.getById(id).subscribe({
      next: (t) => {
        this.todo.set(t);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
