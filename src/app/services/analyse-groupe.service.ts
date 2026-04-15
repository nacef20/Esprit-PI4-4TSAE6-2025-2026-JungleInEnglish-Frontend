import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '@env/environment';
import { 
  ImpactTransfertDTO, 
  GroupeDestinationDTO, 
  ScoreSanteGroupeDTO, 
  RecommandationGroupeDTO,
  ChargePlanningGroupeDTO,
  FusionGroupeRequest,
  FusionResultatDTO,
  ScissionGroupeRequest,
  ScissionResultatDTO,
  DashboardStatsDTO
} from '../models/analysis.model';

@Injectable({
  providedIn: 'root'
})
export class AnalyseGroupeService {
  private apiUrl = `${environment.apiBaseUrl}/pi/analyse`;
  private fusionApiUrl = `${environment.apiBaseUrl}/pi/fusion`;
  private scissionApiUrl = `${environment.apiBaseUrl}/pi/scission`;

  constructor(private http: HttpClient) {}

  getGlobalStats(): Observable<DashboardStatsDTO> {
    return this.http.get<DashboardStatsDTO>(`${this.apiUrl}/stats/global`)
      .pipe(catchError(this.handleError));
  }

  getImpactTransferts(dateDebut: string, dateFin: string, initiePar?: string): Observable<ImpactTransfertDTO[]> {
    let params = new HttpParams()
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);
    
    if (initiePar) {
      params = params.set('initiePar', initiePar);
    }

    return this.http.get<ImpactTransfertDTO[]>(`${this.apiUrl}/transferts/impact`, { params })
      .pipe(catchError(this.handleError));
  }

  getMeilleursGroupesDestination(dateDebut: string, dateFin: string): Observable<GroupeDestinationDTO[]> {
    const params = new HttpParams()
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);

    return this.http.get<GroupeDestinationDTO[]>(`${this.apiUrl}/transferts/meilleurs-groupes-destination`, { params })
      .pipe(catchError(this.handleError));
  }

  getScoresSante(joursReference: number = 30): Observable<ScoreSanteGroupeDTO[]> {
    const params = new HttpParams().set('joursReference', joursReference.toString());
    return this.http.get<ScoreSanteGroupeDTO[]>(`${this.apiUrl}/sante/scores`, { params })
      .pipe(catchError(this.handleError));
  }

  getGroupesParNiveauSante(niveauSante: string, joursReference: number = 30): Observable<ScoreSanteGroupeDTO[]> {
    const params = new HttpParams().set('joursReference', joursReference.toString());
    return this.http.get<ScoreSanteGroupeDTO[]>(`${this.apiUrl}/sante/niveau/${niveauSante}`, { params })
      .pipe(catchError(this.handleError));
  }

  getToutesRecommandations(): Observable<RecommandationGroupeDTO[]> {
    return this.http.get<RecommandationGroupeDTO[]>(`${this.apiUrl}/recommandations`)
      .pipe(catchError(this.handleError));
  }

  getRecommandationsFusion(): Observable<RecommandationGroupeDTO[]> {
    return this.http.get<RecommandationGroupeDTO[]>(`${this.apiUrl}/recommandations/fusion`)
      .pipe(catchError(this.handleError));
  }

  getRecommandationsScission(): Observable<RecommandationGroupeDTO[]> {
    return this.http.get<RecommandationGroupeDTO[]>(`${this.apiUrl}/recommandations/scission`)
      .pipe(catchError(this.handleError));
  }

  getChargePlanningGroupe(groupeId: number, date: string): Observable<ChargePlanningGroupeDTO> {
    const params = new HttpParams().set('date', date);
    return this.http.get<ChargePlanningGroupeDTO>(`${this.apiUrl}/groupes/${groupeId}/planning-charge`, { params })
      .pipe(catchError(this.handleError));
  }

  validerFusion(request: FusionGroupeRequest): Observable<FusionResultatDTO> {
    return this.http.post<FusionResultatDTO>(`${this.fusionApiUrl}/valider`, request)
      .pipe(catchError(this.handleError));
  }

  executerFusion(request: FusionGroupeRequest): Observable<FusionResultatDTO> {
    return this.http.post<FusionResultatDTO>(`${this.fusionApiUrl}/executer`, request)
      .pipe(catchError(this.handleError));
  }

  validerScission(request: ScissionGroupeRequest): Observable<ScissionResultatDTO> {
    return this.http.post<ScissionResultatDTO>(`${this.scissionApiUrl}/valider`, request)
      .pipe(catchError(this.handleError));
  }

  executerScission(request: ScissionGroupeRequest): Observable<ScissionResultatDTO> {
    return this.http.post<ScissionResultatDTO>(`${this.scissionApiUrl}/executer`, request)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('AnalyseGroupeService error:', error);
    // Backend returns { error: "...", message: "..." } on 400/404/500
    const backendMessage = error?.error?.message;
    const backendError = error?.error?.error;
    const message = backendMessage
      ? (backendError ? `${backendError}: ${backendMessage}` : backendMessage)
      : (error?.message || 'An error occurred during the analysis');
    return throwError(() => new Error(message));
  }
}
