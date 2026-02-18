import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { Reminder, CreateReminderRequest } from '../models/reminder.model';

@Injectable({ providedIn: 'root' })
export class ReminderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.todoApiUrl}/api/reminders`;

  getAll(): Observable<Reminder[]> {
    return this.http.get<Reminder[]>(this.baseUrl);
  }

  getById(id: number): Observable<Reminder> {
    return this.http.get<Reminder>(`${this.baseUrl}/${id}`);
  }

  getByTodoId(todoId: number): Observable<Reminder[]> {
    return this.http.get<Reminder[]>(`${this.baseUrl}/todo/${todoId}`);
  }

  create(request: CreateReminderRequest, todoId: number): Observable<Reminder> {
    return this.http.post<Reminder>(
      `${this.baseUrl}/add?todoId=${todoId}`,
      request
    );
  }

  update(id: number, reminder: Reminder): Observable<Reminder> {
    return this.http.put<Reminder>(
      `${this.baseUrl}/update?id=${id}`,
      reminder
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/delete?id=${id}`);
  }
}
