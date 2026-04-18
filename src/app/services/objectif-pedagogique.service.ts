import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '@env/environment';
import { ObjectifPedagogique, StatutObjectif } from '../models/objectif-pedagogique.model';

@Injectable({
  providedIn: 'root'
})
export class ObjectifPedagogiqueService {

  private apiUrl = `${environment.apiBaseUrl}/pi/objectifs`;

  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  constructor(private http: HttpClient) {}

  getAllObjectifs(): Observable<ObjectifPedagogique[]> {
    return this.http.get<ObjectifPedagogique[]>(`${this.apiUrl}/all`)
      .pipe(catchError(this.handleError));
  }

  getObjectifById(id: number): Observable<ObjectifPedagogique> {
    return this.http.get<ObjectifPedagogique>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  addObjectif(objectif: ObjectifPedagogique): Observable<ObjectifPedagogique> {
    return this.http.post<ObjectifPedagogique>(`${this.apiUrl}/add`, objectif, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  updateObjectif(objectif: ObjectifPedagogique): Observable<ObjectifPedagogique> {
    return this.http.put<ObjectifPedagogique>(`${this.apiUrl}/update`, objectif, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  deleteObjectif(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`)
      .pipe(catchError(this.handleError));
  }

  addObjectifAndAssignToGroupe(objectif: ObjectifPedagogique, groupeId: number): Observable<ObjectifPedagogique> {
    return this.http.post<ObjectifPedagogique>(`${this.apiUrl}/add-and-assign/${groupeId}`, objectif, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  getObjectifsByGroupe(groupeId: number): Observable<ObjectifPedagogique[]> {
    return this.http.get<ObjectifPedagogique[]>(`${this.apiUrl}/groupe/${groupeId}`)
      .pipe(catchError(this.handleError));
  }

  updateStatutObjectif(id: number, statut: StatutObjectif): Observable<ObjectifPedagogique> {
    return this.http.put<ObjectifPedagogique>(`${this.apiUrl}/update-statut/${id}/${statut}`, {})
      .pipe(catchError(this.handleError));
  }

  updateTauxReussite(id: number, taux: number): Observable<ObjectifPedagogique> {
    return this.http.put<ObjectifPedagogique>(`${this.apiUrl}/update-taux-reussite/${id}/${taux}`, {})
      .pipe(catchError(this.handleError));
  }

  marquerAtteint(id: number): Observable<ObjectifPedagogique> {
    return this.http.put<ObjectifPedagogique>(`${this.apiUrl}/marquer-atteint/${id}`, {})
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('ObjectifPedagogiqueService error:', error);
    const message = error?.error?.message || error?.message || 'An error occurred';
    return throwError(() => new Error(message));
  }
}
