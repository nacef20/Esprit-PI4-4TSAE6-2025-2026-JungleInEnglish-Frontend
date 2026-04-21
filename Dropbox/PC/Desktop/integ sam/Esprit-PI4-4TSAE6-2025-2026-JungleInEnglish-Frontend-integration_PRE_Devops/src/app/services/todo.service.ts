import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Todo, Reminder, TodoStats, AnalyticsStudent, AnalyticsTeacher, Level } from '../models/todo.model';

@Injectable({
  providedIn: 'root'
})
export class TodoService {
  private baseUrl = 'http://localhost:8222/api';

  constructor(private http: HttpClient) {}

  // --- Todo API ---
  getTodos(): Observable<Todo[]> {
    return this.http.get<Todo[]>(`${this.baseUrl}/todos`);
  }

  getTodoById(id: number): Observable<Todo> {
    return this.http.get<Todo>(`${this.baseUrl}/todos/${id}`);
  }

  filterTodos(level: string, userId: string): Observable<Todo[]> {
    const params = new HttpParams().set('level', level).set('userId', userId);
    return this.http.get<Todo[]>(`${this.baseUrl}/todos/filter`, { params });
  }

  getTodosStats(): Observable<TodoStats> {
    return this.http.get<TodoStats>(`${this.baseUrl}/todos/stats`);
  }

  createTodo(todo: Todo): Observable<Todo> {
    return this.http.post<Todo>(`${this.baseUrl}/todos/add`, todo);
  }

  createLevelTodo(level: string, todo: Todo): Observable<string> {
    return this.http.post(`${this.baseUrl}/todos/add-level?level=${level}`, todo, { responseType: 'text' });
  }

  updateTodo(id: number, todo: Todo): Observable<Todo> {
    return this.http.put<Todo>(`${this.baseUrl}/todos/${id}`, todo);
  }

  deleteTodo(id: number): Observable<void> {
    const params = new HttpParams().set('id', id.toString());
    return this.http.delete<void>(`${this.baseUrl}/todos/delete`, { params });
  }

  // --- Reminder API ---
  getReminders(): Observable<Reminder[]> {
    return this.http.get<Reminder[]>(`${this.baseUrl}/reminders`);
  }

  getRemindersByTodoId(todoId: number): Observable<Reminder[]> {
    return this.http.get<Reminder[]>(`${this.baseUrl}/reminders/todo/${todoId}`);
  }

  addReminder(reminder: any, todoId: number): Observable<Reminder> {
    const params = new HttpParams().set('todoId', todoId.toString());
    return this.http.post<Reminder>(`${this.baseUrl}/reminders/add`, reminder, { params });
  }

  updateReminder(id: number, reminder: Reminder): Observable<Reminder> {
    const params = new HttpParams().set('id', id.toString());
    return this.http.put<Reminder>(`${this.baseUrl}/reminders/update`, reminder, { params });
  }

  deleteReminder(id: number): Observable<void> {
    const params = new HttpParams().set('id', id.toString());
    return this.http.delete<void>(`${this.baseUrl}/reminders/delete`, { params });
  }

  // --- Analytics API ---
  getStudentAnalytics(): Observable<AnalyticsStudent> {
    return this.http.get<AnalyticsStudent>(`${this.baseUrl}/analytics/student`);
  }

  getTeacherAnalytics(): Observable<AnalyticsTeacher> {
    return this.http.get<AnalyticsTeacher>(`${this.baseUrl}/analytics/teacher`);
  }

  // --- User Level API ---
  updateUserLevel(userId: string, level: Level): Observable<any> {
    return this.http.put(`${this.baseUrl}/todo-users/${userId}/level`, { level });
  }

  getAllStudents(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/todo-users/students`);
  }

  syncStudents(): Observable<any> {
    return this.http.post(`${this.baseUrl}/todo-users/sync-all`, {});
  }
}
