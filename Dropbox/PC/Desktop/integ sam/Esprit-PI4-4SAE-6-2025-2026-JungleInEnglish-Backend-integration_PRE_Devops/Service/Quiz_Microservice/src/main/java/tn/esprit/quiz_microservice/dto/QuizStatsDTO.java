package tn.esprit.quiz_microservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizStatsDTO {
    private Long quizId;
    private String quizTitle;
    private double avgScore;
    private double avgPercentage;
    private long totalAttempts;
    private long passedCount;
    private double passRate;
}
