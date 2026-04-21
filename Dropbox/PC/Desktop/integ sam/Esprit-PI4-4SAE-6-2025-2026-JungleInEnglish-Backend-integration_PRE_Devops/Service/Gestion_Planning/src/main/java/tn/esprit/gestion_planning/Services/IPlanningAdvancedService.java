// IPlanningAdvancedService.java
package tn.esprit.gestion_planning.Services;

import tn.esprit.gestion_planning.DTO.*;

import java.time.LocalDate;

public interface IPlanningAdvancedService {
    GenerationResult generateWeeklyPlanning(PlanningGeneratorRequest request);
    WeeklyLoadReport analyzeWeeklyLoad(LocalDate anyDayInWeek);
    // Ajouter dans IPlanningAdvancedService.java
    SemesterReplicationResult replicatePlanningSemester(SemesterReplicationRequest request);
}