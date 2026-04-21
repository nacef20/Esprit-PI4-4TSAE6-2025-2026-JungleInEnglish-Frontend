package tn.esprit.quiz_microservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuizRequest {

    private List<AnswerEntry> answers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerEntry {
        private Long questionId;
        private Long choiceId;
    }
}
