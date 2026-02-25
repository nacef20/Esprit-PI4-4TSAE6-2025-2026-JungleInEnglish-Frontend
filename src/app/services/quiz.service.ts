import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Quiz } from '../models/quiz.model';

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
}
