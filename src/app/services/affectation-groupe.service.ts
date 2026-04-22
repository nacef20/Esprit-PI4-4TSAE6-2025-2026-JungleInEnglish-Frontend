import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { GroupeEtudiant, GroupeFormateur } from '../models/affectation.model';
import { Groupe } from '../models/groupe.model';

@Injectable({
  providedIn: 'root'
})
export class AffectationGroupeService {
  private apiUrl = `${environment.apiBaseUrl}/pi/affectations`;

  constructor(private http: HttpClient) {}

  affecterEtudiant(etudiantId: number, groupeId: number, options?: { scoreEntree?: number; commentaire?: string; affectePar?: number }): Observable<GroupeEtudiant> {
    let params = new HttpParams();

    if (options?.scoreEntree !== undefined && options?.scoreEntree !== null) {
      params = params.set('scoreEntree', options.scoreEntree.toString());
    }

    const commentaire = this.normalizeCommentaire(options?.commentaire);
    if (commentaire) {
      params = params.set('commentaire', commentaire);
    }

    if (options?.affectePar !== undefined && options?.affectePar !== null) {
      params = params.set('affectePar', options.affectePar.toString());
    }

    return this.http.post<GroupeEtudiant>(`${this.apiUrl}/etudiants/affecter/${etudiantId}/${groupeId}`, {}, { params })
      .pipe(catchError(this.handleError));
  }

  changerGroupe(etudiantId: number, nouveauGroupeId: number, data: { raison: string; initiePar: string; scoreAvant?: number; validateurId?: number; commentaire?: string }): Observable<GroupeEtudiant> {
    let params = new HttpParams()
      .set('raison', data.raison)
      .set('initiePar', data.initiePar);

    if (data.scoreAvant !== undefined && data.scoreAvant !== null) {
      params = params.set('scoreAvant', data.scoreAvant.toString());
    }

    if (data.validateurId !== undefined && data.validateurId !== null) {
      params = params.set('validateurId', data.validateurId.toString());
    }

    const commentaire = this.normalizeCommentaire(data.commentaire);
    if (commentaire) {
      params = params.set('commentaire', commentaire);
    }

    return this.http.put<GroupeEtudiant>(`${this.apiUrl}/etudiants/changer-groupe/${etudiantId}/${nouveauGroupeId}`, {}, { params })
      .pipe(catchError(this.handleError));
  }

  desaffecterEtudiant(etudiantId: number, commentaire?: string): Observable<GroupeEtudiant> {
    let params = new HttpParams();
    const commentaireNormalise = this.normalizeCommentaire(commentaire);

    if (commentaireNormalise) {
      params = params.set('commentaire', commentaireNormalise);
    }

    return this.http.put<GroupeEtudiant>(`${this.apiUrl}/etudiants/desaffecter/${etudiantId}`, {}, { params })
      .pipe(catchError(this.handleError));
  }

  suspendreEtudiant(etudiantId: number, commentaire?: string): Observable<GroupeEtudiant> {
    let params = new HttpParams();
    const commentaireNormalise = this.normalizeCommentaire(commentaire);

    if (commentaireNormalise) {
      params = params.set('commentaire', commentaireNormalise);
    }

    return this.http.put<GroupeEtudiant>(`${this.apiUrl}/etudiants/suspendre/${etudiantId}`, {}, { params })
      .pipe(catchError(this.handleError));
  }

  reactiverEtudiant(etudiantId: number): Observable<GroupeEtudiant> {
    return this.http.put<GroupeEtudiant>(`${this.apiUrl}/etudiants/reactiver/${etudiantId}`, {})
      .pipe(catchError(this.handleError));
  }

  getEtudiantsParGroupe(groupeId: number): Observable<GroupeEtudiant[]> {
    return this.http.get<GroupeEtudiant[]>(`${this.apiUrl}/etudiants/groupe/${groupeId}`)
      .pipe(catchError(this.handleError));
  }

  getGroupeActuelEtudiant(etudiantId: number): Observable<GroupeEtudiant> {
    return this.http.get<GroupeEtudiant>(`${this.apiUrl}/etudiants/${etudiantId}/groupe-actuel`)
      .pipe(catchError(this.handleError));
  }

  getHistoriqueEtudiant(etudiantId: number): Observable<GroupeEtudiant[]> {
    return this.http.get<GroupeEtudiant[]>(`${this.apiUrl}/etudiants/${etudiantId}/historique`)
      .pipe(catchError(this.handleError));
  }

  getEtudiantsSansGroupe(): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/etudiants/sans-groupe`)
      .pipe(catchError(this.handleError));
  }

  affecterFormateur(formateurId: number, groupeId: number, data: { role: string; matiere?: string; heuresParSemaine?: number; commentaire?: string }): Observable<GroupeFormateur> {
    let params = new HttpParams().set('role', data.role);
    const matiere = this.normalizeCommentaire(data.matiere);

    if (matiere) {
      params = params.set('matiere', matiere);
    }

    if (data.heuresParSemaine !== undefined && data.heuresParSemaine !== null) {
      params = params.set('heuresParSemaine', data.heuresParSemaine.toString());
    }

    const commentaire = this.normalizeCommentaire(data.commentaire);
    if (commentaire) {
      params = params.set('commentaire', commentaire);
    }

    return this.http.post<GroupeFormateur>(`${this.apiUrl}/formateurs/affecter/${formateurId}/${groupeId}`, {}, { params })
      .pipe(catchError(this.handleError));
  }

  retirerFormateur(formateurId: number, groupeId: number): Observable<GroupeFormateur> {
    return this.http.put<GroupeFormateur>(`${this.apiUrl}/formateurs/retirer/${formateurId}/${groupeId}`, {})
      .pipe(catchError(this.handleError));
  }

  changerRoleFormateur(formateurId: number, groupeId: number, nouveauRole: string): Observable<GroupeFormateur> {
    return this.http.put<GroupeFormateur>(`${this.apiUrl}/formateurs/changer-role/${formateurId}/${groupeId}/${nouveauRole}`, {})
      .pipe(catchError(this.handleError));
  }

  getFormateursParGroupe(groupeId: number): Observable<GroupeFormateur[]> {
    return this.http.get<GroupeFormateur[]>(`${this.apiUrl}/formateurs/groupe/${groupeId}`)
      .pipe(catchError(this.handleError));
  }

  getFormateurPrincipal(groupeId: number): Observable<GroupeFormateur> {
    return this.http.get<GroupeFormateur>(`${this.apiUrl}/formateurs/groupe/${groupeId}/principal`)
      .pipe(catchError(this.handleError));
  }

  getGroupesSansPrincipal(): Observable<Groupe[]> {
    return this.http.get<Groupe[]>(`${this.apiUrl}/formateurs/groupes-sans-principal`)
      .pipe(catchError(this.handleError));
  }

  getChargeHoraireFormateurs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/formateurs/charge-horaire`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('AffectationGroupeService error:', error);
    const message = error?.error?.message || error?.message || 'An error occurred during the assignment operation';
    return throwError(() => new Error(message));
  }

  private normalizeCommentaire(value?: string): string | undefined {
    const trimmed = value?.trim();
    if (!trimmed || trimmed === '-') {
      return undefined;
    }

    return trimmed;
  }
}