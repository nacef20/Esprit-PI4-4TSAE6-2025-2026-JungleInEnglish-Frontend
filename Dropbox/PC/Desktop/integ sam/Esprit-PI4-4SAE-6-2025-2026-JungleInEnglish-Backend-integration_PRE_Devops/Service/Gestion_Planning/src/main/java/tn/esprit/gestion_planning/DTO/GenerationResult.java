// GenerationResult.java
package tn.esprit.gestion_planning.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.gestion_planning.Entites.PlanningSession;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationResult {
    private List<PlanningSession> sessionsGenerees;
    private List<String> echecs;
    private int totalPlanifie;
    private int totalEchec;
}