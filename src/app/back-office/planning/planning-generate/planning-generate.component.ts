import { Component, OnInit } from '@angular/core';
import { PlanningService } from '../../../services/planning.service';
import {
  AssignmentRequest,
  PlanningGeneratorRequest,
  GenerationResult
} from '../../../models/planning.model';

@Component({
  selector: 'app-planning-generate',
  templateUrl: './planning-generate.component.html',
  styleUrls: ['./planning-generate.component.scss']
})
export class PlanningGenerateComponent implements OnInit {

  // ---- Données chargées depuis la DB ----
  allCreneaux: any[] = [];
  allSalles: any[] = [];

  // ---- Formulaire ----
  startDate: string = '';
  selectedCreneauIds: number[] = [];
  selectedSalleIds: number[] = [];
  assignments: AssignmentRequest[] = [];

  newAssignment: AssignmentRequest = { groupeId: 0, matiereId: 0, enseignantId: 0 };

  // ---- État ----
  isLoading: boolean = false;
  errorMessage: string = '';
  result: GenerationResult | null = null;

  constructor(private planningService: PlanningService) {}

  ngOnInit(): void {
    this.setDefaultStartDate();
    this.loadDropdownData();
  }

  /** Initialise la date sur le prochain lundi */
  private setDefaultStartDate(): void {
    const today = new Date();
    const day = today.getDay(); // 0=Dim, 1=Lun, ...
    const diffToMonday = day === 0 ? 1 : 8 - day;
    const nextMonday = new Date(today);
    nextMonday.setDate(today.getDate() + diffToMonday);
    const yyyy = nextMonday.getFullYear();
    const mm = String(nextMonday.getMonth() + 1).padStart(2, '0');
    const dd = String(nextMonday.getDate()).padStart(2, '0');
    this.startDate = `${yyyy}-${mm}-${dd}`;
  }

  /** Charge les créneaux et salles depuis le backend */
  private loadDropdownData(): void {
    this.planningService.getAllCreneaux().subscribe({
      next: (data) => { this.allCreneaux = data; },
      error: () => { this.errorMessage = 'Impossible de charger les créneaux horaires.'; }
    });
    this.planningService.getAllSalles().subscribe({
      next: (data) => { this.allSalles = data; },
      error: () => { this.errorMessage = 'Impossible de charger les salles.'; }
    });
  }

  // ---- Sélection multi-créneaux ----
  toggleCreneau(id: number): void {
    const idx = this.selectedCreneauIds.indexOf(id);
    if (idx === -1) {
      this.selectedCreneauIds.push(id);
    } else {
      this.selectedCreneauIds.splice(idx, 1);
    }
  }

  isCreneauSelected(id: number): boolean {
    return this.selectedCreneauIds.includes(id);
  }

  // ---- Sélection multi-salles ----
  toggleSalle(id: number): void {
    const idx = this.selectedSalleIds.indexOf(id);
    if (idx === -1) {
      this.selectedSalleIds.push(id);
    } else {
      this.selectedSalleIds.splice(idx, 1);
    }
  }

  isSalleSelected(id: number): boolean {
    return this.selectedSalleIds.includes(id);
  }

  // ---- Gestion des assignments ----
  addAssignment(): void {
    if (
      !this.newAssignment.groupeId ||
      !this.newAssignment.matiereId ||
      !this.newAssignment.enseignantId
    ) {
      this.errorMessage = 'Veuillez renseigner les 3 champs de l\'affectation avant d\'ajouter.';
      return;
    }
    this.errorMessage = '';
    this.assignments.push({ ...this.newAssignment });
    this.newAssignment = { groupeId: 0, matiereId: 0, enseignantId: 0 };
  }

  removeAssignment(index: number): void {
    this.assignments.splice(index, 1);
  }

  // ---- Validation globale ----
  isFormValid(): boolean {
    return (
      !!this.startDate &&
      this.selectedCreneauIds.length > 0 &&
      this.selectedSalleIds.length > 0 &&
      this.assignments.length > 0
    );
  }

  // ---- Soumission ----
  generatePlanning(): void {
    if (!this.isFormValid()) {
      this.errorMessage = 'Veuillez remplir tous les champs requis.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.result = null;

    const request: PlanningGeneratorRequest = {
      startDate: this.startDate,
      creneauIds: this.selectedCreneauIds,
      salleIds: this.selectedSalleIds,
      assignments: this.assignments
    };

    this.planningService.generateWeeklyPlanning(request).subscribe({
      next: (res) => {
        this.result = res;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.message || 'Erreur lors de la génération du planning.';
        this.isLoading = false;
      }
    });
  }

  resetForm(): void {
    this.setDefaultStartDate();
    this.selectedCreneauIds = [];
    this.selectedSalleIds = [];
    this.assignments = [];
    this.newAssignment = { groupeId: 0, matiereId: 0, enseignantId: 0 };
    this.result = null;
    this.errorMessage = '';
  }

  formatTime(t: string): string {
    return t ? t.substring(0, 5) : '';
  }
}
