export interface ImpactTransfertDTO {
  groupePrecedentId: number;
  raisonChangement: RaisonChangement;
  initiePar: InitiateurChangement;
  nombreTransferts: number;
  impactMoyenScore: number;
  nombreAmeliorations: number;
  nombreDegradations: number;
  nombreNeutres: number;
}

export interface GroupeDestinationDTO {
  idGroupe: number;
  nomGroupe: string;
  nombreEtudiantsRecus: number;
  ameliorationMoyenne: number;
  ameliorationMax: number;
  ameliorationMin: number;
}

export interface ScoreSanteGroupeDTO {
  idGroupe: number;
  nom: string;
  codeGroupe: string;
  scoreTauxRemplissage: number;
  scoreObjectifs: number;
  scoreReglements: number;
  scoreStabilite: number;
  scoreTotal: number;
  niveauSante: 'CRITIQUE' | 'FAIBLE' | 'MOYEN' | 'BON';
}

export interface RecommandationGroupeDTO {
  idGroupe: number;
  nom: string;
  codeGroupe: string;
  capaciteActuelle: number;
  capaciteMin: number;
  capaciteMax: number;
  tauxRemplissage: number;
  objectifsEnCours: number;
  chargeHoraireTotale: number;
  typeRecommandation: 'FUSION' | 'SCISSION';
  raisonRecommandation: string;
}

export interface FusionGroupeRequest {
  groupeSourceId: number;
  groupeCibleId: number;
  transfererEtudiants: boolean;
  transfererFormateurs: boolean;
  transfererObjectifs: boolean;
  transfererReglements: boolean;
  commentaire?: string;
  declenchePar?: number;
}

export interface FusionResultatDTO {
  groupeSourceId: number;
  groupeSourceCode: string;
  groupeCibleId: number;
  groupeCibleCode: string;
  nombreEtudiantsTransferes: number;
  nombreFormateursTransferes: number;
  nombreObjectifsTransferes: number;
  nombreReglementsTransferes: number;
  etudiantsNonTransferes: number[];
  statutGroupeSource?: string;
  capaciteActuelleGroupeCible: number;
  capaciteMaxGroupeCible: number;
  dateFusion: string;
  statut: 'SUCCES' | 'PARTIEL' | 'ECHEC' | 'POSSIBLE';
  message: string;
}

export interface ScissionGroupeRequest {
  groupeSourceId: number;
  groupeCibleId: number;
  nombreEtudiantsADeplacer: number;
  transfererEtudiants: boolean;
  transfererFormateurs: boolean;
  transfererObjectifs: boolean;
  transfererReglements: boolean;
  commentaire?: string;
  declenchePar?: number;
}

export interface ScissionResultatDTO {
  groupeSourceId: number;
  groupeSourceCode: string;
  groupeCibleId: number;
  groupeCibleCode: string;
  nombreEtudiantsTransferes: number;
  nombreFormateursTransferes: number;
  nombreObjectifsTransferes: number;
  nombreReglementsTransferes: number;
  etudiantsNonTransferes: number[];
  statutGroupeSource?: string;
  statutGroupeCible?: string;
  capaciteActuelleGroupeSource: number;
  capaciteMaxGroupeSource?: number;
  capaciteActuelleGroupeCible: number;
  capaciteMaxGroupeCible?: number;
  dateScission: string;
  statut: 'SUCCES' | 'PARTIEL' | 'ECHEC' | 'POSSIBLE';
  message: string;
}

export enum InitiateurChangement {
  SYSTEME = 'SYSTEME',
  ADMIN = 'ADMIN',
  ETUDIANT = 'ETUDIANT',
  PROFESSEUR = 'PROFESSEUR'
}

export enum RaisonChangement {
  PROGRESSION = 'PROGRESSION',
  DEROGATION = 'DEROGATION',
  INCOMPATIBILITE_HORAIRE = 'INCOMPATIBILITE_HORAIRE',
  DEMANDE_ETUDIANT = 'DEMANDE_ETUDIANT',
  REEQUILIBRAGE = 'REEQUILIBRAGE'
}

export interface DashboardStatsDTO {
  totalGroupesActifs: number;
  totalEtudiantsAffectes: number;
  totalFormateursActifs: number;
  tauxRemplissageMoyen: number;
  groupesSousRemplis: number;
  groupesQuasiPleins: number;
  transfertsCeMois: number;
  objectifsEnCours: number;
  ratioEtudiantsParFormateur: number;
  tendance: 'STABLE' | 'CROISSANCE' | 'DECROISSANCE';
}

export interface ChargePlanningGroupeDTO {
  idGroupe: number;
  codeGroupe: string;
  nomGroupe: string;
  weekNumber: number;
  year: number;
  heuresPlanifiees: number;
  seuilAlerteHeures: number;
  tauxCharge: number;
  surcharge: boolean;
  niveauCharge: 'AUCUNE' | 'MODEREE' | 'ELEVEE' | 'CRITIQUE';
  message: string;
}
