import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { forkJoin } from 'rxjs';
import { AffectationGroupeService } from '../../../services/affectation-groupe.service';
import { GroupeService } from '../../../services/groupe.service';
import { GroupeEtudiant, GroupeFormateur, RoleFormateur, RaisonChangement, InitiateurChangement } from '../../../models/affectation.model';
import { Groupe, StatutGroupe } from '../../../models/groupe.model';

@Component({
  selector: 'app-gestion-membre-groupe',
  templateUrl: './gestion-membre-groupe.component.html',
  styleUrls: ['./gestion-membre-groupe.component.scss']
})
export class GestionMembreGroupeComponent implements OnInit, OnChanges {
  @Input() groupeId!: number;
  @Input() groupe: Groupe | null = null;

  etudiants: GroupeEtudiant[] = [];
  formateurs: GroupeFormateur[] = [];
  formateurPrincipal: GroupeFormateur | null = null;
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;
  transferLoading = false;
  transferError: string | null = null;

  showAffectEtudiant = false;
  showAffectFormateur = false;
  showChangeGroupe = false;

  newEtudiant = { id: undefined as number | undefined, score: undefined as number | undefined, comment: '' };
  newFormateur = { id: undefined as number | undefined, role: RoleFormateur.ASSISTANT, matiere: '', hours: 0, comment: '' };
  transferEtudiant = { etudiantId: 0, newGroupeId: undefined as number | undefined, raison: RaisonChangement.PROGRESSION, initie: InitiateurChangement.ADMIN, scoreAvant: undefined as number | undefined, comment: '' };

  roles = Object.values(RoleFormateur);
  raisons = Object.values(RaisonChangement);
  initiateurs = Object.values(InitiateurChangement);
  availableGroups: Groupe[] = [];

  constructor(
    private affectationService: AffectationGroupeService,
    private groupeService: GroupeService
  ) {}

