import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AttemptAnswer } from '../models/attempt-answer.model';

@Injectable({ providedIn: 'root' })
export class AttemptAnswerService {

  private apiUrl = 'http://localhost:8222/api/attempt-answers';

  constructor(private http: HttpClient) {}

  saveAll(answers: AttemptAnswer[]): Observable<AttemptAnswer[]> {
    return this.http.post<AttemptAnswer[]>(this.apiUrl, answers);
  }

  getByAttemptId(attemptId: number): Observable<AttemptAnswer[]> {
    return this.http.get<AttemptAnswer[]>(`${this.apiUrl}/attempt/${attemptId}`);
  }
}
