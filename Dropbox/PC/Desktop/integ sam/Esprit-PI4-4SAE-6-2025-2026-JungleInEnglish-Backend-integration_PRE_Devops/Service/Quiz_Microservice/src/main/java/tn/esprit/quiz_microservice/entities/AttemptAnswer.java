package tn.esprit.quiz_microservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    @JsonIgnoreProperties({"choices"})
    private Attempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonIgnoreProperties({"choices", "quiz"})
    private Question question;

    @ManyToOne
    @JoinColumn(name = "selected_choice_id")
    @JsonIgnoreProperties({"question", "attempt"})
    private Choice selectedChoice;
}
