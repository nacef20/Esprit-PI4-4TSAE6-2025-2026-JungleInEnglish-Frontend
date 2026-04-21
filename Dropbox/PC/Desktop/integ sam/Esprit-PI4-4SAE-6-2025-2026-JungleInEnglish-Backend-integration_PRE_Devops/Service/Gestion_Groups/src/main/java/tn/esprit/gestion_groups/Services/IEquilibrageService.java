package tn.esprit.gestion_groups.Services;

import tn.esprit.gestion_groups.dto.PlanEquilibrageDTO;

import java.util.List;

public interface IEquilibrageService {

    /** Génère un plan d'équilibrage pour un niveau (simulation) */
    PlanEquilibrageDTO genererPlanEquilibrage(Long niveauId, int seuilTolerance);

    /** Exécute un plan d'équilibrage (IRRÉVERSIBLE) */
    PlanEquilibrageDTO executerPlanEquilibrage(Long niveauId, int seuilTolerance);

    /** Liste les niveaux qui ont suffisamment de groupes pour être équilibrés */
    List<Long> getNiveauxEligibles();
}
