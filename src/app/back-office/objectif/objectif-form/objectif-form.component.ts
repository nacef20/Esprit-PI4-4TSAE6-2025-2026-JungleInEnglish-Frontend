import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ObjectifPedagogiqueService } from '../../../services/objectif-pedagogique.service';
import { GroupeService } from '../../../services/groupe.service';
import { ObjectifPedagogique, CompetenceCible, NiveauDifficulte, StatutObjectif, MethodeEvaluation } from '../../../models/objectif-pedagogique.model';
import { Groupe } from '../../../models/groupe.model';

@Component({
  selector: 'app-objectif-form',
  templateUrl: './objectif-form.component.html',
  styleUrls: ['./objectif-form.component.scss'],
  standalone: false
})
export class ObjectifFormComponent implements OnInit {
  objectifForm: FormGroup;
  isEditMode = false;
  idObjectif: number | null = null;
  groupes: Groupe[] = [];
  
  competences = Object.values(CompetenceCible);
  niveaux = Object.values(NiveauDifficulte);
  statuts = Object.values(StatutObjectif);
  evaluations = Object.values(MethodeEvaluation);

  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private objectifService: ObjectifPedagogiqueService,
    private groupeService: GroupeService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.objectifForm = this.fb.group({
      codeObjectif: ['', [Validators.required, Validators.pattern(/^OBJ-[A-Z0-9]+-[A-Z]+-[0-9]{3}$/)]],
      titre: ['', [Validators.required]],
      description: ['', [Validators.required]],
      competenceCible: [CompetenceCible.SPEAKING, [Validators.required]],
      niveauDifficulte: [NiveauDifficulte.MOYEN, [Validators.required]],
      priorite: [3, [Validators.required, Validators.min(1), Validators.max(5)]],
      dateDebutPrevue: [null],
      dateFinPrevue: [null],
      dureeEstimeeHeures: [0],
      prerequis: [''],
      statut: [StatutObjectif.NON_COMMENCE, [Validators.required]],
      tauxReussiteGroupe: [0, [Validators.min(0), Validators.max(100)]],
      nombreSeancesNecessaires: [0],
      supportsPedagogiques: [''],
      methodeEvaluation: [MethodeEvaluation.OBSERVATION],
      commentaireProfesseur: [''],
      groupeId: [null]
    });
  }

  ngOnInit(): void {
    this.idObjectif = Number(this.route.snapshot.paramMap.get('id'));
    this.isEditMode = !!this.idObjectif && !isNaN(this.idObjectif);

    this.loadGroupes();

    if (this.isEditMode && this.idObjectif) {
      this.loadObjectif(this.idObjectif);
    }
  }

  loadGroupes(): void {
    this.groupeService.getAllGroupes().subscribe({
      next: (data) => this.groupes = data,
      error: (err) => console.error('Error loading groupes', err)
    });
  }

  loadObjectif(id: number): void {
    this.loading = true;
    this.objectifService.getObjectifById(id).subscribe({
      next: (objectif) => {
        this.objectifForm.patchValue({
          ...objectif,
          groupeId: objectif.groupe?.idGroupe || null
        });
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error while loading the objective';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.objectifForm.invalid) return;

    this.loading = true;
    const formValue = this.objectifForm.value;
    const objectif: ObjectifPedagogique = {
      ...formValue,
      idObjectif: this.isEditMode ? this.idObjectif! : undefined
    };

    const action = this.isEditMode ? 
      this.objectifService.updateObjectif(objectif) : 
      (formValue.groupeId ? 
        this.objectifService.addObjectifAndAssignToGroupe(objectif, formValue.groupeId) : 
        this.objectifService.addObjectif(objectif));

    action.subscribe({
      next: () => {
        this.router.navigate(['/admin/objectif']);
      },
      error: (err) => {
        this.error = 'An error occurred while saving the objective';
        this.loading = false;
        console.error(err);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/admin/objectif']);
  }

  getCompetenceLabel(competence: string): string {
    const labels: { [key: string]: string } = {
      LISTENING: 'Listening',
      SPEAKING: 'Speaking',
      READING: 'Reading',
      WRITING: 'Writing',
      GRAMMAR: 'Grammar',
      VOCABULARY: 'Vocabulary'
    };
    return labels[competence] || competence;
  }

  getLevelLabel(level: string): string {
    const labels: { [key: string]: string } = {
      FACILE: 'Easy',
      MOYEN: 'Medium',
      DIFFICILE: 'Hard',
      TRES_DIFFICILE: 'Very hard'
    };
    return labels[level] || level;
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      NON_COMMENCE: 'Not started',
      EN_COURS: 'In progress',
      ATTEINT: 'Achieved',
      PARTIELLEMENT_ATTEINT: 'Partially achieved',
      NON_ATTEINT: 'Not achieved'
    };
    return labels[status] || status;
  }

  getEvaluationLabel(evaluation: string): string {
    const labels: { [key: string]: string } = {
      TEST: 'Test',
      ORAL: 'Oral',
      PROJET: 'Project',
      OBSERVATION: 'Observation'
    };
    return labels[evaluation] || evaluation;
  }
}
