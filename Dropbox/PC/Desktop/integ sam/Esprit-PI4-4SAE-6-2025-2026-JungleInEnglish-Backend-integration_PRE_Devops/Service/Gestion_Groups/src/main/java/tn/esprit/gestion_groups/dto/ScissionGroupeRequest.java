package tn.esprit.gestion_groups.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScissionGroupeRequest {

    private Long groupeSourceId;
    private Long groupeCibleId;
    private Integer nombreEtudiantsADeplacer;

    private Boolean transfererEtudiants = true;
    private Boolean transfererFormateurs = false;
    private Boolean transfererObjectifs = false;
    private Boolean transfererReglements = false;

    private String commentaire;
    private Long declenchePar;
}