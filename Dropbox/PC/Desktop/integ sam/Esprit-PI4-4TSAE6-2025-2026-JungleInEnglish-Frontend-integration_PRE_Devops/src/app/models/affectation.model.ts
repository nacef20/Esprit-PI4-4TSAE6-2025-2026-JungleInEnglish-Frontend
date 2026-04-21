import { Groupe } from './groupe.model';

export interface GroupeEtudiant {
  idGroupeEtudiant: number;
  etudiantId: number;
  groupe: Groupe;
  dateAffectation: string;
  dateSortie?: string;
  statut: StatutAffectation | string;
  scoreEntree?: number;
  commentaire?: string;
  affectePar?: number;
}

export interface GroupeFormateur {
  idGroupeFormateur: number;
  formateurId: number;
  groupe: Groupe;
  role: RoleFormateur | string;
  dateDebut: string;
  dateFin?: string;
  estActif: boolean;
  matiere?: string;
  heuresParSemaine?: number;
  commentaire?: string;
}

export enum StatutAffectation {
  ACTIF = 'ACTIF',
  SUSPENDU = 'SUSPENDU',
  SORTI = 'SORTI'
}

export enum RoleFormateur {
  PRINCIPAL = 'PRINCIPAL',
  ASSISTANT = 'ASSISTANT',
  REMPLACANT = 'REMPLACANT',
  OBSERVATEUR = 'OBSERVATEUR'
}

export enum RaisonChangement {
  PROGRESSION = 'PROGRESSION',
  DEROGATION = 'DEROGATION',
  INCOMPATIBILITE_HORAIRE = 'INCOMPATIBILITE_HORAIRE',
  DEMANDE_ETUDIANT = 'DEMANDE_ETUDIANT',
  REEQUILIBRAGE = 'REEQUILIBRAGE'
}

export enum InitiateurChangement {
  SYSTEME = 'SYSTEME',
  ADMIN = 'ADMIN',
  ETUDIANT = 'ETUDIANT',
  PROFESSEUR = 'PROFESSEUR'
}