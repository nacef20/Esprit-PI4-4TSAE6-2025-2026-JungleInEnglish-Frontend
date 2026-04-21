package tn.esprit.quiz_microservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalAttempts;
    private double globalAvgScore;
    private double globalPassRate;
    private double bestScore;
    private List<QuizStatsDTO> quizStats;
    private GradeDistributionDTO gradeDistribution;
    private List<FailedQuestionDTO> mostFailedQuestions;
}
