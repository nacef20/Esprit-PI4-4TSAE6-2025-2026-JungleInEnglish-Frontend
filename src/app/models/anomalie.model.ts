export interface AlerteGroupeDTO {
  idGroupe: number;
  nomGroupe: string;
  codeGroupe: string;
  typeAnomalie: 'SANS_FORMATEUR' | 'CAPACITE_INCOHERENTE' | 'OBJECTIF_EN_RETARD' | 'SANS_OBJECTIF' | 'REGLEMENT_INACTIF';
  severite: 'CRITIQUE' | 'WARNING' | 'INFO';
  description: string;
  suggestion: string;
  priorite: number;
  objectifId?: number;
  codeObjectif?: string;
  joursRetard?: number;
}

export interface ResumeAnomalies {
  [key: string]: number;
}
