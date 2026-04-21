// PlanningGeneratorRequest.java
package tn.esprit.gestion_planning.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanningGeneratorRequest {
    // Ce que l'on veut planifier : chaque combo groupe+matiere+enseignant
    private List<AssignmentRequest> assignments;
    // IDs des créneaux disponibles
    private List<Long> creneauIds;
    // IDs des salles disponibles
    private List<Long> salleIds;
    // Lundi de la semaine cible
    private LocalDate startDate;
}