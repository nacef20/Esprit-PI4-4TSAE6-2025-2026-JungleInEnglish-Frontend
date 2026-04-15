export interface MouvementEquilibrageDTO {
  groupeSourceId: number;
  groupeSourceCode: string;
  capaciteAvantSource: number;
  groupeCibleId: number;
  groupeCibleCode: string;
  capaciteAvantCible: number;
  nombreEtudiantsATransferer: number;
  statut: string;
  message: string;
}

export interface PlanEquilibrageDTO {
  niveauId: number;
  nombreGroupes: number;
  totalEtudiants: number;
  cibleIdeale: number;
  seuilTolerance: number;
  mouvements: MouvementEquilibrageDTO[];
  totalTransferts: number;
  ecartTypeAvant: number;
  ecartTypeApres: number;
  ameliorationPourcent: number;
  statut: 'SIMULE' | 'EXECUTE';
  message: string;
  dateGeneration: string;
}
