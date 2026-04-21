package tn.esprit.gestion_groups.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_groups.dto.PlanEquilibrageDTO;
import tn.esprit.gestion_groups.Services.IEquilibrageService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/equilibrage")
public class EquilibrageController {

    private final IEquilibrageService equilibrageService;

    /**
     * GET /equilibrage/niveaux
     * Liste des niveaux éligibles (au moins 2 groupes actifs).
     */
    @GetMapping("/niveaux")
    public List<Long> getNiveauxEligibles() {
        return equilibrageService.getNiveauxEligibles();
    }

    /**
     * GET /equilibrage/plan?niveauId=2&seuil=2
     * Génère un plan d'équilibrage (SIMULATION — aucune modification).
     */
    @GetMapping("/plan")
    public PlanEquilibrageDTO genererPlan(
            @RequestParam Long niveauId,
            @RequestParam(defaultValue = "2") int seuil) {
        return equilibrageService.genererPlanEquilibrage(niveauId, seuil);
    }

    /**
     * POST /equilibrage/executer?niveauId=2&seuil=2
     * Exécute le plan d'équilibrage — IRRÉVERSIBLE.
     */
    @PostMapping("/executer")
    public PlanEquilibrageDTO executerPlan(
            @RequestParam Long niveauId,
            @RequestParam(defaultValue = "2") int seuil) {
        return equilibrageService.executerPlanEquilibrage(niveauId, seuil);
    }
}
