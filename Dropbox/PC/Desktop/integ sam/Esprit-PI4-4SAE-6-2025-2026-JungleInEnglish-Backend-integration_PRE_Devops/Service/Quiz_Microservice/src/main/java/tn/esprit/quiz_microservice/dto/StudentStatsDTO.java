package tn.esprit.quiz_microservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatsDTO {
    private long totalAttempts;
    private double avgScore;
    private double bestScore;
    private double passRate;
}
