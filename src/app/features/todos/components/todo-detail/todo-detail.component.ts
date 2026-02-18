import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TodoService } from '../../services/todo.service';
import type { Todo } from '../../models/todo.model';

@Component({
  selector: 'app-todo-detail',
  standalone: true,
  imports: [RouterLink],
  template: `
    @if (todo()) {
      <h1>{{ todo()!.title }}</h1>
      <p>{{ todo()!.description }}</p>
      <p><strong>Status:</strong> {{ todo()!.status }} | <strong>Priority:</strong> {{ todo()!.priority }}</p>
      <a routerLink="/todos">← Back to list</a>
    } @else if (loading()) {
      <p>Loading...</p>
    } @else {
      <p>Todo not found.</p>
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
