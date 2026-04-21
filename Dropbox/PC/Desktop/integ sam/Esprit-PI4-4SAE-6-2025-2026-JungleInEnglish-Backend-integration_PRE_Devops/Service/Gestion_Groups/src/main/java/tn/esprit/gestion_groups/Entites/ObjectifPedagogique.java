package tn.esprit.gestion_groups.Entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "objectifs_pedagogiques")
public class ObjectifPedagogique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idObjectif;

    @Column(unique = true, nullable = false)
    private String codeObjectif; // ex: "OBJ-A1-CONV-001"

    private String titre;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private CompetenceCible competenceCible; // LISTENING, SPEAKING, READING, WRITING, GRAMMAR, VOCABULARY

    @Enumerated(EnumType.STRING)
    private NiveauDifficulte niveauDifficulte; // FACILE, MOYEN, DIFFICILE, TRES_DIFFICILE

    private Integer priorite; // 1-5 (1=critique, 5=secondaire)

    private LocalDate dateDebutPrevue;

    private LocalDate dateFinPrevue;

    private Integer dureeEstimeeHeures;

    @Column(length = 500)
    private String prerequis;

    @Enumerated(EnumType.STRING)
    private StatutObjectif statut; // NON_COMMENCE, EN_COURS, ATTEINT, PARTIELLEMENT_ATTEINT, NON_ATTEINT

    private Integer tauxReussiteGroupe; // 0-100%

    private Integer nombreSeancesNecessaires;

    @Column(length = 500)
    private String supportsPedagogiques;

    @Enumerated(EnumType.STRING)
    private MethodeEvaluation methodeEvaluation; // TEST, ORAL, PROJET, OBSERVATION

    @Column(length = 1000)
    private String commentaireProfesseur;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Relation ManyToOne avec Groupe
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id")
    @JsonIgnore
    private Groupe groupe;

    public ObjectifPedagogique() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}