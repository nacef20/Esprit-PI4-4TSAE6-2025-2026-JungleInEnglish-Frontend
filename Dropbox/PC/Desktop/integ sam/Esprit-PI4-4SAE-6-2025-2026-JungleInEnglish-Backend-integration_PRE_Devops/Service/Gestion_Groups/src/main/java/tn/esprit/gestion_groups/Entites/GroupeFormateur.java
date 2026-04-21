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
@Table(name = "groupe_formateurs")
public class GroupeFormateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGroupeFormateur;

    // FK vers microservice Formateurs / Users
    @Column(nullable = false)
    private Long formateurId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id", nullable = false)
    @JsonIgnore
    private Groupe groupe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleFormateur role; // PRINCIPAL, ASSISTANT, REMPLACANT, OBSERVATEUR

    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin; // null = encore actif dans ce groupe

    private Boolean estActif = true;

    // Matière / module enseigné dans ce groupe (ex: "Grammaire", "Oral")
    private String matiere;

    // Nombre d'heures par semaine prévues
    private Integer heuresParSemaine;

    @Column(length = 500)
    private String commentaire;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public GroupeFormateur() {}

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
