package tn.esprit.quiz_microservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Float score;

    private Integer totalPoints;

    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    @JsonIgnoreProperties({"questions", "attempts"})
    private Quiz quiz;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "student_name")
    private String studentName;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Choice> choices = new ArrayList<>();
}
