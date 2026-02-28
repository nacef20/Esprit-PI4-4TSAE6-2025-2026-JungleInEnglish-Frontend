import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Quiz } from '../models/quiz.model';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class QuizService {

  private apiUrl = 'http://localhost:8089/api/quizzes';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Quiz[]> {
    return this.http.get<Quiz[]>(this.apiUrl);
  }

  getById(id: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.apiUrl}/${id}`);
  }

  create(quiz: Quiz): Observable<Quiz> {
    return this.http.post<Quiz>(this.apiUrl, quiz);
  }

  update(id: number, quiz: Quiz): Observable<Quiz> {
    return this.http.put<Quiz>(this.apiUrl, { ...quiz, id });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getAllPaginated(page: number, size: number, status?: string, title?: string): Observable<Page<Quiz>> {
    let url = `${this.apiUrl}/page?page=${page}&size=${size}`;
    if (status) url += `&status=${status}`;
    if (title) url += `&title=${encodeURIComponent(title)}`;
    return this.http.get<Page<Quiz>>(url);
  }

  getAvailablePaginated(page: number, size: number, title?: string, filter?: string, studentId?: number): Observable<Page<Quiz>> {
    let url = `${this.apiUrl}/page/available?page=${page}&size=${size}`;
    if (title) url += `&title=${encodeURIComponent(title)}`;
    if (filter) url += `&filter=${filter}`;
    if (studentId) url += `&studentId=${studentId}`;
    return this.http.get<Page<Quiz>>(url);
  }
}
