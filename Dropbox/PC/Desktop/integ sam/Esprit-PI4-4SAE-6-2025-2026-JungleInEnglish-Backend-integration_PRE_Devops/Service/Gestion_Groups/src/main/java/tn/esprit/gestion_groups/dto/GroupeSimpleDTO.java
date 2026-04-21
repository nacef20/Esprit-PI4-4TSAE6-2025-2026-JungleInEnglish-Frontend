package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.gestion_groups.Entites.StatutGroupe;
import tn.esprit.gestion_groups.Entites.TypeGroupe;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupeSimpleDTO {
    private Long idGroupe;
    private String codeGroupe;
    private String nom;
    private Long niveauId;
    private Integer capaciteMin;
    private Integer capaciteMax;
    private Integer capaciteActuelle;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private TypeGroupe type;
    private StatutGroupe statut;
}
