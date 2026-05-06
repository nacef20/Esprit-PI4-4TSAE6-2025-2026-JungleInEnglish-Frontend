import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CvService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  uploadCV(file: File, candidateId: string, jobOfferId: number, firstName?: string | null, lastName?: string | null, email?: string | null) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('candidateId', candidateId);
    formData.append('jobOfferId', jobOfferId.toString());
    if (firstName) formData.append('firstName', firstName);
    if (lastName) formData.append('lastName', lastName);
    if (email) formData.append('email', email);
    
    return this.http.post<any>(`${this.apiUrl}/api/cvs/upload`, formData);
  }

  getMatches(cvId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/cvs/matches?cvId=${cvId}`);
  }

  getDashboardStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/api/statistiques/dashboard`);
  }

  getLatestMatchesByCandidate(candidateId: string) {
    return this.http.get<any[]>(`${this.apiUrl}/api/cvs/matches/by-candidate?candidateId=${candidateId}`);
  }

  getUploadedJobIdsByCandidate(candidateId: string): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/api/cvs/uploaded-job-ids?candidateId=${candidateId}`);
  }
}
