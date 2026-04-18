export interface Groupe {
  idGroupe?: number;
  codeGroupe: string;
  nom: string;
  niveauId?: number;
  capaciteMin?: number;
  capaciteMax?: number;
  capaciteActuelle?: number;
  dateDebut?: string;
  dateFin?: string;
  type?: TypeGroupe;
  statut?: StatutGroupe;
  createdAt?: string;
  updatedAt?: string;
}

export interface UtilisateurResume {
  id?: string;
  username?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
}

export interface FormateurEnrichi {
  formateurId: number;
  username?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  role?: string;
  dateDebut?: string;
  dateFin?: string;
  estActif?: boolean;
  matiere?: string;
  heuresParSemaine?: number;
  commentaire?: string;
}

export interface GroupeEnrichi extends Groupe {
  nombreFormateursActifs?: number;
  formateurs?: FormateurEnrichi[];
}

export enum TypeGroupe {
  ETUDIANT = 'ETUDIANT',
  CLASSE = 'CLASSE',
  ADMINISTRATION = 'ADMINISTRATION'
}

export enum StatutGroupe {
  OUVERT = 'OUVERT',
  COMPLET = 'COMPLET',
  EN_COURS = 'EN_COURS',
  TERMINE = 'TERMINE',
  ANNULE = 'ANNULE',
  ARCHIVE = 'ARCHIVE'
}
