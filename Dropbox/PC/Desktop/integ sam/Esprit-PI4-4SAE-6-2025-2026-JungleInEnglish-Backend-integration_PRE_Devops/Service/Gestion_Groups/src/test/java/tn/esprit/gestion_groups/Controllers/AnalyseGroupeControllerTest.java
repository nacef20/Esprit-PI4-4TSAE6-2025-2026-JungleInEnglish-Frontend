package tn.esprit.gestion_groups.Controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.gestion_groups.Services.IAnalyseGroupeService;
import tn.esprit.gestion_groups.dto.DashboardStatsDTO;
import tn.esprit.gestion_groups.dto.ChargePlanningGroupeDTO;
import tn.esprit.gestion_groups.dto.ScoreSanteGroupeDTO;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalyseGroupeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAnalyseGroupeService analyseService;

    @InjectMocks
    private AnalyseGroupeController analyseGroupeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyseGroupeController).build();
    }

    @Test
    void getChargePlanningGroupe_shouldReturnGroupChargePayload() throws Exception {
        ChargePlanningGroupeDTO dto = ChargePlanningGroupeDTO.builder()
                .idGroupe(12L)
                .codeGroupe("A1-MATIN-01")
                .nomGroupe("Groupe A1 Matin")
                .weekNumber(16)
                .year(2026)
                .heuresPlanifiees(28.5)
                .seuilAlerteHeures(35.0)
                .tauxCharge(81.43)
                .surcharge(false)
                .niveauCharge("ELEVEE")
                .message("Le groupe reste sous le seuil hebdomadaire avec 28.50 h planifiées.")
                .build();

        when(analyseService.getChargePlanningGroupe(eq(12L), any(LocalDate.class))).thenReturn(dto);

        mockMvc.perform(get("/analyse/groupes/12/planning-charge")
                        .param("date", "2026-04-14")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idGroupe").value(12))
                .andExpect(jsonPath("$.codeGroupe").value("A1-MATIN-01"))
                .andExpect(jsonPath("$.heuresPlanifiees").value(28.5))
                .andExpect(jsonPath("$.surcharge").value(false));
    }

    @Test
    void getGlobalStats_shouldReturnDashboardStats() throws Exception {
        DashboardStatsDTO dto = DashboardStatsDTO.builder()
                .totalGroupesActifs(3L)
                .totalEtudiantsAffectes(33L)
                .totalFormateursActifs(2L)
                .tauxRemplissageMoyen(22.2)
                .groupesSousRemplis(2L)
                .groupesQuasiPleins(0L)
                .transfertsCeMois(73L)
                .objectifsEnCours(0L)
                .ratioEtudiantsParFormateur(16.5)
                .tendance("STABLE")
                .build();

        when(analyseService.getGlobalStats()).thenReturn(dto);

        mockMvc.perform(get("/analyse/stats/global").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGroupesActifs").value(3))
                .andExpect(jsonPath("$.tendance").value("STABLE"));
    }

    @Test
    void getScoresSante_shouldReturnHealthScores() throws Exception {
        ScoreSanteGroupeDTO score = new ScoreSanteGroupeDTO(1L, "Alpha", "A1", 20.0, 15.0, 10.0, 5.0, 50.0);
        when(analyseService.calculerScoresSante(30)).thenReturn(List.of(score));

        mockMvc.perform(get("/analyse/sante/scores").param("joursReference", "30").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nom").value("Alpha"))
                .andExpect(jsonPath("$[0].scoreTotal").value(50.0));
    }
}