import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ReglementGroupeService } from '../../../services/reglement-groupe.service';
import { GroupeService } from '../../../services/groupe.service';
import { ReglementGroupe, CategorieReglement, TypeSanction } from '../../../models/reglement-groupe.model';
import { Groupe } from '../../../models/groupe.model';

@Component({
  selector: 'app-reglement-form',
  templateUrl: './reglement-form.component.html',
  styleUrls: ['./reglement-form.component.scss'],
  standalone: false
})
export class ReglementFormComponent implements OnInit {
  reglementForm: FormGroup;
  isEditMode = false;
  idReglement: number | null = null;
  groupes: Groupe[] = [];
  categories = Object.values(CategorieReglement);
  sanctions = Object.values(TypeSanction);
  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private reglementService: ReglementGroupeService,
    private groupeService: GroupeService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.reglementForm = this.fb.group({
      codeReglement: ['', [Validators.required, Validators.pattern(/^REG-[A-Z]+-[0-9]{3}$/)]],
      titre: ['', [Validators.required]],
      categorie: [CategorieReglement.PRESENCE, [Validators.required]],
      descriptionRegle: ['', [Validators.required]],
      consequence: [''],
      estObligatoire: [true],
      tolerance: [''],
      sanction: [TypeSanction.AVERTISSEMENT],
      seuilApplication: [null],
      estActif: [true],
      groupeId: [null]
    });
  }

  ngOnInit(): void {
    this.idReglement = Number(this.route.snapshot.paramMap.get('id'));
    this.isEditMode = !!this.idReglement && !isNaN(this.idReglement);

    this.loadGroupes();

    if (this.isEditMode && this.idReglement) {
      this.loadReglement(this.idReglement);
    }
  }

  loadGroupes(): void {
    this.groupeService.getAllGroupes().subscribe({
      next: (data) => this.groupes = data,
      error: (err) => console.error('Error loading groupes', err)
    });
  }

  loadReglement(id: number): void {
    this.loading = true;
    this.reglementService.getReglementById(id).subscribe({
      next: (reglement) => {
        this.reglementForm.patchValue({
          ...reglement,
          groupeId: reglement.groupe?.idGroupe || null
        });
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error while loading the rule';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.reglementForm.invalid) return;

    this.loading = true;
    const formValue = this.reglementForm.value;
    const reglement: ReglementGroupe = {
      ...formValue,
      idReglement: this.isEditMode ? this.idReglement! : undefined
    };

    const action = this.isEditMode ? 
      this.reglementService.updateReglement(reglement) : 
      (formValue.groupeId ? 
        this.reglementService.addReglementAndAssignToGroupe(reglement, formValue.groupeId) : 
        this.reglementService.addReglement(reglement));

    action.subscribe({
      next: () => {
        this.router.navigate(['/admin/reglement']);
      },
      error: (err) => {
        this.error = 'An error occurred while saving the rule';
        this.loading = false;
        console.error(err);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/admin/reglement']);
  }

  getCategoryLabel(category: string): string {
    const labels: { [key: string]: string } = {
      PRESENCE: 'Attendance',
      RETARD: 'Tardiness',
      PARTICIPATION: 'Participation',
      COMPORTEMENT: 'Behavior',
      DEVOIRS: 'Homework',
      MATERIEL: 'Materials',
      AUTRE: 'Other'
    };
    return labels[category] || category;
  }

  getSanctionLabel(sanction?: string): string {
    const labels: { [key: string]: string } = {
      AVERTISSEMENT: 'Warning',
      EXCLUSION_TEMPORAIRE: 'Temporary exclusion',
      EXCLUSION_DEFINITIVE: 'Permanent exclusion',
      AUTRE: 'Other'
    };
    return sanction ? (labels[sanction] || sanction) : '-';
  }
}
