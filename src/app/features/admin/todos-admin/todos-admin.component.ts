import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AsyncPipe, LowerCasePipe } from '@angular/common';
import { BehaviorSubject, switchMap } from 'rxjs';
import { TodoService } from '../../todos/services/todo.service';
import type { Todo, TodoStatus, Priority } from '../../todos/models/todo.model';

@Component({
  selector: 'app-todos-admin',
  standalone: true,
  imports: [FormsModule, AsyncPipe, LowerCasePipe],
  template: `
    <div class="page">
      <header class="page-header">
        <h1>Todos backoffice</h1>
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
            <textarea id="description" [(ngModel)]="form.description" name="description" placeholder="Description" rows="2"></textarea>
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
          </div>
          <div class="form-row checkbox-row">
            <input id="ischecked" [(ngModel)]="form.ischecked" type="checkbox" name="ischecked" />
            <label for="ischecked">Checked</label>
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
                    <th>Title</th>
                    <th>Status</th>
                    <th>Priority</th>
                    <th>Checked</th>
                    <th class="col-actions">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  @for (todo of todos; track todo.id) {
                    <tr>
                      <td><strong>{{ todo.title }}</strong></td>
                      <td><span class="badge badge-{{ todo.status | lowercase }}">{{ todo.status }}</span></td>
                      <td><span class="badge badge-priority-{{ todo.priority | lowercase }}">{{ todo.priority }}</span></td>
                      <td>{{ todo.ischecked ? 'Yes' : 'No' }}</td>
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
      margin-bottom: 1.5rem;
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

    .empty {
      margin: 0;
      color: #64748b;
      font-size: 0.95rem;
    }
  `,
})
export class TodosAdminComponent implements OnInit {
  private readonly todoService = inject(TodoService);
  private readonly refresh$ = new BehaviorSubject<void>(undefined);
  protected readonly todos$ = this.refresh$.pipe(
    switchMap(() => this.todoService.getAll())
  );

  protected form: Partial<Todo> = this.getEmptyForm();
  protected editingTodo: Todo | null = null;

  ngOnInit(): void {}

  private getEmptyForm(): Partial<Todo> {
    return {
      title: '',
      description: '',
      status: 'PENDING',
      priority: 'MEDIUM',
      ischecked: false,
    };
  }

  protected saveTodo(): void {
    if (!this.form.title) return;
    if (this.editingTodo?.id != null) {
      const payload: Todo = {
        id: this.editingTodo.id,
        title: this.form.title,
        description: this.form.description,
        status: (this.form.status as TodoStatus) ?? 'PENDING',
        priority: (this.form.priority as Priority) ?? 'MEDIUM',
        ischecked: this.form.ischecked ?? false,
      };
      this.todoService.update(this.editingTodo.id, payload).subscribe({
        next: () => {
          this.editingTodo = null;
          this.form = this.getEmptyForm();
          this.refresh$.next();
        },
      });
    } else {
      this.todoService.create(this.form as Todo).subscribe({
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
      ischecked: todo.ischecked ?? false,
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
}
