package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tn.esprit.gestion_groups.Entites.InitiateurChangement;
import tn.esprit.gestion_groups.Entites.RaisonChangement;

@Getter
@AllArgsConstructor
public class ImpactTransfertDTO {

    private Long groupePrecedentId;         // Groupe source (qui perd des étudiants)
    private RaisonChangement raisonChangement;
    private InitiateurChangement initiePar;
    private Long nombreTransferts;
    private Double impactMoyenScore;        // positif = amélioration, négatif = dégradation
    private Long nombreAmeliorations;       // scoreApres > scoreAvant
    private Long nombreDegradations;        // scoreApres < scoreAvant
    private Long nombreNeutres;             // scoreApres = scoreAvant
}
