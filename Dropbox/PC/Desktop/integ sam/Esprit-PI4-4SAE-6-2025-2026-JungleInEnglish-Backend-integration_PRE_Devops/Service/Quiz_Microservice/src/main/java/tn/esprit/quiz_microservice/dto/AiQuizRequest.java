package tn.esprit.quiz_microservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiQuizRequest {

    private String prompt;
    private int numberOfQuestions = 5;
    private String difficulty = "medium"; // easy, medium, hard
    private Long teacherId;
    private Long courseId;

    public String getPrompt() {
        return prompt;
    }
}
