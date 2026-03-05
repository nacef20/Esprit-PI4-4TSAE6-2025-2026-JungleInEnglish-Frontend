import { Component, OnInit, inject, signal, computed, effect } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { AsyncPipe, DatePipe, NgIf, NgForOf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BehaviorSubject, switchMap, map, shareReplay, Subscription } from 'rxjs';
import { TodoService } from '../../services/todo.service';
import { AuthService } from '../../../../core/services/auth.service';
import type { Todo, TodoStatus, Priority } from '../../models/todo.model';
import { toSignal } from '@angular/core/rxjs-interop';

export interface CalendarDay {
  date: Date;
  todos: Todo[];
  isToday: boolean;
  isCurrentMonth: boolean;
}

@Component({
  selector: 'app-todo-list',
  standalone: true,
  imports: [RouterLink, AsyncPipe, DatePipe, NgIf, NgForOf, FormsModule],
  template: `
    <div class="todos-page">
      <header class="todos-header">
        <div class="header-content">
          <h1>{{ isTeacher() ? 'Backoffice' : 'My Todos' }}</h1>
          <p class="subtitle">{{ isTeacher() ? 'Manage and assign tasks' : 'Personal & Level-based tasks' }}</p>
        </div>
        <div class="header-actions">
          <button (click)="toggleCalendar()" class="btn-calendar" [class.active]="showCalendar()">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            {{ showCalendar() ? 'Hide Calendar' : 'View Calendar' }}
          </button>
          <button [routerLink]="isTeacher() ? '/admin/todos/stats' : '/todos/stats'" class="btn-stats">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            Analytics
          </button>
        </div>
      </header>

      <!-- Calendar Section -->
      @if (showCalendar()) {
        <div class="calendar-section card mb-2rem animate-slide-down">
          <div class="calendar-header">
            <button (click)="previousMonth()" class="btn-nav">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" /></svg>
            </button>
            <h3>{{ currentDateSignal() | date:'MMMM yyyy' }}</h3>
            <button (click)="nextMonth()" class="btn-nav">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" /></svg>
            </button>
          </div>
          
          <div class="calendar-grid-container">
            <div class="calendar-weekdays">
              <span>Sun</span><span>Mon</span><span>Tue</span><span>Wed</span><span>Thu</span><span>Fri</span><span>Sat</span>
            </div>
            <div class="calendar-days">
              <div *ngFor="let day of calendarDays()" 
                   class="calendar-day" 
                   [class.today]="day.isToday"
                   [class.other-month]="!day.isCurrentMonth"
                   [class.selected]="selectedDate()?.getTime() === day.date.getTime()"
                   (click)="selectDay(day)">
                <span class="day-number">{{ day.date | date:'d' }}</span>
                <div class="day-todos-list">
                  <div *ngFor="let todo of day.todos.slice(0, 2)" 
                       class="day-todo-item" 
                       [class.checked]="todo.checked"
                       [class.p-high]="todo.priority === 'HIGH'"
                       [title]="todo.title">
                    {{ todo.title }}
                  </div>
                  <div *ngIf="day.todos.length > 2" class="more-count">
                    +{{ day.todos.length - 2 }} more
                  </div>
                </div>
              </div>
            </div>
          </div>

          @if (selectedDate()) {
            <div class="selected-day-details animate-fade-in">
              <h4>Tasks for {{ selectedDate() | date:'longDate' }}</h4>
              @if (selectedTodos().length === 0) {
                <p class="empty-inline">No tasks for this day.</p>
              } @else {
                <div class="mini-todo-list">
                  <div *ngFor="let todo of selectedTodos()" class="mini-todo-item">
                    <span class="status-dot" [class.checked]="todo.checked"></span>
                    <span class="mini-title">{{ todo.title }}</span>
                  </div>
                </div>
              }
            </div>
          }
        </div>
      }

      <div class="todo-creation-section card mb-2rem">
        <form (ngSubmit)="createTodo()" class="creation-form">
          <div class="form-grid">
            <div class="input-group">
              <input 
                name="title" 
                [(ngModel)]="newTodo.title" 
                placeholder="What needs to be done?" 
                required
                class="main-input"
              />
            </div>
            @if (isTeacher()) {
              <div class="select-group">
                <select name="level" [(ngModel)]="newTodo.level">
                  <option value="">Public (All)</option>
                  <option *ngFor="let lv of levels" [value]="lv">{{ lv }}</option>
                </select>
              </div>
            }
            <button type="submit" class="btn-create" [disabled]="!newTodo.title">
              Add Todo
            </button>
          </div>
        </form>
      </div>

      <ng-container *ngIf="todos$ | async as todos; else loading">
        <!-- Personal Todos Section -->
        <div class="section-title">
          <h2>My Personal Tasks</h2>
          <span class="count-badge">{{ (personalTodos$ | async)?.length || 0 }}</span>
        </div>
        
        <ng-container *ngIf="(personalTodos$ | async) as pTodos">
          @if (pTodos.length === 0) {
            <p class="empty-inline">No personal tasks yet.</p>
          } @else {
            <div class="todos-grid mb-2rem">
              <div *ngFor="let todo of pTodos; trackBy: trackById"
                 class="todo-card"
                 [class.checked]="todo.checked">
                <div class="todo-header">
                  <div class="todo-checkbox-wrapper">
                    <div class="todo-checkbox" [class.checked]="todo.checked" (click)="toggleChecked(todo)">
                      <svg *ngIf="todo.checked" xmlns="http://www.w3.org/2000/svg" fill="none"
                           viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3"
                              d="M5 13l4 4L19 7" />
                      </svg>
                    </div>
                  </div>
                  @if (isTeacher()) {
                    <span class="todo-status-badge" [class]="'status-' + todo.status.toLowerCase()">
                      {{ todo.status }}
                    </span>
                  }
                </div>
                  <h3 class="todo-title" (click)="goToDetail(todo.id)">{{ todo.title }}</h3>
                
                <button type="button" class="btn-icon btn-delete" (click)="deleteTodo(todo)" title="Delete task">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
                
                @if (isTeacher()) {
                  <p *ngIf="todo.description" class="todo-description">{{ todo.description }}</p>
                  <div class="todo-footer">
                    <span class="todo-priority" [class]="'priority-' + todo.priority.toLowerCase()">
                      {{ todo.priority }}
                    </span>
                    <span *ngIf="todo.dueDate" class="todo-date">
                      <svg xmlns="http://www.w3.org/2000/svg" fill="none"
                           viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      {{ todo.dueDate | date:'short' }}
                    </span>
                  </div>
                }
              </div>
            </div>
          }
        </ng-container>

        <!-- Level Todos Section -->
        <div class="section-title">
          <h2>Assignments (Level {{ currentUser()?.level || 'N/A' }})</h2>
          <span class="count-badge count-level">{{ (levelTodos$ | async)?.length || 0 }}</span>
        </div>

        <ng-container *ngIf="(levelTodos$ | async) as lTodos">
          @if (lTodos.length === 0) {
            <p class="empty-inline">No assignments for your level.</p>
          } @else {
            <div class="todos-grid">
              <div *ngFor="let todo of lTodos; trackBy: trackById"
                 class="todo-card todo-card-level"
                 [class.checked]="todo.checked">
                <div class="todo-header">
                  @if (isTeacher()) {
                    <div class="todo-checkbox-wrapper">
                      <div class="todo-checkbox" [class.checked]="todo.checked" (click)="toggleChecked(todo)">
                        <svg *ngIf="todo.checked" xmlns="http://www.w3.org/2000/svg" fill="none"
                             viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3"
                                d="M5 13l4 4L19 7" />
                        </svg>
                      </div>
                    </div>
                  } @else {
                    <div class="todo-assignment-badge">
                      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                      </svg>
                      Assignment
                    </div>
                  }
                  @if (isTeacher()) {
                    <span class="todo-status-badge" [class]="'status-' + todo.status.toLowerCase()">
                      {{ todo.status }}
                    </span>
                  }
                </div>
                  <h3 class="todo-title" (click)="goToDetail(todo.id)">{{ todo.title }}</h3>

                @if (isTeacher()) {
                  <button type="button" class="btn-icon btn-delete" (click)="deleteTodo(todo)" title="Delete assignment">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                }
                
                @if (isTeacher() || true) { <!-- Always show description for shared assignments? No, follow original minimal rule -->
                  @if (isTeacher()) {
                    <p *ngIf="todo.description" class="todo-description">{{ todo.description }}</p>
                    <div class="todo-footer">
                      <span class="todo-priority" [class]="'priority-' + todo.priority.toLowerCase()">
                        {{ todo.priority }}
                      </span>
                      <span *ngIf="todo.dueDate" class="todo-date">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none"
                             viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        {{ todo.dueDate | date:'short' }}
                      </span>
                    </div>
                  }
                }
              </div>
            </div>
          }
        </ng-container>
      </ng-container>

      <ng-template #loading>
        <div class="loading-state">
          <div class="spinner"></div>
          <p>Loading todos...</p>
        </div>
      </ng-template>
    </div>
  `,
  styles: `
    .todos-page {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem 1rem;
    }
    .todos-header {
      margin-bottom: 2.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1.5rem;
    }
    .header-content {
      text-align: left;
    }
    .btn-stats {
      background: #0ea37a;
      color: white;
      border: none;
      padding: 0.75rem 1.5rem;
      border-radius: 12px;
      font-weight: 700;
      display: flex;
      align-items: center;
      gap: 0.6rem;
      cursor: pointer;
      transition: all 0.3s ease;
      box-shadow: 0 4px 12px rgba(14, 163, 122, 0.2);
    }
    .btn-stats:hover {
      background: #0b8a66;
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(14, 163, 122, 0.3);
    }

    .card {
      background: white;
      border-radius: 16px;
      padding: 1.25rem;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
      border: 1px solid #e2e8f0;
    }

    .mb-2rem { margin-bottom: 2rem; }

    .todo-creation-section {
      background: #f8fafc;
      border: 1px dashed #cbd5e1;
    }

    .form-grid {
      display: flex;
      gap: 1rem;
      align-items: center;
    }

    .input-group { flex: 1; }

    .main-input {
      width: 100%;
      background: transparent;
      border: none;
      border-bottom: 2px solid #e2e8f0;
      padding: 0.75rem 0;
      font-size: 1.1rem;
      font-weight: 500;
      transition: border-color 0.2s;
    }

    .main-input:focus {
      outline: none;
      border-color: #0ea37a;
    }

    .select-group select {
      padding: 0.6rem 1rem;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
      background: white;
      font-size: 0.9rem;
      color: #64748b;
    }

    .btn-create {
      background: #0ea37a;
      color: white;
      border: none;
      padding: 0.75rem 1.5rem;
      border-radius: 10px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-create:hover:not(:disabled) {
      background: #0b8a66;
    }

    .btn-create:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .section-title {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1.25rem;
      border-left: 4px solid #0ea37a;
      padding-left: 1rem;
    }

    .section-title h2 {
      margin: 0;
      font-size: 1.1rem;
      font-weight: 600;
      color: #334155;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .count-badge {
      background: #f1f5f9;
      color: #64748b;
      padding: 0.2rem 0.6rem;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 700;
    }

    .count-level {
      background: #0ea37a;
      color: white;
    }

    .empty-inline {
      color: #94a3b8;
      font-size: 0.9rem;
      font-style: italic;
      margin-bottom: 2rem;
    }

    .todo-card-level {
      border-left: 4px solid #2563eb;
    }

    .todo-title {
      cursor: pointer;
    }

    .todo-title:hover {
      color: #0ea37a;
    }

    .btn-icon {
      background: none;
      border: none;
      padding: 0.4rem;
      cursor: pointer;
      border-radius: 6px;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;
      position: absolute;
      right: 0.75rem;
      top: 50%;
      transform: translateY(-50%);
      opacity: 0;
    }

    .todo-card:hover .btn-icon {
      opacity: 1;
    }

    .btn-delete {
      color: #94a3b8;
    }

    .btn-delete:hover {
      background: #fee2e2;
      color: #ef4444;
    }

    .btn-icon svg {
      width: 18px;
      height: 18px;
    }

    .todo-assignment-badge {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      font-size: 0.75rem;
      font-weight: 600;
      color: #2563eb;
      background: #eff6ff;
      padding: 0.25rem 0.6rem;
      border-radius: 8px;
    }

    .todo-assignment-badge svg {
      width: 14px;
      height: 14px;
    }
    .btn-stats svg {
      width: 20px;
      height: 20px;
    }
    .todos-header h1 {
      font-size: 2.5rem;
      font-weight: 700;
      color: #0ea37a;
      margin: 0 0 0.5rem 0;
      letter-spacing: -0.02em;
    }
    .subtitle {
      color: #64748b;
      font-size: 1.1rem;
      margin: 0;
    }
    .todos-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 1.5rem;
    }
    .todo-card {
      background: white;
      border-radius: 16px;
      padding: 1.5rem;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
      transition: all 0.3s ease;
      text-decoration: none;
      color: inherit;
      display: flex;
      flex-direction: column;
      gap: 1rem;
      border: 2px solid #e2e8f0;
      position: relative;
      overflow: hidden;
    }
    .todo-card::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 4px;
      height: 100%;
      background: #0ea37a;
      transform: scaleY(0);
      transition: transform 0.3s ease;
    }
    .todo-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px rgba(14, 163, 122, 0.15);
      border-color: #0ea37a;
    }
    .todo-card:hover::before {
      transform: scaleY(1);
    }
    .todo-card.checked {
      opacity: 0.7;
      background: #f8fafc;
    }
    .todo-card.checked .todo-title {
      text-decoration: line-through;
      color: #94a3b8;
    }
    .todo-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .todo-checkbox-wrapper {
      display: flex;
      align-items: center;
    }
    .todo-checkbox {
      width: 28px;
      height: 28px;
      border: 2px solid #cbd5e1;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      background: white;
      cursor: pointer;
    }
    .todo-checkbox.checked {
      background: #0ea37a;
      border-color: #0ea37a;
      transform: scale(1.1);
      animation: checkPop 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .todo-checkbox svg {
      width: 18px;
      height: 18px;
      color: white;
      stroke-width: 3;
    }
    @keyframes checkPop {
      0% { transform: scale(1); }
      50% { transform: scale(1.2); }
      100% { transform: scale(1.1); }
    }
    .todo-status-badge {
      font-size: 0.75rem;
      font-weight: 600;
      padding: 0.35rem 0.75rem;
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
    .todo-title {
      font-size: 1.25rem;
      font-weight: 700;
      color: #1e293b;
      margin: 0;
      line-height: 1.3;
      transition: color 0.2s;
    }
    .todo-description {
      color: #64748b;
      font-size: 0.95rem;
      line-height: 1.5;
      margin: 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .todo-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 0.75rem;
      border-top: 1px solid #f1f5f9;
      margin-top: auto;
    }
    .todo-priority {
      font-size: 0.8rem;
      font-weight: 600;
      padding: 0.25rem 0.6rem;
      border-radius: 12px;
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
    .todo-date {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      color: #64748b;
      font-size: 0.85rem;
    }
    .todo-date svg {
      width: 14px;
      height: 14px;
    }
    .empty-state, .loading-state {
      text-align: center;
      padding: 4rem 2rem;
    }
    .empty-icon {
      font-size: 4rem;
      margin-bottom: 1rem;
    }
    .empty-state p, .loading-state p {
      color: #64748b;
      font-size: 1.1rem;
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

    /* Calendar Premium Styles */
    .header-actions { display: flex; gap: 1rem; }
    .btn-calendar {
      background: white; color: #0ea37a; border: 2px solid #0ea37a;
      padding: 0.75rem 1.5rem; border-radius: 12px; font-weight: 700;
      display: flex; align-items: center; gap: 0.6rem; cursor: pointer;
      transition: all 0.3s ease;
    }
    .btn-calendar.active { background: #0ea37a; color: white; }
    .btn-calendar:hover { background: #f0fdf4; }
    .btn-calendar.active:hover { background: #0b8a66; }
    .btn-calendar svg { width: 20px; height: 20px; }

    .calendar-section { background: rgba(255, 255, 255, 0.8); backdrop-filter: blur(10px); }
    .calendar-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
    .calendar-header h3 { font-size: 1.5rem; color: #1e293b; margin: 0; }
    .btn-nav { background: #f1f5f9; border: none; padding: 0.5rem; border-radius: 8px; cursor: pointer; color: #64748b; }
    .btn-nav:hover { background: #e2e8f0; color: #1e293b; }
    .btn-nav svg { width: 24px; height: 24px; }

    .calendar-grid-container { background: white; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; }
    .calendar-weekdays { display: grid; grid-template-columns: repeat(7, 1fr); background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
    .calendar-weekdays span { padding: 0.75rem; text-align: center; font-size: 0.85rem; font-weight: 700; color: #64748b; }
    
    .calendar-days { display: grid; grid-template-columns: repeat(7, 1fr); }
    .calendar-day { 
      min-height: 100px; padding: 0.5rem; border-right: 1px solid #f1f5f9; border-bottom: 1px solid #f1f5f9;
      cursor: pointer; transition: all 0.2s; display: flex; flex-direction: column; align-items: flex-start;
    }
    .calendar-day:nth-child(7n) { border-right: none; }
    .calendar-day:hover { background: #f0fdf4; }
    .calendar-day.other-month { opacity: 0.3; }
    .calendar-day.today { background: #fdf2f8; }
    .calendar-day.today .day-number { background: #db2777; color: white; width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; border-radius: 50%; }
    .calendar-day.selected { background: #0ea37a; color: white; }
    .calendar-day.selected .day-indicators .dot { background: white !important; }

    .day-number { font-weight: 700; font-size: 1rem; margin-bottom: 0.75rem; color: #64748b; }
    .day-todos-list { 
      width: 100%; display: flex; flex-direction: column; gap: 4px; overflow: hidden;
    }
    .day-todo-item {
      font-size: 0.7rem; padding: 2px 6px; border-radius: 4px;
      white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
      background: #f1f5f9; color: #475569; border-left: 3px solid #cbd5e1;
      font-weight: 500;
    }
    .day-todo-item.checked { opacity: 0.5; text-decoration: line-through; background: #f8fafc; border-left-color: #94a3b8; }
    .day-todo-item.p-high { background: #fee2e2; color: #991b1b; border-left-color: #ef4444; }
    .calendar-day.selected .day-todo-item { background: rgba(255,255,255,0.2); color: white; border-left-color: white; }

    .more-count { font-size: 0.65rem; color: #94a3b8; font-weight: 700; text-align: center; margin-top: 2px; }
    .calendar-day.selected .more-count { color: white; opacity: 0.8; }

    .selected-day-details { margin-top: 2rem; padding-top: 1.5rem; border-top: 1px dashed #cbd5e1; }
    .selected-day-details h4 { margin-top: 0; color: #1e293b; }
    .mini-todo-list { display: flex; flex-direction: column; gap: 0.75rem; }
    .mini-todo-item { display: flex; align-items: center; gap: 0.75rem; background: #f8fafc; padding: 0.75rem 1rem; border-radius: 10px; }
    .status-dot { width: 10px; height: 10px; border-radius: 50%; background: #e2e8f0; border: 2px solid #cbd5e1; }
    .status-dot.checked { background: #0ea37a; border-color: #0ea37a; }
    .mini-title { font-size: 0.95rem; font-weight: 500; color: #334155; }

    .animate-slide-down { animation: slideDown 0.4s ease-out; }
    .animate-fade-in { animation: fadeIn 0.3s ease-in; }
    @keyframes slideDown { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @media (max-width: 768px) {
      .todos-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
      }
      .todos-header h1 {
        font-size: 2rem;
      }
    }
  `,
})
export class TodoListComponent implements OnInit {
  private readonly todoService = inject(TodoService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  public readonly todos$ = this.refresh$.pipe(
    switchMap(() => this.todoService.getAll()),
    shareReplay(1)
  );

  protected readonly personalTodos$ = this.todos$.pipe(
    map((todos: Todo[]) => todos.filter(t => !t.level))
  );

  protected readonly levelTodos$ = this.todos$.pipe(
    map((todos: Todo[]) => todos.filter(t => !!t.level))
  );

  protected readonly levels = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2'];
  protected newTodo: Partial<Todo> = this.resetForm();

  currentUser = this.authService.currentUser;

  // Calendar-related properties
  protected readonly todosSignal = toSignal(this.todos$, { initialValue: [] });
  protected readonly currentDateSignal = signal(new Date());
  protected readonly calendarDays = computed(() => this.generateCalendarDays(this.currentDateSignal(), this.todosSignal()));
  protected readonly selectedDate = signal<Date | null>(null);
  protected readonly selectedTodos = computed(() => {
    const date = this.selectedDate();
    if (!date) return [];
    return this.getTodosForDate(date, this.todosSignal());
  });
  protected readonly showCalendar = signal(false);

  private generateCalendarDays(currentDate: Date, todos: Todo[]): CalendarDay[] {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);

    const startDate = new Date(firstDay);
    startDate.setDate(startDate.getDate() - firstDay.getDay());

    const endDate = new Date(lastDay);
    endDate.setDate(endDate.getDate() + (6 - lastDay.getDay()));

    const days: CalendarDay[] = [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const tempDate = new Date(startDate);
    while (tempDate <= endDate) {
      const date = new Date(tempDate);
      days.push({
        date: date,
        todos: this.getTodosForDate(date, todos),
        isToday: date.getTime() === today.getTime(),
        isCurrentMonth: date.getMonth() === month
      });
      tempDate.setDate(tempDate.getDate() + 1);
    }
    return days;
  }

  private getTodosForDate(date: Date, todos: Todo[]): Todo[] {
    const checkDate = new Date(date);
    checkDate.setHours(0, 0, 0, 0);

    return todos.filter(todo => {
      if (!todo.dueDate) return false;
      const dDate = new Date(todo.dueDate);
      dDate.setHours(0, 0, 0, 0);
      return checkDate.getTime() === dDate.getTime();
    });
  }

  previousMonth() {
    const date = this.currentDateSignal();
    this.currentDateSignal.set(new Date(date.getFullYear(), date.getMonth() - 1, 1));
  }

  nextMonth() {
    const date = this.currentDateSignal();
    this.currentDateSignal.set(new Date(date.getFullYear(), date.getMonth() + 1, 1));
  }

  selectDay(day: CalendarDay) {
    this.selectedDate.set(day.date);
  }

  toggleCalendar() {
    this.showCalendar.update(v => !v);
  }

  isTeacher() {
    return this.authService.isTeacher();
  }

  ngOnInit(): void { }

  private resetForm(): Partial<Todo> {
    return {
      title: '',
      status: 'PENDING',
      priority: 'MEDIUM',
      checked: false,
      level: ''
    };
  }

  createTodo() {
    if (!this.newTodo.title) return;

    // Student creation implies level = null (personal task)
    const payload = {
      ...this.newTodo,
      dueDate: this.newTodo.dueDate || undefined,
      level: this.isTeacher() ? (this.newTodo.level || undefined) : undefined
    } as Todo;

    this.todoService.create(payload).subscribe({
      next: () => {
        this.newTodo = this.resetForm();
        this.refresh$.next();
      },
      error: (err: unknown) => console.error('Error creating todo:', err)
    });
  }

  toggleChecked(todo: Todo): void {
    if (todo.id == null) return;

    // Safety Guard: Students can't check level-based tasks
    if (!this.isTeacher() && todo.level) {
      console.warn('Students cannot modify level-based assignments');
      return;
    }

    const updatedTodo: Todo = {
      ...todo,
      checked: !todo.checked,
    };
    this.todoService.update(todo.id, updatedTodo).subscribe({
      next: (updated: Todo) => {
        todo.checked = updated.checked;
        this.refresh$.next();
      },
      error: (err: unknown) => {
        console.error('Error toggling todo:', err);
        this.refresh$.next();
      },
    });
  }

  deleteTodo(todo: Todo): void {
    if (todo.id == null) return;

    // Safety Guard: Students can't delete level-based tasks
    if (!this.isTeacher() && todo.level) {
      console.warn('Students cannot delete level-based assignments');
      return;
    }

    if (!confirm('Are you sure you want to delete this task?')) return;

    this.todoService.delete(todo.id).subscribe({
      next: () => {
        this.refresh$.next();
      },
      error: (err: unknown) => {
        console.error('Error deleting todo:', err);
      }
    });
  }

  goToDetail(id?: number) {
    if (id) {
      this.router.navigate(['/todos', id]);
    }
  }

  public trackById(index: number, todo: Todo): number {
    return todo.id ?? index;
  }
}