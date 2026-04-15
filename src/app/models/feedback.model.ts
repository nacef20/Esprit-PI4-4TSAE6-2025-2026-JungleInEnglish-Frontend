export type ReactionType = 'INSIGHTFUL' | 'HELPFUL' | 'NEEDS_WORK';
export type ProfessorBadgeTier = 'NONE' | 'ROOKIE' | 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM' | 'LEGEND';

export interface FeedbackRequest {
  studentId: string;     // Keycloak sub (UUID)
  lessonId: number;
  courseId: number;
  professorId: string;   // Keycloak sub of the professor
  comment: string;
  starRating: number;    // 1–5
}

export interface ReactionRequest {
  studentId: string;     // Keycloak sub
  feedbackId: number;
  reactionType: ReactionType;
}

export interface FeedbackResponse {
  id: number;
  studentId: string;     // Keycloak sub
  lessonId: number;
  courseId: number;
  professorId: string;
  comment: string;
  starRating: number;
  createdAt: string;
  qualityScore: number;
  insightfulCount: number;
  helpfulCount: number;
  needsWorkCount: number;
  // local UI state — not from the API
  myReaction?: ReactionType | null;
}

export interface ProfessorBadgeResponse {
  professorId: string;
  badgeType: ProfessorBadgeTier;
  starBadge: ProfessorBadgeTier | string;
  totalScore: number;
}
