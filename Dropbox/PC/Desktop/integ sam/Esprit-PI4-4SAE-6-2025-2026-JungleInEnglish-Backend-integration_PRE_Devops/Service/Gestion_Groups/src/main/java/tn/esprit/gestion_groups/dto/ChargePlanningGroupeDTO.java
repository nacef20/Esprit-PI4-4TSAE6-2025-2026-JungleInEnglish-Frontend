package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargePlanningGroupeDTO {

    private Long idGroupe;
    private String codeGroupe;
    private String nomGroupe;
    private Integer weekNumber;
    private Integer year;
    private Double heuresPlanifiees;
    private Double seuilAlerteHeures;
    private Double tauxCharge;
    private Boolean surcharge;
    private String niveauCharge;
    private String message;
}