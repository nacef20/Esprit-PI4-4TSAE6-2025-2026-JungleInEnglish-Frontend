package tn.esprit.gestion_groups.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class FusionResultatDTO {

    private Long groupeSourceId;
    private String groupeSourceCode;

    private Long groupeCibleId;
    private String groupeCibleCode;

    // Ce qui a été transféré
    private Integer nombreEtudiantsTransferes;
    private Integer nombreFormateursTransferes;
    private Integer nombreObjectifsTransferes;
    private Integer nombreReglementsTransferes;

    // Étudiants qui n'ont PAS pu être transférés (groupe cible plein en cours de fusion)
    private List<Long> etudiantsNonTransferes;

    // Statut final
    private String statutGroupeSource;     // "ARCHIVE" après fusion
    private Integer capaciteActuelleGroupeCible;
    private Integer capaciteMaxGroupeCible;

    private LocalDateTime dateFusion;
    private String statut;                 // "SUCCES" | "PARTIEL" | "ECHEC"
    private String message;
}
