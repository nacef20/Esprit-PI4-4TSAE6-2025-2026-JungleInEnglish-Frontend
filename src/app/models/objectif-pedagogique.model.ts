import { Groupe } from './groupe.model';

export interface ObjectifPedagogique {
  idObjectif?: number;
  codeObjectif: string;
  titre: string;
  description: string;
  competenceCible: CompetenceCible;
  niveauDifficulte: NiveauDifficulte;
  priorite: number;
  dateDebutPrevue?: string;
  dateFinPrevue?: string;
  dureeEstimeeHeures?: number;
  prerequis?: string;
  statut: StatutObjectif;
  tauxReussiteGroupe?: number;
  nombreSeancesNecessaires?: number;
  supportsPedagogiques?: string;
  methodeEvaluation?: MethodeEvaluation;
  commentaireProfesseur?: string;
  createdAt?: string;
  updatedAt?: string;
  groupe?: Groupe;
}

export enum CompetenceCible {
  LISTENING = 'LISTENING',
  SPEAKING = 'SPEAKING',
  READING = 'READING',
  WRITING = 'WRITING',
  GRAMMAR = 'GRAMMAR',
  VOCABULARY = 'VOCABULARY'
}

export enum NiveauDifficulte {
  FACILE = 'FACILE',
  MOYEN = 'MOYEN',
  DIFFICILE = 'DIFFICILE',
  TRES_DIFFICILE = 'TRES_DIFFICILE'
}

export enum StatutObjectif {
  NON_COMMENCE = 'NON_COMMENCE',
  EN_COURS = 'EN_COURS',
  ATTEINT = 'ATTEINT',
  PARTIELLEMENT_ATTEINT = 'PARTIELLEMENT_ATTEINT',
  NON_ATTEINT = 'NON_ATTEINT'
}

export enum MethodeEvaluation {
  TEST = 'TEST',
  ORAL = 'ORAL',
  PROJET = 'PROJET',
  OBSERVATION = 'OBSERVATION'
}
