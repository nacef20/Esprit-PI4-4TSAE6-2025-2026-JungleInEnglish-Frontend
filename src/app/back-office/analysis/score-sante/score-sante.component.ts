import { Component, OnInit } from '@angular/core';
import { AnalyseGroupeService } from '../../../services/analyse-groupe.service';
import { ScoreSanteGroupeDTO } from '../../../models/analysis.model';

@Component({
  selector: 'app-score-sante',
  templateUrl: './score-sante.component.html',
  styleUrls: ['./score-sante.component.scss'],
  standalone: false
})
export class ScoreSanteComponent implements OnInit {
  joursReference: number = 30;
  scores: ScoreSanteGroupeDTO[] = [];
  filteredScores: ScoreSanteGroupeDTO[] = [];
  selectedNiveau: string = 'ALL';

  loading = false;
  error: string | null = null;

  stats = {
    CRITIQUE: 0,
    FAIBLE: 0,
    MOYEN: 0,
    BON: 0
  };

  constructor(private analyseService: AnalyseGroupeService) {}

  ngOnInit(): void {
    this.refreshScores();
  }

  refreshScores(): void {
    this.loading = true;
    this.error = null;
    this.analyseService.getScoresSante(this.joursReference)
      .subscribe({
        next: (data) => {
          this.scores = data;
          this.calculateStats();
          this.filterByNiveau(this.selectedNiveau);
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Unable to load health scores';
          this.loading = false;
        }
      });
  }

  calculateStats(): void {
    this.stats = {
      CRITIQUE: this.scores.filter(s => s.niveauSante === 'CRITIQUE').length,
      FAIBLE: this.scores.filter(s => s.niveauSante === 'FAIBLE').length,
      MOYEN: this.scores.filter(s => s.niveauSante === 'MOYEN').length,
      BON: this.scores.filter(s => s.niveauSante === 'BON').length
    };
  }

  filterByNiveau(niveau: string): void {
    this.selectedNiveau = niveau;
    if (niveau === 'ALL') {
      this.filteredScores = [...this.scores];
    } else {
      this.filteredScores = this.scores.filter(s => s.niveauSante === niveau);
    }
  }

  getBadgeClass(niveau: string): string {
    switch (niveau) {
      case 'CRITIQUE': return 'badge-critique';
      case 'FAIBLE': return 'badge-faible';
      case 'MOYEN': return 'badge-moyen';
      case 'BON': return 'badge-bon';
      default: return '';
    }
  }

  getProgressColor(niveau: string): string {
    switch (niveau) {
      case 'CRITIQUE': return '#e53e3e';
      case 'FAIBLE': return '#ed8936';
      case 'MOYEN': return '#ecc94b';
      case 'BON': return '#38a169';
      default: return '#cbd5e0';
    }
  }

  getNiveauLabel(niveau: string): string {
    switch (niveau) {
      case 'CRITIQUE': return 'Critical';
      case 'FAIBLE': return 'Low';
      case 'MOYEN': return 'Medium';
      case 'BON': return 'Good';
      default: return niveau;
    }
  }
}
