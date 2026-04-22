import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Groupe, StatutGroupe, TypeGroupe } from '../../../models/groupe.model';
import { GroupeService } from '../../../services/groupe.service';

@Component({
  selector: 'app-groupe-form',
  templateUrl: './groupe-form.component.html',
  styleUrls: ['./groupe-form.component.scss']
})
export class GroupeFormComponent implements OnInit {

  codeGroupe: string = '';
  nom: string = '';
  niveauId: number | null = null;
  capaciteMin: number | null = null;
  capaciteMax: number | null = null;
  dateDebut: string = '';
  dateFin: string = '';
  selectedType: TypeGroupe = TypeGroupe.ETUDIANT;
  selectedStatut: StatutGroupe = StatutGroupe.OUVERT;

  isEditMode: boolean = false;
  groupeId: number | null = null;
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  typeValues = Object.values(TypeGroupe);
  statutValues = Object.values(StatutGroupe);

  constructor(
    private groupeService: GroupeService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.groupeId = +params['id'];
        this.loadGroupe(this.groupeId);
      }
    });
  }

  loadGroupe(id: number): void {
    this.isLoading = true;
    this.groupeService.getGroupeById(id).subscribe({
      next: (data: Groupe) => {
        this.codeGroupe = data.codeGroupe || '';
        this.nom = data.nom || '';
        this.niveauId = data.niveauId ?? null;
        this.capaciteMin = data.capaciteMin ?? null;
        this.capaciteMax = data.capaciteMax ?? null;
        this.dateDebut = data.dateDebut ? data.dateDebut.toString().substring(0, 10) : '';
        this.dateFin = data.dateFin ? data.dateFin.toString().substring(0, 10) : '';
        this.selectedType = (data.type as TypeGroupe) || TypeGroupe.ETUDIANT;
        this.selectedStatut = (data.statut as StatutGroupe) || StatutGroupe.OUVERT;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Error while loading the group';
        this.isLoading = false;
      }
    });
  }

  validateForm(): boolean {
    this.errorMessage = '';
    if (!this.codeGroupe.trim()) {
      this.errorMessage = 'Group code is required';
      return false;
    }
    if (!this.nom.trim()) {
      this.errorMessage = 'Group name is required';
      return false;
    }
    if (this.capaciteMin !== null && this.capaciteMax !== null && this.capaciteMin > this.capaciteMax) {
      this.errorMessage = 'Minimum capacity cannot exceed maximum capacity';
      return false;
    }
    return true;
  }

  onSubmit(): void {
    if (!this.validateForm()) return;

    this.isLoading = true;

    const payload: Groupe = {
      codeGroupe: this.codeGroupe.trim(),
      nom: this.nom.trim(),
      niveauId: this.niveauId ?? undefined,
      capaciteMin: this.capaciteMin ?? undefined,
      capaciteMax: this.capaciteMax ?? undefined,
      dateDebut: this.dateDebut || undefined,
      dateFin: this.dateFin || undefined,
      type: this.selectedType,
      statut: this.selectedStatut
    };

    if (this.isEditMode && this.groupeId) {
      payload.idGroupe = this.groupeId;
      this.groupeService.updateGroupe(payload).subscribe({
        next: () => {
          this.successMessage = 'Group updated successfully!';
          this.isLoading = false;
          setTimeout(() => this.router.navigate(['/admin/groupe']), 1500);
        },
        error: (err: any) => {
          this.errorMessage = 'Error while updating: ' + err.message;
          this.isLoading = false;
        }
      });
    } else {
      this.groupeService.createGroupe(payload).subscribe({
        next: () => {
          this.successMessage = 'Group created successfully!';
          this.isLoading = false;
          setTimeout(() => this.router.navigate(['/admin/groupe']), 1500);
        },
        error: (err: any) => {
          this.errorMessage = 'Error while creating: ' + err.message;
          this.isLoading = false;
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/admin/groupe']);
  }

  getTypeLabel(type?: string): string {
    const labels: { [key: string]: string } = {
      ETUDIANT: 'Student',
      CLASSE: 'Class',
      ADMINISTRATION: 'Administration'
    };
    return type ? (labels[type] || type) : '-';
  }

  getStatusLabel(status?: string): string {
    const labels: { [key: string]: string } = {
      OUVERT: 'Open',
      COMPLET: 'Full',
      EN_COURS: 'In progress',
      TERMINE: 'Completed',
      ANNULE: 'Canceled',
      ARCHIVE: 'Archived'
    };
    return status ? (labels[status] || status) : '-';
  }
}
