package tn.esprit.quiz_microservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Choice content is required")
    @Size(min = 1, max = 500, message = "Content must be between 1 and 500 characters")
    private String content;

    @NotNull(message = "You must specify if this choice is correct or not")
    private Boolean isCorrect;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonIgnoreProperties({"choices", "quiz"})
    private Question question;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    @JsonIgnoreProperties({"choices", "quiz"})
    private Attempt attempt;
}
