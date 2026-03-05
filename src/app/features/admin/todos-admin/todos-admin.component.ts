import { Component, OnInit, inject, signal, computed, effect } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AsyncPipe, LowerCasePipe, DatePipe } from '@angular/common';
import { BehaviorSubject, switchMap, map, shareReplay, Subscription } from 'rxjs';
import { TodoService } from '../../todos/services/todo.service';
import { UserService } from '../../../core/services/user.service';
import type { Todo, TodoStatus, Priority } from '../../todos/models/todo.model';
import type { User } from '../../../core/services/auth.service';
import { AuthService } from '../../../core/services/auth.service';
import { toObservable } from '@angular/core/rxjs-interop';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-todos-admin',
  standalone: true,
  imports: [FormsModule, RouterLink, AsyncPipe, LowerCasePipe],
  template: `
    <div class="page">
      <header class="page-header">
        <div class="header-main">
          <h1>Todos backoffice</h1>
          <button routerLink="stats" class="btn btn-stats">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            View Statistics
          </button>
        </div>
        <p class="subtitle">Create, edit and delete todos.</p>
      </header>

      <section class="card card-form">
        <h2>{{ editingTodo ? 'Edit todo' : 'Add todo' }}</h2>
        <form (ngSubmit)="saveTodo()" class="form">
          <div class="form-row">
            <label for="title">Title</label>
            <input id="title" [(ngModel)]="form.title" name="title" placeholder="Todo title" required />
          </div>
          <div class="form-row">
            <label for="description">Description</label>
            <textarea id="description" [(ngModel)]="form.description" name="description" placeholder="Description" rows="3"></textarea>
          </div>
          <div class="form-row two-cols">
            <div>
              <label for="status">Status</label>
              <select id="status" [(ngModel)]="form.status" name="status">
                <option value="PENDING">PENDING</option>
                <option value="IN_PROGRESS">IN PROGRESS</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>
            <div>
              <label for="priority">Priority</label>
              <select id="priority" [(ngModel)]="form.priority" name="priority">
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </div>
            <div>
              <label for="level">Target Level</label>
              <select id="level" [(ngModel)]="form.level" name="level">
                <option value="">All Levels</option>
                <option value="A1">A1</option>
                <option value="A2">A2</option>
                <option value="B1">B1</option>
                <option value="B2">B2</option>
                <option value="C1">C1</option>
                <option value="C2">C2</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <label for="notificationEmail">Notification Email</label>
            <input id="notificationEmail" [(ngModel)]="form.notificationEmail" name="notificationEmail" type="email" placeholder="user@example.com" />
          </div>
          <div class="form-row">
            <label for="dueDate">Due Date</label>
            <input id="dueDate" [(ngModel)]="form.dueDate" name="dueDate" type="datetime-local" />
          </div>
          <div class="form-row checkbox-row">
            <label class="custom-checkbox-label" for="checked">
              <input id="checked" [(ngModel)]="form.checked" type="checkbox" name="checked" class="custom-checkbox-input" />
              <span class="custom-checkbox" [class.checked]="form.checked">
                @if (form.checked) {
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
                  </svg>
                }
              </span>
              <span class="checkbox-text">Mark as checked</span>
            </label>
          </div>
          <div class="form-actions">
            @if (editingTodo) {
              <button type="button" class="btn btn-secondary" (click)="cancelEdit()">Cancel</button>
            }
            <button type="submit" class="btn btn-primary">
              {{ editingTodo ? 'Update todo' : 'Create todo' }}
            </button>
          </div>
        </form>
      </section>

      <section class="card card-table">
        <h2>All todos</h2>
        @if (todos$ | async; as todos) {
          @if (todos.length === 0) {
            <p class="empty">No todos yet. Create one above.</p>
          } @else {
            <div class="table-wrap">
              <table class="data-table">
                <thead>
                  <tr>
                    <th class="col-check">Check</th>
                    <th>Title</th>
                    <th>Status</th>
                    <th>Priority</th>
                    <th>Level</th>
                    <th class="col-actions">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  @for (todo of todos; track todo.id) {
                    <tr [class.checked-row]="todo.checked">
                      <td class="col-check">
                        <input
                          type="checkbox"
                          [checked]="todo.checked"
                          (change)="toggleChecked(todo)"
                          class="todo-checkbox-input"
                        />
                      </td>
                      <td>
                        <strong [class.checked-text]="todo.checked">{{ todo.title }}</strong>
                      </td>
                      <td><span class="badge badge-{{ todo.status | lowercase }}">{{ todo.status }}</span></td>
                      <td><span class="badge badge-priority-{{ todo.priority | lowercase }}">{{ todo.priority }}</span></td>
                      <td><span class="badge badge-level-{{ (todo.level || 'all') | lowercase }}">{{ todo.level || 'All' }}</span></td>
                      <td class="col-actions">
                        <button type="button" class="btn btn-sm btn-edit" (click)="startEdit(todo)">Edit</button>
                        <button type="button" class="btn btn-sm btn-danger" (click)="deleteTodo(todo.id!)">Delete</button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        }
      </section>

      <section class="card card-table">
        <h2>Student Management</h2>
        @if (students$ | async; as students) {
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Current Level</th>
                  <th>Set Level</th>
                </tr>
              </thead>
              <tbody>
                @for (student of students; track student.id) {
                  <tr>
                    <td>{{ student.username }}</td>
                    <td>{{ student.email }}</td>
                    <td>
                      <span class="badge badge-level-{{ (student.level || '') | lowercase }}">{{ student.level || 'No level' }}</span>
                    </td>
                    <td>
                      <div class="level-actions">
                        @for (lv of levels; track lv) {
                          <button 
                            class="btn btn-xs" 
                            [class.btn-active]="student.level === lv"
                            (click)="assignLevel(student.id, lv)"
                          >
                            {{ lv }}
                          </button>
                        }
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </section>
    </div>
  `,
  styles: `
    .page {
      max-width: 960px;
      margin: 0 auto;
      font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif;
      color: #1a1a2e;
    }

    .page-header {
      margin-bottom: 2rem;
    }

    .header-main {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.5rem;
    }

    .btn-stats {
      background: #0ea37a;
      color: white;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.6rem 1.25rem;
    }

    .btn-stats:hover {
      background: #0b8a66;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(14, 163, 122, 0.2);
    }

    .btn-stats svg {
      width: 18px;
      height: 18px;
    }

    .page-header h1 {
      margin: 0 0 0.25rem 0;
      font-size: 1.75rem;
      font-weight: 700;
      letter-spacing: -0.02em;
    }

    .subtitle {
      margin: 0;
      color: #64748b;
      font-size: 0.95rem;
    }

    .card {
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
      border: 1px solid #e2e8f0;
      padding: 1.5rem;
      margin-bottom: 1.5rem;
    }

    .card h2 {
      margin: 0 0 1rem 0;
      font-size: 1.1rem;
      font-weight: 600;
      color: #334155;
    }

    .form .form-row {
      margin-bottom: 1rem;
    }

    .form .form-row label {
      display: block;
      font-size: 0.8rem;
      font-weight: 500;
      color: #475569;
      margin-bottom: 0.35rem;
    }

    .form input[type="text"],
    .form textarea,
    .form select {
      width: 100%;
      padding: 0.5rem 0.75rem;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 0.95rem;
      box-sizing: border-box;
    }

    .form input:focus,
    .form textarea:focus,
    .form select:focus {
      outline: none;
      border-color: #2563eb;
      box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
    }

    .form-row.two-cols {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }

    .form-row.checkbox-row {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .form-row.checkbox-row label {
      margin-bottom: 0;
      cursor: pointer;
    }

    .form-actions {
      display: flex;
      gap: 0.75rem;
      margin-top: 1.25rem;
    }

    .btn {
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-size: 0.9rem;
      font-weight: 500;
      cursor: pointer;
      border: none;
      transition: background 0.15s, color 0.15s;
    }

    .btn-primary {
      background: #2563eb;
      color: #fff;
    }

    .btn-primary:hover {
      background: #1d4ed8;
    }

    .btn-secondary {
      background: #f1f5f9;
      color: #475569;
    }

    .btn-secondary:hover {
      background: #e2e8f0;
    }

    .btn-edit {
      background: #e0f2fe;
      color: #0369a1;
    }

    .btn-edit:hover {
      background: #bae6fd;
    }

    .btn-danger {
      background: #fee2e2;
      color: #b91c1c;
    }

    .btn-danger:hover {
      background: #fecaca;
    }

    .btn-sm {
      padding: 0.35rem 0.65rem;
      font-size: 0.8rem;
      margin-right: 0.5rem;
    }

    .table-wrap {
      overflow-x: auto;
    }

    .data-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.9rem;
    }

    .data-table th,
    .data-table td {
      padding: 0.75rem;
      text-align: left;
      border-bottom: 1px solid #e2e8f0;
    }

    .data-table th {
      font-weight: 600;
      color: #475569;
      background: #f8fafc;
    }

    .data-table tbody tr:hover {
      background: #f8fafc;
    }

    .col-check {
      width: 60px;
      text-align: center;
      vertical-align: middle;
    }

    .todo-checkbox-input {
      width: 22px;
      height: 22px;
      cursor: pointer;
      accent-color: #0ea37a;
    }

    .checked-row {
      background: rgba(14, 163, 122, 0.03) !important;
      opacity: 0.85;
    }

    .checked-text {
      text-decoration: line-through;
      color: #94a3b8;
    }

    .col-actions {
      white-space: nowrap;
    }

    .badge {
      display: inline-block;
      padding: 0.2rem 0.5rem;
      border-radius: 6px;
      font-size: 0.8rem;
      font-weight: 500;
    }

    .badge-pending { background: #fef3c7; color: #92400e; }
    .badge-in_progress { background: #dbeafe; color: #1e40af; }
    .badge-completed { background: #d1fae5; color: #065f46; }
    .badge-cancelled { background: #f3f4f6; color: #4b5563; }
    .badge-priority-low { background: #f0fdf4; color: #166534; }
    .badge-priority-medium { background: #fef9c3; color: #854d0e; }
    .badge-priority-high { background: #fee2e2; color: #991b1b; }

    .badge-level-a1 { background: #f1f5f9; color: #475569; }
    .badge-level-a2 { background: #e0f2fe; color: #0369a1; }
    .badge-level-b1 { background: #dcfce7; color: #166534; }
    .badge-level-b2 { background: #fef9c3; color: #854d0e; }
    .badge-level-c1 { background: #ffedd5; color: #9a3412; }
    .badge-level-c2 { background: #fee2e2; color: #991b1b; }

    .level-actions {
      display: flex;
      gap: 0.25rem;
    }

    .btn-xs {
      padding: 0.2rem 0.4rem;
      font-size: 0.7rem;
      background: #f1f5f9;
      color: #64748b;
    }

    .btn-xs:hover {
      background: #e2e8f0;
    }

    .btn-active {
      background: #2563eb !important;
      color: white !important;
    }

    .empty {
      margin: 0;
      color: #64748b;
      font-size: 0.95rem;
    }
  `,
})
export class TodosAdminComponent implements OnInit {
  private readonly todoService = inject(TodoService);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly refresh$ = new BehaviorSubject<void>(undefined);
  private readonly refreshStudents$ = new BehaviorSubject<void>(undefined);

