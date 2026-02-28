import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Attempt } from '../models/attempt.model';
import { Page } from './quiz.service';

@Injectable({ providedIn: 'root' })
export class AttemptService {

  private apiUrl = 'http://localhost:8089/api/attempts';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Attempt[]> {
    return this.http.get<Attempt[]>(this.apiUrl);
  }

  getById(id: number): Observable<Attempt> {
    return this.http.get<Attempt>(`${this.apiUrl}/${id}`);
  }

  create(attempt: Attempt): Observable<Attempt> {
    return this.http.post<Attempt>(this.apiUrl, attempt);
  }

  update(id: number, attempt: Attempt): Observable<Attempt> {
    return this.http.put<Attempt>(this.apiUrl, { ...attempt, id });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  countByQuizAndStudent(quizId: number, studentId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/count?quizId=${quizId}&studentId=${studentId}`);
  }

  getAllPaginated(page: number, size: number, quizId?: number | null, studentName?: string, quizTitle?: string): Observable<Page<Attempt>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (quizId) {
      params = params.set('quizId', quizId.toString());
    }
    if (studentName && studentName.trim()) {
      params = params.set('studentName', studentName.trim());
    }
    if (quizTitle && quizTitle.trim()) {
      params = params.set('quizTitle', quizTitle.trim());
    }
    return this.http.get<Page<Attempt>>(`${this.apiUrl}/page`, { params });
  }

  getByStudentPaginated(studentId: number, page: number, size: number): Observable<Page<Attempt>> {
    return this.http.get<Page<Attempt>>(
      `${this.apiUrl}/page/student/${studentId}?page=${page}&size=${size}`);
  }

  getStudentStats(studentId: number): Observable<{ totalAttempts: number; avgScore: number; bestScore: number; passRate: number }> {
    return this.http.get<{ totalAttempts: number; avgScore: number; bestScore: number; passRate: number }>(
      `${this.apiUrl}/stats/student/${studentId}`);
  }

  downloadPdf(attemptId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${attemptId}/pdf`, { responseType: 'blob' });
  }
}
