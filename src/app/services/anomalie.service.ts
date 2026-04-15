import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '@env/environment';
import { AlerteGroupeDTO, ResumeAnomalies } from '../models/anomalie.model';

@Injectable({
  providedIn: 'root'
})
export class AnomalieService {
  private apiUrl = `${environment.apiBaseUrl}/pi/alertes`;

  constructor(private http: HttpClient) {}

  scanComplet(): Observable<AlerteGroupeDTO[]> {
    return this.http.get<AlerteGroupeDTO[]>(`${this.apiUrl}/scan`)
      .pipe(catchError(this.handleError));
  }

  getResume(): Observable<ResumeAnomalies> {
    return this.http.get<ResumeAnomalies>(`${this.apiUrl}/scan/resume`)
      .pipe(catchError(this.handleError));
  }

  getParType(type: string): Observable<AlerteGroupeDTO[]> {
    return this.http.get<AlerteGroupeDTO[]>(`${this.apiUrl}/type/${type}`)
      .pipe(catchError(this.handleError));
  }

  getParSeverite(severite: string): Observable<AlerteGroupeDTO[]> {
    return this.http.get<AlerteGroupeDTO[]>(`${this.apiUrl}/severite/${severite}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('AnomalieService error:', error);
    const message = error?.error?.message || error?.message || 'Error while scanning anomalies';
    return throwError(() => new Error(message));
  }
}
