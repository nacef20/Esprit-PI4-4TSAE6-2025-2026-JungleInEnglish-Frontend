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
public class DashboardStatsDTO {
    private long totalGroupesActifs;
    private long totalEtudiantsAffectes;
    private long totalFormateursActifs;
    private double tauxRemplissageMoyen;
    private long groupesSousRemplis;
    private long groupesQuasiPleins;
    private long transfertsCeMois;
    private long objectifsEnCours;
    
    // Calculés côté Java si nécessaire
    private double ratioEtudiantsParFormateur;
    private String tendance; // "STABLE", "CROISSANCE", "DECROISSANCE"
}
