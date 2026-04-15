import { Component, OnInit } from '@angular/core';
import { AffectationGroupeService } from '../../../services/affectation-groupe.service';
import { Groupe } from '../../../models/groupe.model';

@Component({
  selector: 'app-dashboard-affectations',
  templateUrl: './dashboard-affectations.component.html',
  styleUrls: ['./dashboard-affectations.component.scss']
})
export class DashboardAffectationsComponent implements OnInit {
  etudiantsSansGroupe: number[] = [];
  groupesSansPrincipal: Groupe[] = [];
  chargeHoraireFormateurs: any[] = [];

  loading = {
    etudiants: false,
    groupes: false,
    charge: false
  };

  showListEtudiants = false;

  constructor(private affectationService: AffectationGroupeService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading.etudiants = true;
    this.affectationService.getEtudiantsSansGroupe().subscribe({
      next: (data) => {
        this.etudiantsSansGroupe = data;
        this.loading.etudiants = false;
      },
      error: () => this.loading.etudiants = false
    });

    this.loading.groupes = true;
    this.affectationService.getGroupesSansPrincipal().subscribe({
      next: (data) => { 
        this.groupesSansPrincipal = data.map((obj: any) => obj[0]);
        this.loading.groupes = false; 
      },
      error: () => this.loading.groupes = false
    });

    this.loading.charge = true;
    this.affectationService.getChargeHoraireFormateurs().subscribe({
      next: (data) => {
        this.chargeHoraireFormateurs = data;
        this.loading.charge = false;
      },
      error: () => this.loading.charge = false
    });
  }

  getChargeColorClass(hours: number): string {
    if (hours > 20) {
      return 'text-danger fw-bold';
    }

    if (hours > 15) {
      return 'text-warning fw-bold';
    }

    return 'text-success fw-bold';
  }
}