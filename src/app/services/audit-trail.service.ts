import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '@env/environment';
import { ParcoursEtudiantDTO, RetentionGroupeDTO } from '../models/audit.model';

@Injectable({
  providedIn: 'root'
})
export class AuditTrailService {
  private apiUrl = `${environment.apiBaseUrl}/pi/audit`;

  constructor(private http: HttpClient) {}

  getParcoursEtudiant(etudiantId: number): Observable<ParcoursEtudiantDTO[]> {
    return this.http.get<ParcoursEtudiantDTO[]>(`${this.apiUrl}/parcours/${etudiantId}`)
      .pipe(catchError(this.handleError));
  }

  getStatistiquesRetention(seuil: number = 1): Observable<RetentionGroupeDTO[]> {
    const params = new HttpParams().set('seuil', seuil.toString());
    return this.http.get<RetentionGroupeDTO[]>(`${this.apiUrl}/retention`, { params })
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('AuditTrailService error:', error);
    const message = error?.error?.message || error?.message || 'Error while running the audit';
    return throwError(() => new Error(message));
  }
}
