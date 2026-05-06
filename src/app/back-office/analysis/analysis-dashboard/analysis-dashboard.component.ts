import { Component, OnInit } from '@angular/core';
import { AnalyseGroupeService } from '../../../services/analyse-groupe.service';
import { AnomalieService } from '../../../services/anomalie.service';
import { DashboardStatsDTO } from '../../../models/analysis.model';

@Component({
  selector: 'app-analysis-dashboard',
  templateUrl: './analysis-dashboard.component.html',
  styleUrls: ['./analysis-dashboard.component.scss'],
  standalone: false
})
export class AnalysisDashboardComponent implements OnInit {
  stats: DashboardStatsDTO = {
    totalGroupesActifs: 0,
    totalEtudiantsAffectes: 0,
    totalFormateursActifs: 0,
    tauxRemplissageMoyen: 0,
    groupesSousRemplis: 0,
    groupesQuasiPleins: 0,
    transfertsCeMois: 0,
    objectifsEnCours: 0,
    ratioEtudiantsParFormateur: 0,
    tendance: 'STABLE'
  };
  anomaliesCount = { CRITIQUE: 0, WARNING: 0, INFO: 0 };
  
  loading = true;
  error: string | null = null;

  constructor(
    private analyseService: AnalyseGroupeService,
    private anomalieService: AnomalieService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.error = null;

    // Load Global Stats
    this.analyseService.getGlobalStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      }
    });

    // Load Anomalies summary for the "Critical" card
    this.anomalieService.getResume().subscribe({
      next: (resume) => {
        this.anomaliesCount.CRITIQUE = resume['CRITIQUE'] || 0;
        this.anomaliesCount.WARNING = resume['WARNING'] || 0;
        this.anomaliesCount.INFO = resume['INFO'] || 0;
      }
    });
  }

  getTendanceIcon(): string {
    if (!this.stats) return 'trending_flat';
    switch (this.stats.tendance) {
      case 'CROISSANCE': return 'trending_up';
      case 'DECROISSANCE': return 'trending_down';
      default: return 'trending_flat';
    }
  }

  getTendanceClass(): string {
    if (!this.stats) return '';
    return 'tendance-' + this.stats.tendance.toLowerCase();
  }

  getTendanceLabel(): string {
    switch (this.stats.tendance) {
      case 'CROISSANCE': return 'Growth';
      case 'DECROISSANCE': return 'Decline';
      default: return 'Stable';
    }
  }
}
