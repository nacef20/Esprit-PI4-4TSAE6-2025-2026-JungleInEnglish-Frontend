import { Component, OnInit } from '@angular/core';
import { AnalyseGroupeService } from '../../../services/analyse-groupe.service';
import {
  RecommandationGroupeDTO,
  FusionGroupeRequest,
  FusionResultatDTO,
  ScissionGroupeRequest,
  ScissionResultatDTO
} from '../../../models/analysis.model';
import { GroupeService } from '../../../services/groupe.service';
import { Groupe } from '../../../models/groupe.model';

@Component({
  selector: 'app-recommandations-groupe',
  templateUrl: './recommandations-groupe.component.html',
  styleUrls: ['./recommandations-groupe.component.scss'],
  standalone: false
})
export class RecommandationsGroupeComponent implements OnInit {
  activeTab: 'ALL' | 'FUSION' | 'SCISSION' = 'ALL';
  allRecommandations: RecommandationGroupeDTO[] = [];
  filteredRecommandations: RecommandationGroupeDTO[] = [];
  groupes: Groupe[] = [];
  
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;

  showFusionModal = false;
  selectedFusionRec: RecommandationGroupeDTO | null = null;
  validatingFusion = false;
  executingFusion = false;
  fusionValidationResult: FusionResultatDTO | null = null;

  showScissionModal = false;
  selectedScissionRec: RecommandationGroupeDTO | null = null;
  validatingScission = false;
  executingScission = false;
  scissionValidationResult: ScissionResultatDTO | null = null;

  fusionForm = {
    groupeCibleId: undefined as number | undefined,
    transfererEtudiants: true,
    transfererFormateurs: false,
    transfererObjectifs: false,
    transfererReglements: false,
    commentaire: '',
    declenchePar: 1
  };

  scissionForm = {
    groupeCibleId: undefined as number | undefined,
    nombreEtudiantsADeplacer: 1,
    transfererEtudiants: true,
    transfererFormateurs: false,
    transfererObjectifs: false,
    transfererReglements: false,
    commentaire: '',
    declenchePar: 1
  };

  stats = {
    FUSION: 0,
    SCISSION: 0
  };

  constructor(
    private analyseService: AnalyseGroupeService,
    private groupeService: GroupeService
  ) {}

  ngOnInit(): void {
    this.loadGroupes();
    this.loadRecommandations();
  }

  loadGroupes(): void {
    this.groupeService.getAllGroupes().subscribe({
      next: (data) => {
        this.groupes = data;
      },
      error: () => {
        this.error = 'Unable to load the group list.';
      }
    });
  }

  loadRecommandations(): void {
    this.loading = true;
    this.error = null;
    this.analyseService.getToutesRecommandations()
      .subscribe({
        next: (data) => {
          this.allRecommandations = data;
          this.stats.FUSION = data.filter(r => r.typeRecommandation === 'FUSION').length;
          this.stats.SCISSION = data.filter(r => r.typeRecommandation === 'SCISSION').length;
          this.applyFilter();
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Error while loading recommendations';
          this.loading = false;
        }
      });
  }

  setTab(tab: 'ALL' | 'FUSION' | 'SCISSION'): void {
    this.activeTab = tab;
    this.applyFilter();
  }

  applyFilter(): void {
    if (this.activeTab === 'ALL') {
      this.filteredRecommandations = [...this.allRecommandations];
    } else {
      this.filteredRecommandations = this.allRecommandations.filter(r => r.typeRecommandation === this.activeTab);
    }
  }

  getTypeClass(type: string): string {
    return type === 'FUSION' ? 'badge-fusion' : 'badge-scission';
  }

  getFillColor(percent: number): string {
    if (percent < 50) return '#667eea';
    if (percent > 90) return '#ed8936';
    return '#cbd5e0';
  }

  canOpenFusion(rec: RecommandationGroupeDTO): boolean {
    return rec.typeRecommandation === 'FUSION';
  }

  canOpenScission(rec: RecommandationGroupeDTO): boolean {
    return rec.typeRecommandation === 'SCISSION';
  }

  openFusionModal(rec: RecommandationGroupeDTO): void {
    if (!this.canOpenFusion(rec)) {
      return;
    }

    this.selectedFusionRec = rec;
    this.showFusionModal = true;
    this.fusionValidationResult = null;
    this.successMessage = null;
    this.error = null;

    this.fusionForm = {
      groupeCibleId: this.getGroupesCibles(rec)[0]?.idGroupe,
      transfererEtudiants: true,
      transfererFormateurs: false,
      transfererObjectifs: false,
      transfererReglements: false,
      commentaire: '',
      declenchePar: 1
    };
  }

  openScissionModal(rec: RecommandationGroupeDTO): void {
    if (!this.canOpenScission(rec)) {
      return;
    }

    this.selectedScissionRec = rec;
    this.showScissionModal = true;
    this.scissionValidationResult = null;
    this.successMessage = null;
    this.error = null;

    this.scissionForm = {
      groupeCibleId: this.getGroupesCibles(rec)[0]?.idGroupe,
      nombreEtudiantsADeplacer: Math.max(1, Math.floor((rec.capaciteActuelle || 1) / 2)),
      transfererEtudiants: true,
      transfererFormateurs: false,
      transfererObjectifs: false,
      transfererReglements: false,
      commentaire: '',
      declenchePar: 1
    };
  }

