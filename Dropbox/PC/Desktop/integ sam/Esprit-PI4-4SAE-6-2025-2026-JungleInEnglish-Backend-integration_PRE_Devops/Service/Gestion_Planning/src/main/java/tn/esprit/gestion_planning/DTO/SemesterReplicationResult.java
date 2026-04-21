package tn.esprit.gestion_planning.DTO;
// SemesterReplicationResult.java

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.gestion_planning.Entites.PlanningSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemesterReplicationResult {

    // Nombre total de semaines traitées
    private int totalSemainesTraitees;

    // Nombre de semaines bloquées (examens)
    private int totalSemainesBloques;

    // Total sessions créées avec succès
    private int totalSessionsCrees;

    // Total sessions ignorées (conflit ou jour férié)
    private int totalSessionsIgnorees;

    // Détail par semaine : lundi -> liste de sessions créées
    private Map<LocalDate, List<PlanningSession>> sessionsParSemaine;

    // Détail des ignorées : lundi -> raison
    private Map<LocalDate, List<String>> raisonsIgnoreesParSemaine;

    // Semaines complètement bloquées
    private List<LocalDate> semainesBloques;
}