import { Component, OnInit } from '@angular/core';
import { AnalyseGroupeService } from '../../../services/analyse-groupe.service';
import { ImpactTransfertDTO, GroupeDestinationDTO, InitiateurChangement } from '../../../models/analysis.model';

@Component({
  selector: 'app-transferts-impact',
  templateUrl: './transferts-impact.component.html',
  styleUrls: ['./transferts-impact.component.scss'],
  standalone: false
})
export class TransfertsImpactComponent implements OnInit {
  // Filtres
  dateDebut: string = '';
  dateFin: string = '';
  initiePar?: InitiateurChangement;
  initiateurs = Object.values(InitiateurChangement);

  impacts: ImpactTransfertDTO[] = [];
  destinations: GroupeDestinationDTO[] = [];
  
  loading = false;
  error: string | null = null;

  constructor(private analyseService: AnalyseGroupeService) {
    // Par défaut : année en cours
    const now = new Date();
    this.dateDebut = new Date(now.getFullYear(), 0, 1).toISOString().slice(0, 16);
    this.dateFin = now.toISOString().slice(0, 16);
  }

  ngOnInit(): void {
    this.refreshAnalyse();
  }

  refreshAnalyse(): void {
    if (!this.dateDebut || !this.dateFin) return;
    
    this.loading = true;
    this.error = null;

    // On lance les deux appels en parallèle
    this.analyseService.getImpactTransferts(this.dateDebut, this.dateFin, this.initiePar)
      .subscribe({
        next: (data) => {
          this.impacts = data;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Erreur lors de l\'analyse d\'impact';
          this.loading = false;
        }
      });

    this.analyseService.getMeilleursGroupesDestination(this.dateDebut, this.dateFin)
      .subscribe({
        next: (data) => this.destinations = data,
        error: (err) => console.error(err)
      });
  }

  getImpactClass(score: number): string {
    if (score > 0) return 'text-success';
    if (score < 0) return 'text-danger';
    return '';
  }
}
