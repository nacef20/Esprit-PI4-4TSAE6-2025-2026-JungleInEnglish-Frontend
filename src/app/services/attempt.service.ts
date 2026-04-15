import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Attempt } from '../models/attempt.model';
import { Page } from './quiz.service';

@Injectable({ providedIn: 'root' })
export class AttemptService {

  private apiUrl = 'http://localhost:8222/api/attempts';

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

  // ── Server-side quiz attempt flow ──

  startAttempt(quizId: number, studentId: number, studentName: string): Observable<{ attemptId: number; startedAt: string; remainingSeconds: number }> {
    return this.http.post<{ attemptId: number; startedAt: string; remainingSeconds: number }>(
      `${this.apiUrl}/start?quizId=${quizId}&studentId=${studentId}&studentName=${encodeURIComponent(studentName)}`, {});
  }

  submitAttempt(attemptId: number, answers: { questionId: number; choiceId: number }[]): Observable<{ attemptId: number; score: number; totalPoints: number; percentage: number; timeExpired: boolean }> {
    return this.http.post<{ attemptId: number; score: number; totalPoints: number; percentage: number; timeExpired: boolean }>(
      `${this.apiUrl}/${attemptId}/submit`, { answers });
  }

  getRemainingTime(attemptId: number): Observable<{ remainingSeconds: number; expired: boolean }> {
    return this.http.get<{ remainingSeconds: number; expired: boolean }>(`${this.apiUrl}/${attemptId}/remaining-time`);
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

  exportListPdf(quizId?: number | null, quizTitle?: string, studentName?: string): Observable<Blob> {
    let params = new HttpParams();
    if (quizId) params = params.set('quizId', quizId.toString());
    if (quizTitle && quizTitle.trim()) params = params.set('quizTitle', quizTitle.trim());
    if (studentName && studentName.trim()) params = params.set('studentName', studentName.trim());
    return this.http.get(`${this.apiUrl}/export/pdf`, { params, responseType: 'blob' });
  }

  // ── Certificate API ──
  private certApiUrl = 'http://localhost:8222/api/certificates';

  getCertificateStatus(attemptId: number): Observable<{ eligible: boolean; exists: boolean; certificateId?: number; certificateNumber?: string; grade?: string; issuedAt?: string; viewUrl?: string }> {
    return this.http.get<any>(`${this.certApiUrl}/attempt/${attemptId}/status`);
  }

  generateCertificate(attemptId: number): Observable<{ certificateId: number; certificateNumber: string; viewUrl: string; downloadUrl: string; grade: string; issuedAt: string }> {
    return this.http.post<any>(`${this.certApiUrl}/attempt/${attemptId}/generate`, {});
  }

  getCertificateViewUrl(certificateId: number): string {
    return `${this.certApiUrl}/${certificateId}/view`;
  }

  getCertificateDownloadUrl(certificateId: number): string {
    return `${this.certApiUrl}/${certificateId}/download`;
  }

  exportListExcel(quizId?: number | null, quizTitle?: string, studentName?: string): Observable<Blob> {
    let params = new HttpParams();
    if (quizId) params = params.set('quizId', quizId.toString());
    if (quizTitle && quizTitle.trim()) params = params.set('quizTitle', quizTitle.trim());
    if (studentName && studentName.trim()) params = params.set('studentName', studentName.trim());
    return this.http.get(`${this.apiUrl}/export/excel`, { params, responseType: 'blob' });
  }
}
