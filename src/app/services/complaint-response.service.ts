import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ComplaintResponse } from '../models/response.model';

@Injectable({ providedIn: 'root' })
export class ComplaintResponseService {

  private apiUrl = 'http://localhost:8087/api/responses';

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

  create(complaintId: number, response: ComplaintResponse): Observable<ComplaintResponse> {
    return this.http.post<ComplaintResponse>(`${this.apiUrl}/complaint/${complaintId}`, response);
  }

  update(id: number, response: ComplaintResponse): Observable<ComplaintResponse> {
    return this.http.put<ComplaintResponse>(`${this.apiUrl}/${id}`, response);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
