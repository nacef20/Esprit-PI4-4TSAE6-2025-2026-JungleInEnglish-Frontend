package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommandationGroupeDTO {

    private Long idGroupe;
    private String nom;
    private String codeGroupe;

    private Integer capaciteActuelle;
    private Integer capaciteMin;
    private Integer capaciteMax;
    private Double tauxRemplissage;         // en pourcentage

    private Long objectifsEnCours;          // charge pédagogique active
    private Long chargeHoraireTotale;       // sum(dureeEstimeeHeures) des objectifs EN_COURS

    private String typeRecommandation;      // "FUSION" ou "SCISSION"
    private String raisonRecommandation;    // explication lisible
}
