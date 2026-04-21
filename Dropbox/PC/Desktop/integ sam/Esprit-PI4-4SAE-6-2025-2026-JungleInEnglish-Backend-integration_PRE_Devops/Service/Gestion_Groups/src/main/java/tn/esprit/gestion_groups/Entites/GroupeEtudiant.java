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
@Table(
    name = "groupe_etudiants",
    uniqueConstraints = {
        // Un étudiant ne peut être ACTIF que dans un seul groupe à la fois
        // (géré en logique métier, pas en contrainte DB car historique gardé)
    }
)
public class GroupeEtudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGroupeEtudiant;

    // FK vers microservice Etudiants (pas de @ManyToOne cross-service)
    @Column(nullable = false)
    private Long etudiantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id", nullable = false)
    @JsonIgnore
    private Groupe groupe;

    @Column(nullable = false)
    private LocalDate dateAffectation;

    private LocalDate dateSortie; // null si encore actif

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAffectation statut; // ACTIF, SUSPENDU, SORTI

    // Score de l'étudiant au moment de l'affectation (pour comparaison future)
    private Integer scoreEntree;

    // Commentaire libre (ex: "Dérogation acceptée par directeur")
    @Column(length = 500)
    private String commentaire;

    // Qui a fait l'affectation
    private Long affectePar; // user_id admin

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public GroupeEtudiant() {}

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
