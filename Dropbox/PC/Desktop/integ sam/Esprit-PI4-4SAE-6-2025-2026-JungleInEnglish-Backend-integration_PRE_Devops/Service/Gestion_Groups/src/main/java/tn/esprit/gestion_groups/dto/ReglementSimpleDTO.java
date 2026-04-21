package tn.esprit.gestion_groups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.gestion_groups.Entites.CategorieReglement;
import tn.esprit.gestion_groups.Entites.TypeSanction;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReglementSimpleDTO {
    private Long idReglement;
    private String codeReglement;
    private String titre;
    private CategorieReglement categorie;
    private String descriptionRegle;
    private Boolean estObligatoire;
    private String tolerance;
    private TypeSanction sanction;
    private Boolean estActif;
    private LocalDateTime dateApplication;
}
