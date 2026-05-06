import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Complaint, ComplaintStatus } from '../models/complaint.model';
import { ComplaintAiAssistRequest, ComplaintAiAssistResponse } from '../models/complaint-ai-assist.model';
import { ComplaintDashboardStats, ComplaintStatsPeriod } from '../models/complaint-dashboard-stats.model';
import { ComplaintSentimentRequest, ComplaintSentimentResponse } from '../models/complaint-sentiment.model';
import { Page } from './quiz.service';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ComplaintService {

  private apiUrl = `${environment.apiBaseUrl}/api/complaints`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Complaint[]> {
    return this.http.get<Complaint[]>(this.apiUrl);
  }

  getById(id: number): Observable<Complaint> {
    return this.http.get<Complaint>(`${this.apiUrl}/${id}`);
  }

  create(complaint: Complaint, attachment?: File | null): Observable<Complaint> {
    if (attachment) {
      return this.http.post<Complaint>(this.apiUrl, this.buildComplaintFormData(complaint, attachment));
    }

    return this.http.post<Complaint>(this.apiUrl, complaint);
  }

  update(id: number, complaint: Complaint, attachment?: File | null, removeAttachment = false): Observable<Complaint> {
    if (attachment || removeAttachment) {
      const formData = this.buildComplaintFormData(complaint, attachment ?? undefined);
      return this.http.put<Complaint>(`${this.apiUrl}/${id}?removeAttachment=${removeAttachment}`, formData);
    }

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

  getPaged(page: number, size: number, status?: ComplaintStatus | '', title?: string): Observable<Page<Complaint>> {
    let url = `${this.apiUrl}/page?page=${page}&size=${size}`;
    if (status) {
      url += `&status=${status}`;
    }
    const safeTitle = (title ?? '').trim();
    if (safeTitle) {
      url += `&title=${encodeURIComponent(safeTitle)}`;
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

  downloadAttachment(id: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.apiUrl}/${id}/attachment`, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  downloadAdminReportPdf(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/stats/admin-report/pdf`, { responseType: 'blob' });
  }

  downloadAdminReportExcel(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/stats/admin-report/excel`, { responseType: 'blob' });
  }

  private buildComplaintFormData(complaint: Complaint, attachment?: File): FormData {
    const formData = new FormData();
    formData.append('complaint', new Blob([JSON.stringify(complaint)], { type: 'application/json' }));

    if (attachment) {
      formData.append('attachment', attachment);
    }

    return formData;
  }
}
