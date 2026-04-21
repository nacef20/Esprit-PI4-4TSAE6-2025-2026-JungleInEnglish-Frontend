import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Question } from '../models/question.model';

@Injectable({ providedIn: 'root' })
export class QuestionService {

  private apiUrl = 'http://localhost:8222/api/questions';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Question[]> {
    return this.http.get<Question[]>(this.apiUrl);
  }

  getByQuizId(quizId: number): Observable<Question[]> {
    return this.http.get<Question[]>(`${this.apiUrl}/quiz/${quizId}`);
  }

  getById(id: number): Observable<Question> {
    return this.http.get<Question>(`${this.apiUrl}/${id}`);
  }

  create(question: Question): Observable<Question> {
    return this.http.post<Question>(this.apiUrl, question);
  }

  update(id: number, question: Question): Observable<Question> {
    return this.http.put<Question>(this.apiUrl, { ...question, id });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
