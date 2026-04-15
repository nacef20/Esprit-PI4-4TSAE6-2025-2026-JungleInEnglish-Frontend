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
        this.error = 'Erreur lors du chargement des membres du groupe';
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
      return `Ce groupe est ${this.groupe.statut.toLowerCase()} : l'affectation d'étudiants est bloquée.`;
    }

    if (this.groupe.capaciteMax !== undefined && this.groupe.capaciteActuelle !== undefined && this.groupe.capaciteActuelle >= this.groupe.capaciteMax) {
      return `Ce groupe est complet (${this.groupe.capaciteActuelle}/${this.groupe.capaciteMax}).`;
    }

    if (this.groupe.statut === StatutGroupe.COMPLET) {
      return 'Ce groupe est complet.';
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
      this.error = 'Veuillez renseigner un ID étudiant valide.';
      return;
    }

    if (scoreEntree !== undefined && !Number.isFinite(scoreEntree)) {
      this.error = 'Veuillez renseigner un score d\'entrée valide.';
      return;
    }

    this.affectationService.getGroupeActuelEtudiant(etudiantId).subscribe({
      next: (groupeActuel) => {
        const groupeActuelId = groupeActuel?.groupe?.idGroupe;

        if (groupeActuelId && groupeActuelId !== this.groupeId) {
          this.error = `Cet étudiant est déjà actif dans le groupe ${groupeActuel.groupe.codeGroupe}. Utilisez "Changer Groupe".`;
          return;
        }

        if (groupeActuelId && groupeActuelId === this.groupeId) {
          this.error = 'Cet étudiant est déjà affecté à ce groupe.';
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
        this.showMessage('Étudiant affecté avec succès');
        this.showAffectEtudiant = false;
        this.loadData();
      },
      error: err => this.error = err.message
    });
  }

  onSuspendEtudiant(id: number): void {
    this.affectationService.suspendreEtudiant(id).subscribe({
      next: () => { this.showMessage('Étudiant suspendu'); this.loadData(); },
      error: err => this.error = err.message
    });
  }

  onDesaffectEtudiant(id: number): void {
    this.affectationService.desaffecterEtudiant(id).subscribe({
      next: () => { this.showMessage('Étudiant désaffecté'); this.loadData(); },
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
      this.transferError = 'Veuillez choisir un groupe de destination valide.';
      return;
    }

    const groupeCible = this.availableGroups.find(g => Number(g.idGroupe) === nouveauGroupeId);
    if (!groupeCible) {
      this.transferError = 'Le groupe de destination est indisponible ou complet.';
      return;
    }

    if (groupeCible.statut === StatutGroupe.ANNULE || groupeCible.statut === StatutGroupe.TERMINE) {
      this.transferError = `Le groupe ${groupeCible.codeGroupe} ne peut pas recevoir d'étudiants.`;
      return;
    }

    if (groupeCible.capaciteMax !== undefined && groupeCible.capaciteActuelle !== undefined && groupeCible.capaciteActuelle >= groupeCible.capaciteMax) {
      this.transferError = `Le groupe ${groupeCible.codeGroupe} est complet (${groupeCible.capaciteActuelle}/${groupeCible.capaciteMax}).`;
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
        this.showMessage('Changement de groupe effectué');
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
        this.showMessage('Formateur affecté avec succès');
        this.showAffectFormateur = false;
        this.loadData();
      },
      error: err => this.error = err.message
    });
  }

  onRetirerFormateur(id: number): void {
    this.affectationService.retirerFormateur(id, this.groupeId).subscribe({
      next: () => { this.showMessage('Formateur retiré'); this.loadData(); },
      error: err => this.error = err.message
    });
  }

  onChangerRole(id: number, roleselect: any): void {
    this.affectationService.changerRoleFormateur(id, this.groupeId, roleselect.value).subscribe({
      next: () => { this.showMessage('Rôle mis à jour'); this.loadData(); },
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

  getRoleClass(role: string): string {
    return role === 'PRINCIPAL' ? 'badge-primary' : 'badge-info';
  }
}