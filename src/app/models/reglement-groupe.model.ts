import { Groupe } from './groupe.model';

export interface ReglementGroupe {
  idReglement?: number;
  codeReglement: string;
  titre: string;
  categorie: CategorieReglement;
  descriptionRegle: string;
  consequence?: string;
  estObligatoire: boolean;
  tolerance?: string;
  sanction?: TypeSanction;
  seuilApplication?: number;
  estActif: boolean;
  dateApplication?: string;
  createdBy?: number;
  createdAt?: string;
  updatedAt?: string;
  groupe?: Groupe;
}

export enum CategorieReglement {
  PRESENCE = 'PRESENCE',
  RETARD = 'RETARD',
  PARTICIPATION = 'PARTICIPATION',
  COMPORTEMENT = 'COMPORTEMENT',
  DEVOIRS = 'DEVOIRS',
  MATERIEL = 'MATERIEL',
  AUTRE = 'AUTRE'
}

export enum TypeSanction {
  AVERTISSEMENT = 'AVERTISSEMENT',
  EXCLUSION_TEMPORAIRE = 'EXCLUSION_TEMPORAIRE',
  EXCLUSION_DEFINITIVE = 'EXCLUSION_DEFINITIVE',
  AUTRE = 'AUTRE'
}
