package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.gestion_groups.Entites.CompetenceCible;
import tn.esprit.gestion_groups.Entites.NiveauDifficulte;
import tn.esprit.gestion_groups.Entites.StatutObjectif;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObjectifSimpleDTO {
    private Long idObjectif;
    private String codeObjectif;
    private String titre;
    private String description;
    private CompetenceCible competenceCible;
    private NiveauDifficulte niveauDifficulte;
    private Integer priorite;
    private LocalDate dateDebutPrevue;
    private LocalDate dateFinPrevue;
    private Integer dureeEstimeeHeures;
    private StatutObjectif statut;
    private Integer tauxReussiteGroupe;
}
