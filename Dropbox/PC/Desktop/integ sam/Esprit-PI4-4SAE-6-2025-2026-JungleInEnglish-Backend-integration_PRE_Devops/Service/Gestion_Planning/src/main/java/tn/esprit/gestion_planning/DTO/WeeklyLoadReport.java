// WeeklyLoadReport.java
package tn.esprit.gestion_planning.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyLoadReport {
    private int weekNumber;
    private int year;
    // enseignantId -> heures totales
    private Map<Long, Double> heuresParEnseignant;
    // groupeId -> heures totales
    private Map<Long, Double> heuresParGroupe;
    // salleId -> taux occupation (%)
    private Map<Long, Double> tauxOccupationParSalle;
    // Messages d'alerte surcharge
    private List<String> alertes;
}