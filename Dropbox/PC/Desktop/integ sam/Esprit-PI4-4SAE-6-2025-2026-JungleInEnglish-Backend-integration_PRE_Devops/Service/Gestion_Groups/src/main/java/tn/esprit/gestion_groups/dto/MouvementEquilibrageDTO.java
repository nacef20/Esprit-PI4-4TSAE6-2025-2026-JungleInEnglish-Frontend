package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MouvementEquilibrageDTO {

    private Long groupeSourceId;
    private String groupeSourceCode;
    private int capaciteAvantSource;

    private Long groupeCibleId;
    private String groupeCibleCode;
    private int capaciteAvantCible;

    private int nombreEtudiantsATransferer;
    private String statut;   // SIMULE, EXECUTE, ECHEC
    private String message;
}
