package tn.esprit.gestion_groups.Controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.gestion_groups.Services.IEquilibrageService;
import tn.esprit.gestion_groups.dto.PlanEquilibrageDTO;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EquilibrageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IEquilibrageService equilibrageService;

    @InjectMocks
    private EquilibrageController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void genererPlan_shouldReturnPlan() throws Exception {
        PlanEquilibrageDTO dto = PlanEquilibrageDTO.builder().niveauId(2L).statut("SIMULE").mouvements(List.of()).build();
        when(equilibrageService.genererPlanEquilibrage(2L, 2)).thenReturn(dto);

        mockMvc.perform(get("/equilibrage/plan").param("niveauId", "2").param("seuil", "2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("SIMULE"));
    }
}