  ngOnInit(): void {
    if (this.groupeId) {
      this.loadData();
    }
    this.loadAvailableGroups();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['groupeId'] && !changes['groupeId'].firstChange) {
      this.loadData();
    }
  }

  loadData(): void {
    this.loading = true;
    this.error = null;
    this.formateurPrincipal = null;

    forkJoin({
      etudiants: this.affectationService.getEtudiantsParGroupe(this.groupeId),
      formateurs: this.affectationService.getFormateursParGroupe(this.groupeId)
    }).subscribe({
      next: ({ etudiants, formateurs }) => {
        this.etudiants = etudiants;
        this.formateurs = formateurs;
        this.formateurPrincipal = formateurs.find(f => f.role === RoleFormateur.PRINCIPAL && f.estActif) ?? null;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error while loading group members';
        this.loading = false;
      }
    });
  }

  isStudentAffectationBlocked(): boolean {
    if (!this.groupe) {
      return false;
    }

    if (this.groupe.statut === StatutGroupe.ANNULE || this.groupe.statut === StatutGroupe.TERMINE) {
      return true;
    }

    if (this.groupe.capaciteMax !== undefined && this.groupe.capaciteActuelle !== undefined) {
      return this.groupe.capaciteActuelle >= this.groupe.capaciteMax;
    }

    return this.groupe.statut === StatutGroupe.COMPLET;
  }

  getStudentAffectationBlockedMessage(): string {
    if (!this.groupe) {
      return '';
    }

    if (this.groupe.statut === StatutGroupe.ANNULE || this.groupe.statut === StatutGroupe.TERMINE) {
      return `This group is ${this.getGroupStatusLabel(this.groupe.statut).toLowerCase()}: student assignment is blocked.`;
    }

    if (this.groupe.capaciteMax !== undefined && this.groupe.capaciteActuelle !== undefined && this.groupe.capaciteActuelle >= this.groupe.capaciteMax) {
      return `This group is full (${this.groupe.capaciteActuelle}/${this.groupe.capaciteMax}).`;
    }

    if (this.groupe.statut === StatutGroupe.COMPLET) {
      return 'This group is full.';
    }

    return '';
  }

  loadAvailableGroups(): void {
    this.groupeService.getAllGroupes().subscribe({
      next: data => this.availableGroups = data.filter(g => g.idGroupe !== this.groupeId)
    });
  }

  onAffectEtudiant(): void {
    if (this.isStudentAffectationBlocked()) {
      this.error = this.getStudentAffectationBlockedMessage();
      return;
    }

    const etudiantId = Number(this.newEtudiant.id);
    const scoreEntree = this.newEtudiant.score === undefined || this.newEtudiant.score === null
      ? undefined
      : Number(this.newEtudiant.score);
    const commentaire = this.newEtudiant.comment.trim();

    if (!Number.isFinite(etudiantId) || etudiantId <= 0) {
      this.error = 'Please enter a valid student ID.';
      return;
    }

    if (scoreEntree !== undefined && !Number.isFinite(scoreEntree)) {
      this.error = 'Please enter a valid entry score.';
      return;
    }

    this.affectationService.getGroupeActuelEtudiant(etudiantId).subscribe({
      next: (groupeActuel) => {
        const groupeActuelId = groupeActuel?.groupe?.idGroupe;

        if (groupeActuelId && groupeActuelId !== this.groupeId) {
          this.error = `This student is already active in group ${groupeActuel.groupe.codeGroupe}. Use "Change Group".`;
          return;
        }

        if (groupeActuelId && groupeActuelId === this.groupeId) {
          this.error = 'This student is already assigned to this group.';
          return;
        }

        this.submitAffectationEtudiant(etudiantId, scoreEntree, commentaire);
      },
      error: () => {
        this.submitAffectationEtudiant(etudiantId, scoreEntree, commentaire);
      }
    });
  }

  private submitAffectationEtudiant(etudiantId: number, scoreEntree?: number, commentaire?: string): void {
    this.affectationService.affecterEtudiant(etudiantId, this.groupeId, {
      scoreEntree,
      commentaire
    }).subscribe({
      next: () => {
        this.showMessage('Student assigned successfully');
        this.showAffectEtudiant = false;
        this.loadData();
      },
      error: err => this.error = err.message
    });
  }

  onSuspendEtudiant(id: number): void {
    this.affectationService.suspendreEtudiant(id).subscribe({
      next: () => { this.showMessage('Student suspended'); this.loadData(); },
      error: err => this.error = err.message
    });
  }

  onDesaffectEtudiant(id: number): void {
    this.affectationService.desaffecterEtudiant(id).subscribe({
      next: () => { this.showMessage('Student unassigned'); this.loadData(); },
      error: err => this.error = err.message
    });
  }

  openChangeGroupe(etudiantId: number): void {
    this.transferEtudiant.etudiantId = etudiantId;
    this.transferEtudiant.newGroupeId = undefined;
    this.error = null;
    this.transferError = null;
    this.showChangeGroupe = true;
  }

  onConfirmTransfer(): void {
    this.transferError = null;

    const nouveauGroupeId = Number(this.transferEtudiant.newGroupeId);

    if (!Number.isFinite(nouveauGroupeId) || nouveauGroupeId <= 0) {
      this.transferError = 'Please choose a valid destination group.';
      return;
    }

    const groupeCible = this.availableGroups.find(g => Number(g.idGroupe) === nouveauGroupeId);
    if (!groupeCible) {
      this.transferError = 'The destination group is unavailable or full.';
      return;
    }

    if (groupeCible.statut === StatutGroupe.ANNULE || groupeCible.statut === StatutGroupe.TERMINE) {
      this.transferError = `Group ${groupeCible.codeGroupe} cannot receive students.`;
      return;
    }

    if (groupeCible.capaciteMax !== undefined && groupeCible.capaciteActuelle !== undefined && groupeCible.capaciteActuelle >= groupeCible.capaciteMax) {
      this.transferError = `Group ${groupeCible.codeGroupe} is full (${groupeCible.capaciteActuelle}/${groupeCible.capaciteMax}).`;
      return;
    }

    this.transferLoading = true;
    this.affectationService.changerGroupe(this.transferEtudiant.etudiantId, nouveauGroupeId, {
      raison: this.transferEtudiant.raison,
      initiePar: this.transferEtudiant.initie,
      scoreAvant: this.transferEtudiant.scoreAvant,
      commentaire: this.transferEtudiant.comment
    }).subscribe({
      next: () => {
        this.showMessage('Group change completed');
        this.showChangeGroupe = false;
        this.transferLoading = false;
        this.loadData();
      },
      error: err => {
        this.transferError = err.message;
        this.transferLoading = false;
      }
    });
  }

  onAffectFormateur(): void {
    if (!this.newFormateur.id) {
      return;
    }

    this.affectationService.affecterFormateur(this.newFormateur.id, this.groupeId, {
      role: this.newFormateur.role,
      matiere: this.newFormateur.matiere,
      heuresParSemaine: this.newFormateur.hours,
      commentaire: this.newFormateur.comment
    }).subscribe({
      next: () => {
        this.showMessage('Instructor assigned successfully');
        this.showAffectFormateur = false;
        this.loadData();
      },
      error: err => this.error = err.message
    });
  }

  onRetirerFormateur(id: number): void {
    this.affectationService.retirerFormateur(id, this.groupeId).subscribe({
      next: () => { this.showMessage('Instructor removed'); this.loadData(); },
      error: err => this.error = err.message
    });
  }

  onChangerRole(id: number, roleselect: any): void {
    this.affectationService.changerRoleFormateur(id, this.groupeId, roleselect.value).subscribe({
      next: () => { this.showMessage('Role updated'); this.loadData(); },
      error: err => this.error = err.message
    });
  }

  showMessage(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => this.successMessage = null, 3000);
  }

  getStatutClass(statut: string): string {
    switch (statut) {
      case 'ACTIF': return 'badge-success';
      case 'SUSPENDU': return 'badge-warning';
      case 'SORTI': return 'badge-danger';
      default: return 'badge-secondary';
    }
  }

  getStatusLabel(status?: string): string {
    const labels: { [key: string]: string } = {
      ACTIF: 'Active',
      SUSPENDU: 'Suspended',
      SORTI: 'Left'
    };
    return status ? (labels[status] || status) : '-';
  }

  getRoleLabel(role?: string): string {
    const labels: { [key: string]: string } = {
      PRINCIPAL: 'Principal',
      ASSISTANT: 'Assistant',
      REMPLACANT: 'Substitute',
      OBSERVATEUR: 'Observer'
    };
    return role ? (labels[role] || role) : '-';
  }

  getReasonLabel(reason?: string): string {
    const labels: { [key: string]: string } = {
      PROGRESSION: 'Progression',
      DEROGATION: 'Exception',
      INCOMPATIBILITE_HORAIRE: 'Schedule conflict',
      DEMANDE_ETUDIANT: 'Student request',
      REEQUILIBRAGE: 'Rebalancing'
    };
    return reason ? (labels[reason] || reason) : '-';
  }

  getInitiatorLabel(initiator?: string): string {
    const labels: { [key: string]: string } = {
      SYSTEME: 'System',
      ADMIN: 'Admin',
      ETUDIANT: 'Student',
      PROFESSEUR: 'Teacher'
    };
    return initiator ? (labels[initiator] || initiator) : '-';
  }

  getGroupStatusLabel(status?: string | null): string {
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

  getRoleClass(role: string): string {
    return role === 'PRINCIPAL' ? 'badge-primary' : 'badge-info';
  }
}