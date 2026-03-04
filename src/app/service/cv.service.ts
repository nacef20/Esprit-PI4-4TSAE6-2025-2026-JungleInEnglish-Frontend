import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CvService {
  private apiUrl = 'http://localhost:8880/api/cvs';

  constructor(private http: HttpClient) { }

  uploadCV(file: File, candidateId: number, jobOfferId: number) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('candidateId', candidateId.toString());
    formData.append('jobOfferId', jobOfferId.toString());
    return this.http.post<any>(`${this.apiUrl}/upload`, formData);
  }

  getMatches(cvId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/matches?cvId=${cvId}`);
  }

  getDashboardStats(): Observable<any> {
    return this.http.get<any>('http://localhost:8880/api/statistiques/dashboard');
  }

  getLatestMatchesByCandidate(candidateId: number) {
    return this.http.get<any[]>(`${this.apiUrl}/matches/by-candidate?candidateId=${candidateId}`);
  }

  getUploadedJobIdsByCandidate(candidateId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/uploaded-job-ids?candidateId=${candidateId}`);
  }
}
