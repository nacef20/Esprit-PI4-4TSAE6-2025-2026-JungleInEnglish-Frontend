package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AlerteGroupeDTO {

    private Long idGroupe;
    private String nomGroupe;
    private String codeGroupe;
    private String typeAnomalie;    // SANS_FORMATEUR, CAPACITE_INCOHERENTE, OBJECTIF_EN_RETARD, SANS_OBJECTIF, REGLEMENT_INACTIF
    private String severite;        // CRITIQUE, WARNING, INFO
    private String description;
    private String suggestion;
    private int priorite;           // 1=CRITIQUE, 2=WARNING, 3=INFO

    // Champs optionnels selon le type
    private Long objectifId;        // si OBJECTIF_EN_RETARD
    private String codeObjectif;    // si OBJECTIF_EN_RETARD
    private Integer joursRetard;    // si OBJECTIF_EN_RETARD
}
