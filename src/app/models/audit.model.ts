export interface ParcoursEtudiantDTO {
  etudiantId: number;
  idGroupe: number;
  nomGroupe: string;
  codeGroupe: string;
  dateAffectation: string;
  dateSortie?: string;
  statut: string;
  scoreEntree?: number;
  scoreApres?: number;
  raisonChangement?: string;
  commentaire?: string;
  dureeSejour: number;
  evolutionScore: number;
}

export interface RetentionGroupeDTO {
  idGroupe: number;
  nomGroupe: string;
  totalAffectations: number;
  dureeMoyenneJours: number;
  nombreSortis: number;
  nombreSuspendus: number;
  nombreActifs: number;
  tauxRetention: number;
}