  // Combine currentUser state with refresh subjects to ensure data is fetched only after auth and on demand
  protected readonly todos$ = toObservable(this.authService.currentUser).pipe(
    switchMap(user => {
      if (!user) return [];
      return this.refresh$.pipe(
        switchMap(() => this.todoService.getAll())
      );
    }),
    shareReplay(1)
  );

  protected readonly students$ = toObservable(this.authService.currentUser).pipe(
    switchMap(user => {
      if (!user) return [];
      return this.refreshStudents$.pipe(
        switchMap(() => this.userService.getStudents())
      );
    }),
    shareReplay(1)
  );

  protected readonly levels = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2'];

  // ✅ Single unified form
  protected form: Partial<Todo> = this.getEmptyForm();

  protected editingTodo: Todo | null = null;

  ngOnInit(): void { }

  private getEmptyForm(): Partial<Todo> {
    return {
      title: '',
      description: '',
      status: 'PENDING',
      priority: 'MEDIUM',
      checked: false,
      dueDate: '',
      reminders: [],
      notificationEmail: '',
      level: ''
    };
  }

  protected saveTodo(): void {
    if (!this.form.title) return;

    if (this.editingTodo?.id != null) {
      const payload: Todo = {
        id: this.editingTodo.id,
        title: this.form.title!,
        description: this.form.description ?? '',
        status: (this.form.status as TodoStatus) ?? 'PENDING',
        priority: (this.form.priority as Priority) ?? 'MEDIUM',
        checked: this.form.checked ?? false,
        dueDate: this.form.dueDate || undefined, // Sanitize empty string
        reminders: this.form.reminders ?? [],
        notificationEmail: this.form.notificationEmail ?? '',
        level: this.form.level || undefined // Sanitize empty string
      };
      this.todoService.update(this.editingTodo.id, payload).subscribe({
        next: () => {
          this.editingTodo = null;
          this.form = this.getEmptyForm();
          this.refresh$.next();
        },
      });
    } else {
      const payload = {
        ...this.form,
        dueDate: this.form.dueDate || undefined,
        level: this.form.level || undefined
      } as Todo;

      this.todoService.create(payload).subscribe({
        next: () => {
          this.form = this.getEmptyForm();
          this.refresh$.next();
        },
      });
    }
  }

