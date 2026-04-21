import { Component, OnInit } from '@angular/core';
import { TodoService } from '../../../services/todo.service';
import { Todo, TodoStatus, Priority } from '../../../models/todo.model';
import { getKeycloak } from '../../../auth/keycloak.config';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-todo-list',
  templateUrl: './todo-list.component.html',
  styleUrls: ['./todo-list.component.scss']
})
export class TodoListComponent implements OnInit {
  todos: Todo[] = [];
  loading = true;
  showForm = false;
  isTeacher = false;
  submittingLevel = false;

  levels = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2'];
  selectedLevel: string = '';

  newTodo: Todo = {
    title: '',
    description: '',
    status: TodoStatus.PENDING,
    priority: Priority.MEDIUM,
    checked: false,
    dueDate: ''
  };

  constructor(private todoService: TodoService) {}

  ngOnInit(): void {
    // Detect if the current user is a Teacher or Admin via Keycloak roles
    try {
      const kc = getKeycloak();
      const roles: string[] = kc?.realmAccess?.roles || [];
      this.isTeacher = roles.some(r => r.toUpperCase() === 'ADMIN' || r.toUpperCase() === 'TEACHER');
    } catch (e) {
      this.isTeacher = false;
    }
    this.loadTodos();
  }

  loadTodos(): void {
    this.loading = true;
    this.todoService.getTodos().subscribe({
      next: (data: Todo[]) => {
        this.todos = data;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading todos', err);
        this.loading = false;
      }
    });
  }

  toggleTodo(todo: Todo): void {
    if (!todo.id) return;
    todo.checked = !todo.checked;
    todo.status = todo.checked ? TodoStatus.COMPLETED : TodoStatus.IN_PROGRESS;

    this.todoService.updateTodo(todo.id, todo).subscribe({
      next: () => {},
      error: (err: any) => {
        todo.checked = !todo.checked; // Rollback
        console.error('Error updating todo', err);
      }
    });
  }

  deleteTodo(id: number): void {
    Swal.fire({
      title: 'Are you sure?',
      text: "You won't be able to revert this!",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#2D5757',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
      if (result.isConfirmed) {
        this.todoService.deleteTodo(id).subscribe({
          next: () => {
            this.todos = this.todos.filter(t => t.id !== id);
            Swal.fire('Deleted!', 'Your task has been deleted.', 'success');
          },
          error: (err: any) => {
            Swal.fire('Error!', 'Could not delete the task.', 'error');
          }
        });
      }
    });
  }

  onSubmit(): void {
    if (!this.newTodo.title.trim()) return;

    // If teacher/admin selected a level → bulk assignment
    if (this.isTeacher && this.selectedLevel) {
      this.submittingLevel = true;
      this.todoService.createLevelTodo(this.selectedLevel, this.newTodo).subscribe({
        next: () => {
          this.submittingLevel = false;
          this.resetForm();
          this.showForm = false;
          Swal.fire({
            icon: 'success',
            title: `Mission assigned to all Level ${this.selectedLevel} students!`,
            timer: 2000,
            showConfirmButton: false
          });
        },
        error: (err: any) => {
          this.submittingLevel = false;
          Swal.fire('Error!', 'Could not assign the mission to the level.', 'error');
        }
      });
    } else {
      // Normal personal todo creation
      this.todoService.createTodo(this.newTodo).subscribe({
        next: (created: Todo) => {
          this.todos.unshift(created);
          this.resetForm();
          this.showForm = false;
          Swal.fire({
            icon: 'success',
            title: 'Task Created',
            timer: 1500,
            showConfirmButton: false
          });
        },
        error: (err: any) => {
          Swal.fire('Error!', 'Could not create the task.', 'error');
        }
      });
    }
  }

  resetForm(): void {
    this.newTodo = {
      title: '',
      description: '',
      status: TodoStatus.PENDING,
      priority: Priority.MEDIUM,
      checked: false,
      dueDate: ''
    };
    this.selectedLevel = '';
  }

  getPriorityClass(priority: Priority): string {
    switch (priority) {
      case Priority.HIGH: return 'bg-red-100 text-red-600 border-red-200';
      case Priority.MEDIUM: return 'bg-amber-100 text-amber-600 border-amber-200';
      case Priority.LOW: return 'bg-emerald-100 text-emerald-600 border-emerald-200';
      default: return 'bg-gray-100 text-gray-600';
    }
  }

  getCompletedCount(): number {
    return this.todos.filter((t: Todo) => t.checked).length;
  }

  getPendingCount(): number {
    return this.todos.filter((t: Todo) => !t.checked).length;
  }
}
