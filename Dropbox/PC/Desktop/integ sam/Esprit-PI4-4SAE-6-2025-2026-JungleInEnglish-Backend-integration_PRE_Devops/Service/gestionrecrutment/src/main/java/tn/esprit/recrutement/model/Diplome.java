package tn.esprit.recrutement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Diplome {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String etablissement;
    private int annee;

    @ManyToOne
    @JoinColumn(name = "cv_id")
    private CV cv;
} 