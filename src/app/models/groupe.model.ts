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
  ANNULE = 'ANNULE'
}
