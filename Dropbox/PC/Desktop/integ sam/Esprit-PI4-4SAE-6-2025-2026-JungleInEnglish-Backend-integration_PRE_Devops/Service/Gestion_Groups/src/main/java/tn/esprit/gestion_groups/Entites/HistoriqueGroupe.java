package tn.esprit.gestion_groups.Entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "historique_groupe")
public class HistoriqueGroupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistorique;

    private Long etudiantId; // FK vers microservice Etudiants

    private Long groupePrecedentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_nouveau_id")
    @JsonIgnore
    private Groupe groupe;

    private LocalDateTime dateChangement;

    @Enumerated(EnumType.STRING)
    private RaisonChangement raisonChangement; // PROGRESSION, DEROGATION, INCOMPATIBILITE_HORAIRE, DEMANDE_ETUDIANT, REEQUILIBRAGE

    @Enumerated(EnumType.STRING)
    private InitiateurChangement initiePar; // SYSTEME, ADMIN, ETUDIANT, PROFESSEUR

    private Integer scoreAvant;

    private Integer scoreApres;

    @Column(length = 500)
    private String commentaire;

    private Long validateurId; // user_id

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public HistoriqueGroupe() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}