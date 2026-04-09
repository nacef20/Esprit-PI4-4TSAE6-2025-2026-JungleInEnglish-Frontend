import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Complaint, ComplaintStatus } from '../models/complaint.model';
import { ComplaintAiAssistRequest, ComplaintAiAssistResponse } from '../models/complaint-ai-assist.model';
import { ComplaintDashboardStats, ComplaintStatsPeriod } from '../models/complaint-dashboard-stats.model';
import { ComplaintSentimentRequest, ComplaintSentimentResponse } from '../models/complaint-sentiment.model';
import { Page } from './quiz.service';

@Injectable({ providedIn: 'root' })
export class ComplaintService {

  private apiUrl = 'http://localhost:8087/api/complaints';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Complaint[]> {
    return this.http.get<Complaint[]>(this.apiUrl);
  }

  getById(id: number): Observable<Complaint> {
    return this.http.get<Complaint>(`${this.apiUrl}/${id}`);
  }

  create(complaint: Complaint): Observable<Complaint> {
    return this.http.post<Complaint>(this.apiUrl, complaint);
  }

  update(id: number, complaint: Complaint): Observable<Complaint> {
    return this.http.put<Complaint>(`${this.apiUrl}/${id}`, complaint);
  }

  reject(id: number): Observable<Complaint> {
    return this.http.put<Complaint>(`${this.apiUrl}/${id}/reject`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getByStatus(status: ComplaintStatus): Observable<Complaint[]> {
    return this.http.get<Complaint[]>(`${this.apiUrl}/status/${status}`);
  }

  getPaged(page: number, size: number, status?: ComplaintStatus | ''): Observable<Page<Complaint>> {
    let url = `${this.apiUrl}/page?page=${page}&size=${size}`;
    if (status) {
      url += `&status=${status}`;
    }
    return this.http.get<Page<Complaint>>(url);
  }

  getDashboardStats(period: ComplaintStatsPeriod = 'MONTH'): Observable<ComplaintDashboardStats> {
    return this.http.get<ComplaintDashboardStats>(`${this.apiUrl}/stats/dashboard?period=${period}`);
  }

  assistTextWithAi(payload: ComplaintAiAssistRequest): Observable<ComplaintAiAssistResponse> {
    return this.http.post<ComplaintAiAssistResponse>(`${this.apiUrl}/ai/assist`, payload);
  }

  analyzeSentiment(payload: ComplaintSentimentRequest): Observable<ComplaintSentimentResponse> {
    return this.http.post<ComplaintSentimentResponse>(`${this.apiUrl}/ai/sentiment`, payload);
  }

  downloadAdminReportPdf(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/stats/admin-report/pdf`, { responseType: 'blob' });
  }
}