  protected startEdit(todo: Todo): void {
    this.editingTodo = todo;
    this.form = {
      title: todo.title,
      description: todo.description ?? '',
      status: todo.status,
      priority: todo.priority,
      checked: todo.checked ?? false,
      dueDate: todo.dueDate ?? '',
      reminders: todo.reminders ?? [],
      notificationEmail: todo.notificationEmail ?? '',
      level: todo.level || ''
    };
  }

  protected cancelEdit(): void {
    this.editingTodo = null;
    this.form = this.getEmptyForm();
  }

  protected deleteTodo(id: number): void {
    if (confirm('Delete this todo? This cannot be undone.')) {
      this.todoService.delete(id).subscribe({
        next: () => {
          if (this.editingTodo?.id === id) {
            this.cancelEdit();
          }
          this.refresh$.next();
        },
      });
    }
  }

  protected toggleChecked(todo: Todo): void {
    if (todo.id == null) return;
    const updatedTodo: Todo = {
      ...todo,
      checked: !todo.checked,
    };
    this.todoService.update(todo.id, updatedTodo).subscribe({
      next: (updated) => {
        todo.checked = updated.checked;
        this.refresh$.next();
      },
      error: (err) => {
        console.error('Error toggling todo:', err);
        todo.checked = !todo.checked; // revert on error
        this.refresh$.next();
      },
    });
  }

  protected assignLevel(userId: number, level: string): void {
    this.userService.setStudentLevel(userId, level).subscribe({
      next: () => {
        this.refreshStudents$.next();
      },
      error: (err) => console.error('Error assigning level:', err)
    });
  }
}


