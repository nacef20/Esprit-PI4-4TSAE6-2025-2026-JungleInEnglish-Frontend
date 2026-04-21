package tn.esprit.gestion_planning.Services;

import tn.esprit.gestion_planning.Entites.PlanningSession;

import java.util.List;

public interface IPlanningSessionService {

    PlanningSession addPlanningSession(PlanningSession planningSession);

    PlanningSession updatePlanningSession(PlanningSession planningSession);

    List<PlanningSession> getAllPlanningSessions();

    PlanningSession getPlanningSessionById(Long idPlanningSession);

    void deletePlanningSession(Long idPlanningSession);
}
