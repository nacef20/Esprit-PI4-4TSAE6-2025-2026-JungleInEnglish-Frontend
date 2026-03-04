export interface Planning {
  idPlanningSession?: number;
  datePlanning: Date | string;
  statut?: string;
  observations?: string;
  groupeId?: number;
  enseignantId?: number;
  matiereId?: number;
  salleId?: number;
  creneauId?: number;
  salle?: {
    id: number;
    nom_salle?: string;
    capacite?: number;
  };
  creneau?: {
    idCreneauHoraire: number;
    heureDebut?: string;
    heureFin?: string;
    jourSemaine?: string;
  };
  createdAt?: Date | string;
  updatedAt?: Date | string;
  createdBy?: string;
  updatedBy?: string;
  
  // Propriétés optionnelles pour compatibilité
  title?: string;
  description?: string;
  startDate?: Date | string;
  endDate?: Date | string;
  type?: PlanningType;
  status?: PlanningStatus;
  participants?: string[];
  location?: string;
}

export enum PlanningType {
  COURSE = 'COURSE',
  EXAM = 'EXAM',
  EVENT = 'EVENT',
  MEETING = 'MEETING',
  OTHER = 'OTHER'
}

export enum PlanningStatus {
  SCHEDULED = 'SCHEDULED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

// =====================================================================
// ADVANCED PLANNING MODELS
// =====================================================================

export interface AssignmentRequest {
  groupeId: number;
  matiereId: number;
  enseignantId: number;
}

export interface PlanningGeneratorRequest {
  startDate: string; // format YYYY-MM-DD
  creneauIds: number[];
  salleIds: number[];
  assignments: AssignmentRequest[];
}

export interface GenerationResult {
  sessionsGenerees: Planning[];
  echecs: string[];
  nombreSucces: number;
  nombreEchecs: number;
}

export interface WeeklyLoadReport {
  weekNumber: number;
  year: number;
  heuresParEnseignant: { [enseignantId: number]: number };
  heuresParGroupe: { [groupeId: number]: number };
  tauxOccupationParSalle: { [salleId: number]: number };
  alertes: string[];
}
