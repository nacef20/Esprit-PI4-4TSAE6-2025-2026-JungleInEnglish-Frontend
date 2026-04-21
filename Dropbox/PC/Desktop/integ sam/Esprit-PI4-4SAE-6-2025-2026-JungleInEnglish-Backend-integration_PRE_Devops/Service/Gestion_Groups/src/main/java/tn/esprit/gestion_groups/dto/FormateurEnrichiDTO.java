package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.gestion_groups.Entites.RoleFormateur;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormateurEnrichiDTO {
    private Long formateurId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private RoleFormateur role;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean estActif;
    private String matiere;
    private Integer heuresParSemaine;
    private String commentaire;
}