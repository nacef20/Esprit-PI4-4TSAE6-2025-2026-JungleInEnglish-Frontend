package tn.esprit.recrutement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Competence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomCompetence;
    private String niveau;

    @ManyToOne
    @JoinColumn(name = "cv_id")
    private CV cv;
} 