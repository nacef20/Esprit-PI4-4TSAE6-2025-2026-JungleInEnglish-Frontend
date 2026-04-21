package tn.esprit.recrutement.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Statistiques {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int nbrCandidat;
    private float tauxMoyenMatching;

    @ElementCollection
    private List<String> competencePop;

    @Column(columnDefinition = "TEXT")
    private String tendancesProfils;
} 