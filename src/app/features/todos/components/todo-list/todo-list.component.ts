import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AsyncPipe } from '@angular/common';
import { TodoService } from '../../services/todo.service';

@Component({
  selector: 'app-todo-list',
  standalone: true,
  imports: [RouterLink, AsyncPipe],
  template: `
    <h1>Todos</h1>
    @if (todos$ | async; as todos) {
      <ul class="todo-list">
        @for (todo of todos; track todo.id) {
          <li>
            <a [routerLink]="['/todos', todo.id]">{{ todo.title }}</a>
            <span class="badge">{{ todo.status }}</span>
          </li>
        }
      </ul>
      @if (todos.length === 0) {
        <p>No todos yet.</p>
      }
    } @else {
      <p>Loading...</p>
    }
  `,
  styles: `
    .todo-list {
      list-style: none;
      padding: 0;
    }
    .todo-list li {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 0;
      border-bottom: 1px solid #eee;
    }
    .badge {
      font-size: 0.75rem;
      padding: 0.25rem 0.5rem;
      background: #e0e0e0;
      border-radius: 4px;
    }
  `,
})
export class TodoListComponent implements OnInit {
  private readonly todoService = inject(TodoService);
  protected readonly todos$ = this.todoService.getAll();

  ngOnInit(): void {}
}
