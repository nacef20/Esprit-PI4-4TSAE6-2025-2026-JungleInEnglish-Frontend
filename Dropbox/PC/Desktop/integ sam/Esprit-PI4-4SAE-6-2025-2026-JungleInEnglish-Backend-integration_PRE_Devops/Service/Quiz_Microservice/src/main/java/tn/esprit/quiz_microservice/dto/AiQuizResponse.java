package tn.esprit.quiz_microservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiQuizResponse {

    private String title;
    private String description;
    private int duration;
    private List<AiQuestion> questions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiQuestion {
        private String content;
        private int points;
        private List<AiChoice> choices;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiChoice {
        private String content;

        @JsonProperty("isCorrect")
        private boolean isCorrect;
    }
}
