import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AffectationGroupeService } from '../../../services/affectation-groupe.service';
import { GroupeEtudiant } from '../../../models/affectation.model';

@Component({
  selector: 'app-profil-groupe-etudiant',
  templateUrl: './profil-groupe-etudiant.component.html',
  styleUrls: ['./profil-groupe-etudiant.component.scss']
})
export class ProfilGroupeEtudiantComponent implements OnInit {
  etudiantId!: number;
  groupeActuel: GroupeEtudiant | null = null;
  historique: GroupeEtudiant[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private affectationService: AffectationGroupeService
  ) {}

  ngOnInit(): void {
    this.etudiantId = Number(this.route.snapshot.paramMap.get('etudiantId'));
    if (this.etudiantId) {
      this.loadData();
    }
  }

  loadData(): void {
    this.loading = true;
    this.error = null;

    this.affectationService.getGroupeActuelEtudiant(this.etudiantId).subscribe({
      next: (data) => this.groupeActuel = data,
      error: (err) => console.log('Aucun groupe actuel')
    });

    this.affectationService.getHistoriqueEtudiant(this.etudiantId).subscribe({
      next: (data) => {
        this.historique = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error while loading the student profile';
        this.loading = false;
      }
    });
  }

  getStatutClass(statut: string): string {
    switch (statut) {
      case 'ACTIF':
        return 'badge-success';
      case 'SUSPENDU':
        return 'badge-warning';
      case 'SORTI':
        return 'badge-danger';
      default:
        return 'badge-secondary';
    }
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      ACTIF: 'Active',
      SUSPENDU: 'Suspended',
      SORTI: 'Left'
    };
    return labels[status] || status;
  }
}