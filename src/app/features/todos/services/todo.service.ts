import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { Todo, TodoStatus, TodoStats } from '../models/todo.model';

@Injectable({ providedIn: 'root' })
export class TodoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.todoApiUrl}/api/todos`;

  getAll(): Observable<Todo[]> {
    return this.http.get<Todo[]>(this.baseUrl);
  }

  getById(id: number): Observable<Todo> {
    return this.http.get<Todo>(`${this.baseUrl}/${id}`);
  }

  getByStatus(status: TodoStatus): Observable<Todo[]> {
    return this.http.get<Todo[]>(`${this.baseUrl}/status/${status}`);
  }

  create(todo: Todo): Observable<Todo> {
    return this.http.post<Todo>(`${this.baseUrl}/add`, todo);
  }

  /** PUT /api/todos/{id} - send full todo body including ischecked */
  update(id: number, todo: Todo): Observable<Todo> {
    return this.http.put<Todo>(`${this.baseUrl}/${id}`, todo);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete?id=${id}`);
  }

  getStats(): Observable<TodoStats> {
    return this.http.get<TodoStats>(`${this.baseUrl}/stats`);
  }
}
