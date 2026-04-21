package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PlanEquilibrageDTO {

    private Long niveauId;
    private int nombreGroupes;
    private int totalEtudiants;
    private int cibleIdeale;          // totalEtudiants / nombreGroupes
    private int seuilTolerance;

    private List<MouvementEquilibrageDTO> mouvements;

    private int totalTransferts;
    private double ecartTypeAvant;    // mesure le déséquilibre avant
    private double ecartTypeApres;    // mesure le déséquilibre après
    private double ameliorationPourcent; // ((avant - apres) / avant) * 100

    private String statut;            // SIMULE, EXECUTE
    private String message;
    private LocalDateTime dateGeneration;
}
