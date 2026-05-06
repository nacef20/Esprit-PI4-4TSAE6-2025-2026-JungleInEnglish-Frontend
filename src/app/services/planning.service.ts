import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Planning, PlanningGeneratorRequest, GenerationResult, WeeklyLoadReport, SemesterReplicationRequest, SemesterReplicationResult } from '../models/planning.model';

@Injectable({
    providedIn: 'root'
})
export class PlanningService {
        // Fallback API base (à adapter si besoin)
        private baseApiUrl = 'http://localhost:8222';

        /**
         * Récupérer toutes les salles
         */
        getAllSalles(): Observable<any[]> {
            return this.http.get<any[]>(`${this.baseApiUrl}/salles`)
                .pipe(catchError(this.handleError));
        }

        /**
         * Récupérer tous les créneaux horaires
         */
        getAllCreneaux(): Observable<any[]> {
            return this.http.get<any[]>(`${this.baseApiUrl}/creneaux`)
                .pipe(catchError(this.handleError));
        }
    private apiUrl = 'http://localhost:8222/plannings'; // URL corrigée pour correspondre au backend

    private httpOptions = {
        headers: new HttpHeaders({
            'Content-Type': 'application/json'
        })
    };

    constructor(private http: HttpClient) { }

    /**
     * Récupérer tous les plannings
     */
    getAllPlannings(): Observable<Planning[]> {
        const url = `${this.apiUrl}`;
        return this.http.get<Planning[]>(url)
            .pipe(
                catchError(this.handleError)
            );
    }

    /**
     * Récupérer un planning par ID
     */
    getPlanningById(id: number): Observable<Planning> {
        const url = `${this.apiUrl}/${id}`;
        return this.http.get<Planning>(url)
            .pipe(
                catchError(this.handleError)
            );
    }

    /**
     * Créer un nouveau planning
     */
    createPlanning(planning: Planning): Observable<Planning> {
        const url = `${this.apiUrl}`;
        return this.http.post<Planning>(url, planning, this.httpOptions)
            .pipe(
                catchError(this.handleError)
            );
    }

    /**
     * Mettre à jour un planning existant
     */
    updatePlanning(id: number, planning: Planning): Observable<Planning> {
        const url = `${this.apiUrl}/${id}`;
        return this.http.put<Planning>(url, planning, this.httpOptions)
            .pipe(
                catchError(this.handleError)
            );
    }

    /**
     * Supprimer un planning
     */
    deletePlanning(id: number): Observable<void> {
        const url = `${this.apiUrl}/${id}`;
        return this.http.delete<void>(url)
            .pipe(
                catchError(this.handleError)
            );
    }

    /**
     * Rechercher des plannings par critères
     */
    searchPlannings(searchTerm: string): Observable<Planning[]> {
        const url = `${this.apiUrl}/search?q=${searchTerm}`;
        return this.http.get<Planning[]>(url)
            .pipe(
                catchError(this.handleError)
            );
    }

    // =====================================================================
    // ADVANCED PLANNING METHODS
    // =====================================================================

    /**
     * POST /plannings/advanced/generate
     * Génère automatiquement un planning pour la semaine donnée.
     */
    generateWeeklyPlanning(request: PlanningGeneratorRequest): Observable<GenerationResult> {
        const url = `${this.baseApiUrl}/plannings/advanced/generate`;
        return this.http.post<GenerationResult>(url, request, this.httpOptions)
            .pipe(catchError(this.handleError));
    }

    /**
     * GET /plannings/advanced/weekly-load?date=YYYY-MM-DD
     * Analyse la charge de la semaine contenant la date fournie.
     */
    analyzeWeeklyLoad(date: string): Observable<WeeklyLoadReport> {
        const url = `${this.baseApiUrl}/plannings/advanced/weekly-load?date=${date}`;
        return this.http.get<WeeklyLoadReport>(url)
            .pipe(catchError(this.handleError));
    }

    /**
     * POST /plannings/advanced/replicate-semester
     * Réplique le planning d'une semaine de référence sur tout le semestre.
     */
    replicatePlanningSemester(request: SemesterReplicationRequest): Observable<SemesterReplicationResult> {
        const url = `${this.baseApiUrl}/plannings/advanced/replicate-semester`;
        return this.http.post<SemesterReplicationResult>(url, request, this.httpOptions)
            .pipe(catchError(this.handleError));
    }

    /**
     * Gestion des erreurs
     */
    private handleError(error: any) {
        console.error('Status:', error.status);
        console.error('Body:', error.error);
        const message = error.error?.message || error.error || error.message || 'Erreur inconnue';
        return throwError(() => new Error(message));
    }
    
}
