package tn.esprit.gestion_groups.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.StatutGroupe;
import tn.esprit.gestion_groups.Entites.StatutObjectif;
import tn.esprit.gestion_groups.Repositories.GroupeEtudiantRepository;
import tn.esprit.gestion_groups.Repositories.GroupeRepository;
import tn.esprit.gestion_groups.Repositories.HistoriqueGroupeRepository;
import tn.esprit.gestion_groups.dto.PlanEquilibrageDTO;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquilibrageServiceImplTest {

    @Mock
    private GroupeRepository groupeRepository;

    @Mock
    private GroupeEtudiantRepository groupeEtudiantRepository;

    @Mock
    private HistoriqueGroupeRepository historiqueRepository;

    @Mock
    private GroupeHealthScoreService groupeHealthScoreService;

    @InjectMocks
    private EquilibrageServiceImpl equilibrageService;

    @Test
    void getNiveauxEligibles_shouldDelegateToRepository() {
        when(groupeRepository.findNiveauxAvecPlusieursGroupes(anyList())).thenReturn(List.of(2L));

        assertEquals(List.of(2L), equilibrageService.getNiveauxEligibles());
    }

    @Test
    void genererPlanEquilibrage_shouldReturnSimulationWhenTooFewGroups() {
        when(groupeRepository.getGroupesParNiveauPourEquilibrage(2L, StatutObjectif.EN_COURS, List.of(StatutGroupe.ANNULE, StatutGroupe.TERMINE, StatutGroupe.ARCHIVE)))
                .thenReturn(List.of());

        PlanEquilibrageDTO result = equilibrageService.genererPlanEquilibrage(2L, 2);

        assertEquals("SIMULE", result.getStatut());
        assertEquals(0, result.getMouvements().size());
    }

    @Test
    void executerPlanEquilibrage_shouldReturnExecuteWhenNoMovementNeeded() {
        when(groupeRepository.getGroupesParNiveauPourEquilibrage(2L, StatutObjectif.EN_COURS, List.of(StatutGroupe.ANNULE, StatutGroupe.TERMINE, StatutGroupe.ARCHIVE)))
                .thenReturn(List.of());

        PlanEquilibrageDTO result = equilibrageService.executerPlanEquilibrage(2L, 2);

        assertEquals("EXECUTE", result.getStatut());
    }
}