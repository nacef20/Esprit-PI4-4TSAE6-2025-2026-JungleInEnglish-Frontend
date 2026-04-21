package tn.esprit.gestion_groups.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ScissionResultatDTO {

    private Long groupeSourceId;
    private String groupeSourceCode;

    private Long groupeCibleId;
    private String groupeCibleCode;

    private Integer nombreEtudiantsTransferes;
    private Integer nombreFormateursTransferes;
    private Integer nombreObjectifsTransferes;
    private Integer nombreReglementsTransferes;

    private List<Long> etudiantsNonTransferes;

    private String statutGroupeSource;
    private String statutGroupeCible;

    private Integer capaciteActuelleGroupeSource;
    private Integer capaciteMaxGroupeSource;
    private Integer capaciteActuelleGroupeCible;
    private Integer capaciteMaxGroupeCible;

    private LocalDateTime dateScission;
    private String statut;
    private String message;
}