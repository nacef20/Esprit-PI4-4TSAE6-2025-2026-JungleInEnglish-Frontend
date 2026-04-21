package tn.esprit.gestion_planning.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_planning.Entites.PlanningSession;
import tn.esprit.gestion_planning.Services.IPlanningSessionService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/plannings")
public class PlanningSessionController {

    private final IPlanningSessionService planningSessionService;

    // GET /plannings  (ce qu'Angular appelle)
    @GetMapping
    public List<PlanningSession> getAllPlanningSessions() {
        return planningSessionService.getAllPlanningSessions();
    }

    //  GET /plannings/{id}
    @GetMapping("/{id}")
    public PlanningSession getPlanningSessionById(@PathVariable Long id) {
        return planningSessionService.getPlanningSessionById(id);
    }

    // POST /plannings  (ce qu'Angular appelle)
    @PostMapping
    public PlanningSession addPlanningSession(@RequestBody PlanningSession planningSession) {
        return planningSessionService.addPlanningSession(planningSession);
    }

    // PUT /plannings/{id}
    @PutMapping("/{id}")
    public PlanningSession updatePlanningSession(@PathVariable Long id,
                                                 @RequestBody PlanningSession planningSession) {
        planningSession.setIdPlanningSession(id);
        return planningSessionService.updatePlanningSession(planningSession);
    }

    // DELETE /plannings/{id}
    @DeleteMapping("/{id}")
    public void deletePlanningSession(@PathVariable Long id) {
        planningSessionService.deletePlanningSession(id);
    }
}
