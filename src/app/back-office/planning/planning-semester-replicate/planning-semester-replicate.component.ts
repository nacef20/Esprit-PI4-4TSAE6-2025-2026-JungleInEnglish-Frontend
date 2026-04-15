import { Component, OnInit } from '@angular/core';
import { PlanningService } from '../../../services/planning.service';
import {
  SemesterReplicationRequest,
  SemesterReplicationResult
} from '../../../models/planning.model';

@Component({
  selector: 'app-planning-semester-replicate',
  templateUrl: './planning-semester-replicate.component.html',
  styleUrls: ['./planning-semester-replicate.component.scss']
})
export class PlanningSemesterReplicateComponent implements OnInit {

  // ── Formulaire ───────────────────────────────────────────────────
  semaineReference: string = '';
  debutSemestre: string = '';
  finSemestre: string = '';

  joursFeries: string[] = [];
  newJourFerie: string = '';

  semainesExamens: string[] = [];
  newSemaineExamen: string = '';

  // ── État ─────────────────────────────────────────────────────────
  isLoading: boolean = false;
  errorMessage: string = '';
  result: SemesterReplicationResult | null = null;

  // ── Détail expandable ────────────────────────────────────────────
  expandedWeeks: Set<string> = new Set();
  expandedIgnored: Set<string> = new Set();

  // ── Entrées calculées depuis le résultat ─────────────────────────
  sessionsParSemaineEntries: { date: string; sessions: any[] }[] = [];
  raisonsIgnoreesEntries: { date: string; raisons: string[] }[] = [];

  constructor(private planningService: PlanningService) {}

  ngOnInit(): void {
    this.initDefaultDates();
  }

  // ── Initialisation des dates par défaut ──────────────────────────

  private initDefaultDates(): void {
    // Prochain lundi comme semaine de référence
    const monday = this.getNextMonday(new Date());
    this.semaineReference = this.toDateString(monday);

    // Semestre : du lundi courant à ~5 mois plus tard
    this.debutSemestre = this.toDateString(monday);
    const fin = new Date(monday);
    fin.setMonth(fin.getMonth() + 5);
    this.finSemestre = this.toDateString(fin);
  }

  private getNextMonday(from: Date): Date {
    const d = new Date(from);
    const day = d.getDay(); // 0=Dim
    const diff = day === 1 ? 0 : day === 0 ? 1 : 8 - day;
    d.setDate(d.getDate() + diff);
    return d;
  }

  private toDateString(d: Date): string {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  // ── Jours fériés ─────────────────────────────────────────────────

  addJourFerie(): void {
    const val = this.newJourFerie?.trim();
    if (!val) return;
    if (this.joursFeries.includes(val)) {
      this.errorMessage = `Le jour férié ${val} est déjà dans la liste.`;
      return;
    }
    this.joursFeries.push(val);
    this.joursFeries.sort();
    this.newJourFerie = '';
    this.errorMessage = '';
  }

  removeJourFerie(date: string): void {
    this.joursFeries = this.joursFeries.filter(d => d !== date);
  }

  // ── Semaines d'examens ────────────────────────────────────────────

  addSemaineExamen(): void {
    const val = this.newSemaineExamen?.trim();
    if (!val) return;
    if (this.semainesExamens.includes(val)) {
      this.errorMessage = `La semaine d'examen ${val} est déjà dans la liste.`;
      return;
    }
    this.semainesExamens.push(val);
    this.semainesExamens.sort();
    this.newSemaineExamen = '';
    this.errorMessage = '';
  }

  removeSemaineExamen(date: string): void {
    this.semainesExamens = this.semainesExamens.filter(d => d !== date);
  }

  // ── Validation ───────────────────────────────────────────────────

  private validate(): string | null {
    if (!this.semaineReference) return 'La semaine de référence est obligatoire.';
    if (!this.debutSemestre)    return 'La date de début du semestre est obligatoire.';
    if (!this.finSemestre)      return 'La date de fin du semestre est obligatoire.';
    if (this.debutSemestre > this.finSemestre)
      return 'La date de début doit être antérieure à la date de fin.';
    if (!this.isMonday(this.semaineReference))
      return 'La semaine de référence doit être un lundi.';
    return null;
  }

  private isMonday(dateStr: string): boolean {
    const d = new Date(dateStr + 'T00:00:00');
    return d.getDay() === 1;
  }

  // ── Lancement de la réplication ───────────────────────────────────

  replicate(): void {
    const err = this.validate();
    if (err) {
      this.errorMessage = err;
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.result = null;
    this.sessionsParSemaineEntries = [];
    this.raisonsIgnoreesEntries = [];
    this.expandedWeeks.clear();
    this.expandedIgnored.clear();

    const request: SemesterReplicationRequest = {
      semaineReference: this.semaineReference,
      debutSemestre:    this.debutSemestre,
      finSemestre:      this.finSemestre,
      joursFeries:      [...this.joursFeries],
      semainesExamens:  [...this.semainesExamens]
    };

    this.planningService.replicatePlanningSemester(request).subscribe({
      next: (data) => {
        this.result = data;
        this.buildEntries(data);
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Erreur lors de la réplication du semestre.';
        this.isLoading = false;
      }
    });
  }

  private buildEntries(r: SemesterReplicationResult): void {
    this.sessionsParSemaineEntries = Object.entries(r.sessionsParSemaine || {})
      .map(([date, sessions]) => ({ date, sessions }))
      .sort((a, b) => a.date.localeCompare(b.date));

    this.raisonsIgnoreesEntries = Object.entries(r.raisonsIgnoreesParSemaine || {})
      .map(([date, raisons]) => ({ date, raisons }))
      .sort((a, b) => a.date.localeCompare(b.date));
  }

  // ── Expand / collapse ─────────────────────────────────────────────

  toggleWeek(date: string): void {
    this.expandedWeeks.has(date)
      ? this.expandedWeeks.delete(date)
      : this.expandedWeeks.add(date);
  }

  isWeekExpanded(date: string): boolean {
    return this.expandedWeeks.has(date);
  }

  toggleIgnored(date: string): void {
    this.expandedIgnored.has(date)
      ? this.expandedIgnored.delete(date)
      : this.expandedIgnored.add(date);
  }

  isIgnoredExpanded(date: string): boolean {
    return this.expandedIgnored.has(date);
  }

  // ── Utilitaires d'affichage ───────────────────────────────────────

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString('fr-FR', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  getSuccessRate(): number {
    if (!this.result) return 0;
    const total = this.result.totalSessionsCrees + this.result.totalSessionsIgnorees;
    if (total === 0) return 0;
    return Math.round((this.result.totalSessionsCrees / total) * 100);
  }

  reset(): void {
    this.result = null;
    this.errorMessage = '';
    this.sessionsParSemaineEntries = [];
    this.raisonsIgnoreesEntries = [];
    this.expandedWeeks.clear();
    this.expandedIgnored.clear();
    this.initDefaultDates();
    this.joursFeries = [];
    this.semainesExamens = [];
  }
}
