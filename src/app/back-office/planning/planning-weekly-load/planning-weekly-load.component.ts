import { Component, OnInit } from '@angular/core';
import { PlanningService } from '../../../services/planning.service';
import { WeeklyLoadReport } from '../../../models/planning.model';

@Component({
  selector: 'app-planning-weekly-load',
  templateUrl: './planning-weekly-load.component.html',
  styleUrls: ['./planning-weekly-load.component.scss']
})
export class PlanningWeeklyLoadComponent implements OnInit {

  selectedDate: string = '';
  report: WeeklyLoadReport | null = null;
  isLoading: boolean = false;
  errorMessage: string = '';

  // Pour les tableaux
  enseignantEntries: { id: number; heures: number }[] = [];
  groupeEntries: { id: number; heures: number }[] = [];
  salleEntries: { id: number; taux: number }[] = [];

  // Seuils (miroir du backend)
  readonly SEUIL_ENSEIGNANT = 30;
  readonly SEUIL_GROUPE = 35;
  readonly SEUIL_SALLE = 80;

  constructor(private planningService: PlanningService) {}

  ngOnInit(): void {
    this.setDefaultDate();
  }

  private setDefaultDate(): void {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    this.selectedDate = `${yyyy}-${mm}-${dd}`;
  }

  analyzeLoad(): void {
    if (!this.selectedDate) {
      this.errorMessage = 'Veuillez sélectionner une date.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.report = null;

    this.planningService.analyzeWeeklyLoad(this.selectedDate).subscribe({
      next: (data) => {
        this.report = data;
        this.buildEntries(data);
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Erreur lors de l\'analyse de la charge.';
        this.isLoading = false;
      }
    });
  }

  private buildEntries(r: WeeklyLoadReport): void {
    this.enseignantEntries = Object.entries(r.heuresParEnseignant)
      .map(([id, heures]) => ({ id: +id, heures }))
      .sort((a, b) => b.heures - a.heures);

    this.groupeEntries = Object.entries(r.heuresParGroupe)
      .map(([id, heures]) => ({ id: +id, heures }))
      .sort((a, b) => b.heures - a.heures);

    this.salleEntries = Object.entries(r.tauxOccupationParSalle)
      .map(([id, taux]) => ({ id: +id, taux }))
      .sort((a, b) => b.taux - a.taux);
  }

  getBarColor(value: number, seuil: number): string {
    const pct = (value / seuil) * 100;
    if (pct >= 100) return '#e53e3e';
    if (pct >= 80) return '#ed8936';
    return '#4fd1c5';
  }

  getBarWidth(value: number, max: number): number {
    if (max === 0) return 0;
    return Math.min((value / max) * 100, 100);
  }

  getMaxHeuresEnseignant(): number {
    return this.enseignantEntries.length
      ? Math.max(...this.enseignantEntries.map(e => e.heures), this.SEUIL_ENSEIGNANT)
      : this.SEUIL_ENSEIGNANT;
  }

  getMaxHeuresGroupe(): number {
    return this.groupeEntries.length
      ? Math.max(...this.groupeEntries.map(g => g.heures), this.SEUIL_GROUPE)
      : this.SEUIL_GROUPE;
  }

  isAlertSurcharge(alerte: string): boolean {
    return alerte.includes('SURCHARGE') || alerte.includes('SATURÉE');
  }
}
