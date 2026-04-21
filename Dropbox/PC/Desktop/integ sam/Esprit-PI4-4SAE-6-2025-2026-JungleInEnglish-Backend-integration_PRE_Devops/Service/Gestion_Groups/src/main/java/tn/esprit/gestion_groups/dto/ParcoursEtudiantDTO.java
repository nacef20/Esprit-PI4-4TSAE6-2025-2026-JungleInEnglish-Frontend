package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ParcoursEtudiantDTO {

    private Long etudiantId;

    // Étape dans le parcours
    private Long idGroupe;
    private String nomGroupe;
    private String codeGroupe;

    private LocalDate dateAffectation;
    private LocalDate dateSortie;
    private String statut;          // ACTIF, SUSPENDU, SORTI

    private Integer scoreEntree;
    private Integer scoreApres;     // du HistoriqueGroupe

    private String raisonChangement; // PROGRESSION, DEROGATION, etc.
    private String commentaire;

    private int dureeSejour;        // en jours (calculé)
    private int evolutionScore;     // scoreApres - scoreEntree (calculé)
}
