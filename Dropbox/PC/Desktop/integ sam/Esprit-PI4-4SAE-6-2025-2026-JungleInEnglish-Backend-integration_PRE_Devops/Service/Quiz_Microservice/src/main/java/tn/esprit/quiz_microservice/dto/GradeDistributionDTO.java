package tn.esprit.quiz_microservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeDistributionDTO {
    private long excellent;  // >= 80%
    private long good;       // 60-79%
    private long average;    // 50-59%
    private long failed;     // < 50%
}
