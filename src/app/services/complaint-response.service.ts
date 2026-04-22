import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ComplaintResponse } from '../models/response.model';
import { ResponseAiAssistRequest, ResponseAiAssistResponse } from '../models/response-ai-assist.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ComplaintResponseService {

  private apiUrl = `${environment.apiBaseUrl}/api/responses`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ComplaintResponse[]> {
    return this.http.get<ComplaintResponse[]>(this.apiUrl);
  }

  getById(id: number): Observable<ComplaintResponse> {
    return this.http.get<ComplaintResponse>(`${this.apiUrl}/${id}`);
  }

  getByComplaintId(complaintId: number): Observable<ComplaintResponse[]> {
    return this.http.get<ComplaintResponse[]>(`${this.apiUrl}/complaint/${complaintId}`);
  }

  create(complaintId: number, response: ComplaintResponse, attachment?: File | null): Observable<ComplaintResponse> {
    if (attachment) {
      return this.http.post<ComplaintResponse>(
        `${this.apiUrl}/complaint/${complaintId}`,
        this.buildResponseFormData(response, attachment)
      );
    }

    return this.http.post<ComplaintResponse>(`${this.apiUrl}/complaint/${complaintId}`, response);
  }

  update(id: number, response: ComplaintResponse, attachment?: File | null, removeAttachment = false): Observable<ComplaintResponse> {
    if (attachment || removeAttachment) {
      const formData = this.buildResponseFormData(response, attachment ?? undefined);
      return this.http.put<ComplaintResponse>(`${this.apiUrl}/${id}?removeAttachment=${removeAttachment}`, formData);
    }

    return this.http.put<ComplaintResponse>(`${this.apiUrl}/${id}`, response);
  }

  assistTextWithAi(payload: ResponseAiAssistRequest): Observable<ResponseAiAssistResponse> {
    return this.http.post<ResponseAiAssistResponse>(`${this.apiUrl}/ai/assist`, payload);
  }

  downloadAttachment(id: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.apiUrl}/${id}/attachment`, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  private buildResponseFormData(response: ComplaintResponse, attachment?: File): FormData {
    const formData = new FormData();
    formData.append('response', new Blob([JSON.stringify(response)], { type: 'application/json' }));

    if (attachment) {
      formData.append('attachment', attachment);
    }

    return formData;
  }
}