  closeFusionModal(): void {
    this.showFusionModal = false;
    this.selectedFusionRec = null;
    this.fusionValidationResult = null;
  }

  closeScissionModal(): void {
    this.showScissionModal = false;
    this.selectedScissionRec = null;
    this.scissionValidationResult = null;
  }

  getGroupesCibles(rec: RecommandationGroupeDTO): Groupe[] {
    const invalidStatuts = ['ANNULE', 'TERMINE', 'ARCHIVE'];
    return this.groupes.filter(g =>
      g.idGroupe !== rec.idGroupe &&
      (!g.statut || !invalidStatuts.includes(g.statut))
    );
  }

  validerFusion(): void {
    if (!this.selectedFusionRec || !this.fusionForm.groupeCibleId) {
      this.error = 'Please select a target group for the merge.';
      return;
    }

    this.validatingFusion = true;
    this.error = null;

    this.analyseService.validerFusion(this.buildFusionRequest()).subscribe({
      next: (result) => {
        this.fusionValidationResult = result;
        this.validatingFusion = false;
      },
      error: (err) => {
        this.error = err.message;
        this.validatingFusion = false;
      }
    });
  }

  executerFusion(): void {
    if (!this.selectedFusionRec || !this.fusionForm.groupeCibleId) {
      this.error = 'Please select a target group for the merge.';
      return;
    }

    this.executingFusion = true;
    this.error = null;

    this.analyseService.executerFusion(this.buildFusionRequest()).subscribe({
      next: (result) => {
        this.fusionValidationResult = result;
        this.successMessage = result.message || 'Merge executed successfully.';
        this.executingFusion = false;
        this.loadRecommandations();
        this.loadGroupes();
      },
      error: (err) => {
        this.error = err.message;
        this.executingFusion = false;
      }
    });
  }

  validerScission(): void {
    if (!this.selectedScissionRec || !this.scissionForm.groupeCibleId) {
      this.error = 'Please select a target group for the split.';
      return;
    }

    if (!Number.isFinite(Number(this.scissionForm.nombreEtudiantsADeplacer)) || Number(this.scissionForm.nombreEtudiantsADeplacer) <= 0) {
      this.error = 'Please enter a valid number of students to move.';
      return;
    }

    this.validatingScission = true;
    this.error = null;

    this.analyseService.validerScission(this.buildScissionRequest()).subscribe({
      next: (result) => {
        this.scissionValidationResult = result;
        this.validatingScission = false;
      },
      error: (err) => {
        this.error = err.message;
        this.validatingScission = false;
      }
    });
  }

  executerScission(): void {
    if (!this.selectedScissionRec || !this.scissionForm.groupeCibleId) {
      this.error = 'Please select a target group for the split.';
      return;
    }

    if (!Number.isFinite(Number(this.scissionForm.nombreEtudiantsADeplacer)) || Number(this.scissionForm.nombreEtudiantsADeplacer) <= 0) {
      this.error = 'Please enter a valid number of students to move.';
      return;
    }

    this.executingScission = true;
    this.error = null;

    this.analyseService.executerScission(this.buildScissionRequest()).subscribe({
      next: (result) => {
        this.scissionValidationResult = result;
        this.successMessage = result.message || 'Split executed successfully.';
        this.executingScission = false;
        this.loadRecommandations();
        this.loadGroupes();
      },
      error: (err) => {
        this.error = err.message;
        this.executingScission = false;
      }
    });
  }

  private buildFusionRequest(): FusionGroupeRequest {
    const commentaire = this.fusionForm.commentaire.trim();

    return {
      groupeSourceId: this.selectedFusionRec!.idGroupe,
      groupeCibleId: Number(this.fusionForm.groupeCibleId),
      transfererEtudiants: this.fusionForm.transfererEtudiants,
      transfererFormateurs: this.fusionForm.transfererFormateurs,
      transfererObjectifs: this.fusionForm.transfererObjectifs,
      transfererReglements: this.fusionForm.transfererReglements,
      commentaire: commentaire || undefined,
      declenchePar: this.fusionForm.declenchePar || undefined
    };
  }

  private buildScissionRequest(): ScissionGroupeRequest {
    const commentaire = this.scissionForm.commentaire.trim();

    return {
      groupeSourceId: this.selectedScissionRec!.idGroupe,
      groupeCibleId: Number(this.scissionForm.groupeCibleId),
      nombreEtudiantsADeplacer: Number(this.scissionForm.nombreEtudiantsADeplacer),
      transfererEtudiants: this.scissionForm.transfererEtudiants,
      transfererFormateurs: this.scissionForm.transfererFormateurs,
      transfererObjectifs: this.scissionForm.transfererObjectifs,
      transfererReglements: this.scissionForm.transfererReglements,
      commentaire: commentaire || undefined,
      declenchePar: this.scissionForm.declenchePar || undefined
    };
  }
}
