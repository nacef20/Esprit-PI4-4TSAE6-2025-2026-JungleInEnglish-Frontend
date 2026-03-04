export interface QuizStats {
  quizId: number;
  quizTitle: string;
  avgScore: number;
  avgPercentage: number;
  totalAttempts: number;
  passedCount: number;
  passRate: number;
}

export interface GradeDistribution {
  excellent: number;
  good: number;
  average: number;
  failed: number;
}

export interface FailedQuestion {
  questionId: number;
  questionContent: string;
  quizTitle: string;
  totalAnswers: number;
  wrongAnswers: number;
  failRate: number;
}

export interface DashboardStats {
  totalAttempts: number;
  globalAvgScore: number;
  globalPassRate: number;
  bestScore: number;
  quizStats: QuizStats[];
  gradeDistribution: GradeDistribution;
  mostFailedQuestions: FailedQuestion[];
}
