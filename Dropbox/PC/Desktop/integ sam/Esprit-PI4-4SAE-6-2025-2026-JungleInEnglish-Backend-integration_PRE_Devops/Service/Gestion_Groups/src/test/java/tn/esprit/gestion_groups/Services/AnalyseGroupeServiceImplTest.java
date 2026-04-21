package tn.esprit.gestion_groups.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.StatutGroupe;
import tn.esprit.gestion_groups.Repositories.GroupeRepository;
import tn.esprit.gestion_groups.Repositories.HistoriqueGroupeRepository;
import tn.esprit.gestion_groups.clients.PlanningFeignClient;
import tn.esprit.gestion_groups.dto.DashboardStatsDTO;
import tn.esprit.gestion_groups.dto.ChargePlanningGroupeDTO;
import tn.esprit.gestion_groups.dto.RecommandationGroupeDTO;
import tn.esprit.gestion_groups.dto.ScoreSanteGroupeDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyseGroupeServiceImplTest {

    @Mock
    private HistoriqueGroupeRepository historiqueRepository;

    @Mock
    private GroupeRepository groupeRepository;

    @Mock
    private GroupeHealthScoreService groupeHealthScoreService;

    @Mock
    private PlanningFeignClient planningFeignClient;

    @InjectMocks
    private AnalyseGroupeServiceImpl analyseGroupeService;

    @Test
    void getChargePlanningGroupe_shouldBuildGroupCentricResponseFromPlanningLoad() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(12L);
        groupe.setCodeGroupe("A1-MATIN-01");
        groupe.setNom("Groupe A1 Matin");

        PlanningFeignClient.WeeklyLoadReportDTO weeklyLoad = new PlanningFeignClient.WeeklyLoadReportDTO();
        weeklyLoad.setWeekNumber(16);
        weeklyLoad.setYear(2026);
        Map<Long, Double> heuresParGroupe = new HashMap<>();
        heuresParGroupe.put(12L, 40.0);
        weeklyLoad.setHeuresParGroupe(heuresParGroupe);

        when(groupeRepository.findById(12L)).thenReturn(Optional.of(groupe));
        when(planningFeignClient.getWeeklyLoad(any())).thenReturn(weeklyLoad);

        ChargePlanningGroupeDTO result = analyseGroupeService.getChargePlanningGroupe(12L, LocalDate.of(2026, 4, 14));

        assertEquals(12L, result.getIdGroupe());
        assertEquals("A1-MATIN-01", result.getCodeGroupe());
        assertEquals(16, result.getWeekNumber());
        assertEquals(2026, result.getYear());
        assertEquals(40.0, result.getHeuresPlanifiees());
        assertEquals(35.0, result.getSeuilAlerteHeures());
        assertTrue(result.getSurcharge());
        assertEquals("CRITIQUE", result.getNiveauCharge());
    }

    @Test
    void getGlobalStats_shouldMapDashboardRow() {
        when(groupeRepository.getStatistiquesGlobales(anyList(), any(LocalDateTime.class)))
                .thenReturn(java.util.Collections.<Object[]>singletonList(new Object[]{3L, 33L, 2L, 22.2, 2L, 0L, 73L, 0L}));

        DashboardStatsDTO result = analyseGroupeService.getGlobalStats();

        assertEquals(3L, result.getTotalGroupesActifs());
        assertEquals(33L, result.getTotalEtudiantsAffectes());
        assertEquals(2L, result.getTotalFormateursActifs());
        assertEquals(16.5, result.getRatioEtudiantsParFormateur());
        assertEquals("STABLE", result.getTendance());
    }

    @Test
    void calculerScoresSante_shouldSortAndMapRows() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setNom("Alpha");
        groupe.setCodeGroupe("A1");
        groupe.setCapaciteActuelle(18);
        groupe.setCapaciteMax(20);

        when(groupeRepository.getRawDataPourScoreSante(any(), any(), anyList()))
            .thenReturn(java.util.Collections.<Object[]>singletonList(new Object[]{groupe, 3L, 4L, 2L, 2L, 1L}));

        List<ScoreSanteGroupeDTO> result = analyseGroupeService.calculerScoresSante(30);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getIdGroupe());
        assertTrue(result.get(0).getScoreTotal() > 0);
    }

    @Test
    void getRecommandationsFusion_shouldFilterFusionCandidates() {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(2L);
        groupe.setNom("Beta");
        groupe.setCodeGroupe("B1");
        groupe.setCapaciteActuelle(5);
        groupe.setCapaciteMin(8);
        groupe.setCapaciteMax(20);

        when(groupeRepository.getGroupesCandidatsFusionScission(any(), anyList()))
            .thenReturn(java.util.Collections.<Object[]>singletonList(new Object[]{groupe, 1L, 12L}));

        List<RecommandationGroupeDTO> result = analyseGroupeService.getRecommandationsFusion();

        assertEquals(1, result.size());
        assertEquals("FUSION", result.get(0).getTypeRecommandation());
    }
}