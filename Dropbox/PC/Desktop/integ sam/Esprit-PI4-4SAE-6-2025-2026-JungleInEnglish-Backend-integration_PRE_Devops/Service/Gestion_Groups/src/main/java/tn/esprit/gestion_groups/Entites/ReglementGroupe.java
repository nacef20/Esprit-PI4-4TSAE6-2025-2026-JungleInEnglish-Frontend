package tn.esprit.gestion_groups.Entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reglements_groupe")
public class ReglementGroupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReglement;

    @Column(unique = true, nullable = false)
    private String codeReglement; // ex: "REG-PRESENCE-001"

    private String titre;

    @Enumerated(EnumType.STRING)
    private CategorieReglement categorie; // PRESENCE, RETARD, PARTICIPATION, COMPORTEMENT, DEVOIRS, MATERIEL, AUTRE

    @Column(length = 1000)
    private String descriptionRegle;

    @Column(length = 500)
    private String consequence;

    private Boolean estObligatoire = true;

    private String tolerance; // ex: "2 retards tolérés"

    @Enumerated(EnumType.STRING)
    private TypeSanction sanction; // AVERTISSEMENT, EXCLUSION_TEMPORAIRE, EXCLUSION_DEFINITIVE, AUTRE

    private Integer seuilApplication; // nombre de fois avant sanction

    private Boolean estActif = true;

    private LocalDateTime dateApplication;

    private Long createdBy; // user_id

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Relation ManyToOne avec Groupe
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id")
    @JsonIgnore
    private Groupe groupe;

    public ReglementGroupe() {
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