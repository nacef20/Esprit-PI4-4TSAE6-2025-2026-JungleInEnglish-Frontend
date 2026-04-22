import { Component, OnInit } from '@angular/core';
import { AuditTrailService } from '../../../services/audit-trail.service';
import { ParcoursEtudiantDTO, RetentionGroupeDTO } from '../../../models/audit.model';

@Component({
  selector: 'app-audit-parcours',
  templateUrl: './audit-parcours.component.html',
  styleUrls: ['./audit-parcours.component.scss'],
  standalone: false
})
export class AuditParcoursComponent implements OnInit {
  activeTab: 'PARCOURS' | 'RETENTION' = 'PARCOURS';

  // Parcours
  etudiantIdSearch: number | null = null;
  parcours: ParcoursEtudiantDTO[] = [];
  parcoursLoading = false;
  parcoursError: string | null = null;

  // Rétention
  retentionStats: RetentionGroupeDTO[] = [];
  retentionLoading = false;
  retentionError: string | null = null;
  seuilMinimum = 1;

  constructor(private auditService: AuditTrailService) {}

  ngOnInit(): void {
    this.loadRetention();
  }

  setTab(tab: 'PARCOURS' | 'RETENTION'): void {
    this.activeTab = tab;
  }

  // ── Parcours ─────────────────────────────────────────────────────
  rechercherParcours(): void {
    if (!this.etudiantIdSearch) {
      this.parcoursError = 'Veuillez entrer un ID étudiant.';
      return;
    }
    this.parcoursLoading = true;
    this.parcoursError = null;
    this.parcours = [];

    this.auditService.getParcoursEtudiant(this.etudiantIdSearch).subscribe({
      next: (data) => {
        this.parcours = data;
        this.parcoursLoading = false;
        if (data.length === 0) {
          this.parcoursError = 'Aucun parcours trouvé pour cet étudiant.';
        }
      },
      error: (err) => {
        this.parcoursError = err.message;
        this.parcoursLoading = false;
      }
    });
  }

  getScoreEvolutionClass(evolution: number): string {
    if (evolution > 0) return 'score-up';
    if (evolution < 0) return 'score-down';
    return 'score-neutral';
  }

  getScoreEvolutionIcon(evolution: number): string {
    if (evolution > 0) return 'trending_up';
    if (evolution < 0) return 'trending_down';
    return 'trending_flat';
  }

  getStatutClass(statut: string): string {
    switch (statut) {
      case 'ACTIF': return 'badge-actif';
      case 'SORTI': return 'badge-sorti';
      case 'SUSPENDU': return 'badge-suspendu';
      default: return '';
    }
  }

  getTotalDuree(): number {
    return this.parcours.reduce((sum, p) => sum + p.dureeSejour, 0);
  }

  getTotalEvolution(): number {
    const first = this.parcours[0]?.scoreEntree ?? 0;
    const last = this.parcours[this.parcours.length - 1];
    const lastScore = last?.scoreApres ?? last?.scoreEntree ?? 0;
    return lastScore - first;
  }

  // ── Rétention ────────────────────────────────────────────────────
  loadRetention(): void {
    this.retentionLoading = true;
    this.retentionError = null;

    this.auditService.getStatistiquesRetention(this.seuilMinimum).subscribe({
      next: (data) => {
        this.retentionStats = data;
        this.retentionLoading = false;
      },
      error: (err) => {
        this.retentionError = err.message;
        this.retentionLoading = false;
      }
    });
  }

  getRetentionClass(taux: number): string {
    if (taux >= 70) return 'retention-bon';
    if (taux >= 40) return 'retention-moyen';
    return 'retention-faible';
  }
}
