import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '@env/environment';
import { ReglementGroupe, CategorieReglement } from '../models/reglement-groupe.model';

@Injectable({
  providedIn: 'root'
})
export class ReglementGroupeService {

  private apiUrl = `${environment.apiBaseUrl}/pi/reglements`;

  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  constructor(private http: HttpClient) {}

  getAllReglements(): Observable<ReglementGroupe[]> {
    return this.http.get<ReglementGroupe[]>(`${this.apiUrl}/all`)
      .pipe(catchError(this.handleError));
  }

  getReglementById(id: number): Observable<ReglementGroupe> {
    return this.http.get<ReglementGroupe>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  addReglement(reglement: ReglementGroupe): Observable<ReglementGroupe> {
    return this.http.post<ReglementGroupe>(`${this.apiUrl}/add`, reglement, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  updateReglement(reglement: ReglementGroupe): Observable<ReglementGroupe> {
    return this.http.put<ReglementGroupe>(`${this.apiUrl}/update`, reglement, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  deleteReglement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete/${id}`)
      .pipe(catchError(this.handleError));
  }

  addReglementAndAssignToGroupe(reglement: ReglementGroupe, groupeId: number): Observable<ReglementGroupe> {
    return this.http.post<ReglementGroupe>(`${this.apiUrl}/add-and-assign/${groupeId}`, reglement, this.httpOptions)
      .pipe(catchError(this.handleError));
  }

  getReglementsByGroupe(groupeId: number): Observable<ReglementGroupe[]> {
    return this.http.get<ReglementGroupe[]>(`${this.apiUrl}/groupe/${groupeId}`)
      .pipe(catchError(this.handleError));
  }

  getReglementsByCategorie(categorie: CategorieReglement): Observable<ReglementGroupe[]> {
    return this.http.get<ReglementGroupe[]>(`${this.apiUrl}/categorie/${categorie}`)
      .pipe(catchError(this.handleError));
  }

  getReglementsActifs(): Observable<ReglementGroupe[]> {
    return this.http.get<ReglementGroupe[]>(`${this.apiUrl}/actifs`)
      .pipe(catchError(this.handleError));
  }

  activerReglement(id: number): Observable<ReglementGroupe> {
    return this.http.put<ReglementGroupe>(`${this.apiUrl}/activer/${id}`, {})
      .pipe(catchError(this.handleError));
  }

  desactiverReglement(id: number): Observable<ReglementGroupe> {
    return this.http.put<ReglementGroupe>(`${this.apiUrl}/desactiver/${id}`, {})
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('ReglementGroupeService error:', error);
    const message = error?.error?.message || error?.message || 'An error occurred';
    return throwError(() => new Error(message));
  }
}
