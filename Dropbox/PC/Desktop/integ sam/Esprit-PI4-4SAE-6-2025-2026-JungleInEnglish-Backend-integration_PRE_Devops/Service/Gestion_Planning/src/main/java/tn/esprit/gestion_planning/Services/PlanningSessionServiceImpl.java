package tn.esprit.gestion_planning.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_planning.Entites.CreneauHoraire;
import tn.esprit.gestion_planning.Entites.PlanningSession;
import tn.esprit.gestion_planning.Entites.Salle;
import tn.esprit.gestion_planning.Repositories.CreneauRepository;
import tn.esprit.gestion_planning.Repositories.PlanningSessionRepository;
import tn.esprit.gestion_planning.Repositories.SalleRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class PlanningSessionServiceImpl implements IPlanningSessionService {

    private final PlanningSessionRepository planningSessionRepository;
    private final SalleRepository salleRepository;
    private final CreneauRepository creneauRepository;

    @Override
    public PlanningSession addPlanningSession(PlanningSession planningSession) {
        if (planningSession.getSalle() != null && planningSession.getSalle().getId() != null) {
            Salle salle = salleRepository.findById(planningSession.getSalle().getId())
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée avec id: " + planningSession.getSalle().getId()));
            planningSession.setSalle(salle);
        }

        if (planningSession.getCreneau() != null && planningSession.getCreneau().getIdCreneauHoraire() != null) {
            CreneauHoraire creneau = creneauRepository.findById(planningSession.getCreneau().getIdCreneauHoraire())
                    .orElseThrow(() -> new RuntimeException("Créneau non trouvé avec id: " + planningSession.getCreneau().getIdCreneauHoraire()));
            planningSession.setCreneau(creneau);
        }

        planningSession.setCreatedAt(LocalDateTime.now());
        return planningSessionRepository.save(planningSession);
    }

    @Override
    public PlanningSession updatePlanningSession(PlanningSession planningSession) {
        if (planningSession.getSalle() != null && planningSession.getSalle().getId() != null) {
            Salle salle = salleRepository.findById(planningSession.getSalle().getId())
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
            planningSession.setSalle(salle);
        }
        if (planningSession.getCreneau() != null && planningSession.getCreneau().getIdCreneauHoraire() != null) {
            CreneauHoraire creneau = creneauRepository.findById(planningSession.getCreneau().getIdCreneauHoraire())
                    .orElseThrow(() -> new RuntimeException("Créneau non trouvé"));
            planningSession.setCreneau(creneau);
        }
        planningSession.setUpdatedAt(LocalDateTime.now());
        return planningSessionRepository.save(planningSession);
    }

    @Override
    public List<PlanningSession> getAllPlanningSessions() {
        return planningSessionRepository.findAll();
    }

    @Override
    public PlanningSession getPlanningSessionById(Long idPlanningSession) {
        return planningSessionRepository.findById(idPlanningSession).orElse(null);
    }

    @Override
    public void deletePlanningSession(Long idPlanningSession) {
        planningSessionRepository.deleteById(idPlanningSession);
    }
}