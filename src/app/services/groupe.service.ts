import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Groupe, GroupeEnrichi, StatutGroupe, TypeGroupe } from '../models/groupe.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class GroupeService {

  private apiUrl = `${environment.apiBaseUrl}/pi/groupes`;
  private advancedApiUrl = `${environment.apiBaseUrl}/pi`;

  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  constructor(private http: HttpClient) {}

  getAllGroupes(): Observable<Groupe[]> {
    return this.http.get<Groupe[]>(`${this.apiUrl}/all`)
      .pipe(catchError(this.handleError));
  }

  getGroupeById(id: number): Observable<Groupe> {
    return this.http.get<Groupe>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  getGroupeEnrichi(id: number): Observable<GroupeEnrichi> {
    return this.http.get<GroupeEnrichi>(`${this.apiUrl}/${id}/enrichi`)
      .pipe(catchError(this.handleError));
  }

  createGroupe(groupe: Groupe): Observable<Groupe> {
    return this.http.post<Groupe>(`${this.apiUrl}/add`, groupe, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  updateGroupe(groupe: Groupe): Observable<Groupe> {
    return this.http.put<Groupe>(`${this.apiUrl}/update`, groupe, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  deleteGroupe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`)
      .pipe(catchError(this.handleError));
  }

  getGroupesByStatut(statut: StatutGroupe): Observable<Groupe[]> {
    return this.http.get<Groupe[]>(`${this.apiUrl}/statut/${statut}`)
      .pipe(catchError(this.handleError));
  }

  getGroupesByType(type: TypeGroupe): Observable<Groupe[]> {
    return this.http.get<Groupe[]>(`${this.apiUrl}/type/${type}`)
      .pipe(catchError(this.handleError));
  }

  getGroupesDisponibles(): Observable<Groupe[]> {
    return this.http.get<Groupe[]>(`${this.apiUrl}/disponibles`)
      .pipe(catchError(this.handleError));
  }

  searchGroupesByNom(nom: string): Observable<Groupe[]> {
    return this.http.get<Groupe[]>(`${this.apiUrl}/search/${nom}`)
      .pipe(catchError(this.handleError));
  }

  updateStatutGroupe(id: number, statut: StatutGroupe): Observable<Groupe> {
    return this.http.put<Groupe>(`${this.apiUrl}/update-statut/${id}/${statut}`, {})
      .pipe(catchError(this.handleError));
  }

  requestAdvanced<T>(method: 'GET' | 'POST' | 'PUT' | 'DELETE', path: string, body?: unknown, params?: Record<string, unknown>): Observable<T> {
    const httpParams = this.buildHttpParams(params);
    const url = `${this.advancedApiUrl}${path}`;

    return this.http.request<T>(method, url, {
      body,
      params: httpParams
    }).pipe(catchError(this.handleError));
  }

  private buildHttpParams(params?: Record<string, unknown>): HttpParams | undefined {
    if (!params) {
      return undefined;
    }

    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value === undefined || value === null || value === '') {
        return;
      }

      httpParams = httpParams.set(key, String(value));
    });

    return httpParams;
  }

  private handleError(error: any): Observable<never> {
    console.error('GroupeService error:', error);
    const message = error?.error?.message || error?.message || 'An error occurred';
    return throwError(() => new Error(message));
  }
}
