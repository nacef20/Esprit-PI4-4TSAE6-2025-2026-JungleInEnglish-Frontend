import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '@env/environment';
import { PlanEquilibrageDTO } from '../models/equilibrage.model';

@Injectable({
  providedIn: 'root'
})
export class EquilibrageService {
  private apiUrl = `${environment.apiBaseUrl}/pi/equilibrage`;

  constructor(private http: HttpClient) {}

  getNiveauxEligibles(): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/niveaux`)
      .pipe(catchError(this.handleError));
  }

  genererPlan(niveauId: number, seuil: number = 2): Observable<PlanEquilibrageDTO> {
    const params = new HttpParams()
      .set('niveauId', niveauId.toString())
      .set('seuil', seuil.toString());
    return this.http.get<PlanEquilibrageDTO>(`${this.apiUrl}/plan`, { params })
      .pipe(catchError(this.handleError));
  }

  executerPlan(niveauId: number, seuil: number = 2): Observable<PlanEquilibrageDTO> {
    const params = new HttpParams()
      .set('niveauId', niveauId.toString())
      .set('seuil', seuil.toString());
    return this.http.post<PlanEquilibrageDTO>(`${this.apiUrl}/executer`, null, { params })
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('EquilibrageService error:', error);
    const message = error?.error?.message || error?.message || 'Error while balancing groups';
    return throwError(() => new Error(message));
  }
}
