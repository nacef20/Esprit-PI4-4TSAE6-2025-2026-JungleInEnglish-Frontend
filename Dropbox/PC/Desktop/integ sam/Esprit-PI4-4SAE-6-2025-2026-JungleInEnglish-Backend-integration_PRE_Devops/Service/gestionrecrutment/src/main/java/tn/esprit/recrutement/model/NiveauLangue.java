package tn.esprit.recrutement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NiveauLangue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomLangue;

    @Enumerated(EnumType.STRING)
    private Niveau niveauLangue;

    @ManyToOne
    @JoinColumn(name = "cv_id")
    private CV cv;

    public enum Niveau {
        A1, A2, B1, B2, C1, C2
    }
} 