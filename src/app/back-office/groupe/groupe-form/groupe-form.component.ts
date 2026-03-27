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
        this.niveauId = data.niveauId || null;
        this.capaciteMin = data.capaciteMin || null;
        this.capaciteMax = data.capaciteMax || null;
        this.dateDebut = data.dateDebut ? data.dateDebut.toString().substring(0, 10) : '';
        this.dateFin = data.dateFin ? data.dateFin.toString().substring(0, 10) : '';
        this.selectedType = (data.type as TypeGroupe) || TypeGroupe.ETUDIANT;
        this.selectedStatut = (data.statut as StatutGroupe) || StatutGroupe.OUVERT;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Erreur lors du chargement du groupe';
        this.isLoading = false;
      }
    });
  }

  validateForm(): boolean {
    this.errorMessage = '';
    if (!this.codeGroupe.trim()) {
      this.errorMessage = 'Le code groupe est requis';
      return false;
    }
    if (!this.nom.trim()) {
      this.errorMessage = 'Le nom est requis';
      return false;
    }
    if (this.capaciteMin !== null && this.capaciteMax !== null && this.capaciteMin > this.capaciteMax) {
      this.errorMessage = 'La capacité minimale ne peut pas dépasser la capacité maximale';
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
      niveauId: this.niveauId || undefined,
      capaciteMin: this.capaciteMin || undefined,
      capaciteMax: this.capaciteMax || undefined,
      dateDebut: this.dateDebut || undefined,
      dateFin: this.dateFin || undefined,
      type: this.selectedType,
      statut: this.selectedStatut
    };

    if (this.isEditMode && this.groupeId) {
      payload.idGroupe = this.groupeId;
      this.groupeService.updateGroupe(payload).subscribe({
        next: () => {
          this.successMessage = 'Groupe mis à jour avec succès !';
          this.isLoading = false;
          setTimeout(() => this.router.navigate(['/admin/groupe']), 1500);
        },
        error: (err: any) => {
          this.errorMessage = 'Erreur lors de la mise à jour : ' + err.message;
          this.isLoading = false;
        }
      });
    } else {
      this.groupeService.createGroupe(payload).subscribe({
        next: () => {
          this.successMessage = 'Groupe créé avec succès !';
          this.isLoading = false;
          setTimeout(() => this.router.navigate(['/admin/groupe']), 1500);
        },
        error: (err: any) => {
          this.errorMessage = 'Erreur lors de la création : ' + err.message;
          this.isLoading = false;
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/admin/groupe']);
  }
}
