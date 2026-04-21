package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupeDestinationDTO {

    private Long idGroupe;
    private String nomGroupe;
    private Long nombreEtudiantsRecus;
    private Double ameliorationMoyenne;     // avg(scoreApres - scoreAvant)
    private Integer ameliorationMax;        // meilleure progression individuelle
    private Integer ameliorationMin;        // pire progression individuelle
}
