package tn.esprit.quiz_microservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FailedQuestionDTO {
    private Long questionId;
    private String questionContent;
    private String quizTitle;
    private long totalAnswers;
    private long wrongAnswers;
    private double failRate;
}
