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
import tn.esprit.gestion_groups.Services.IAnomalieService;
import tn.esprit.gestion_groups.dto.AlerteGroupeDTO;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnomalieControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAnomalieService anomalieService;

    @InjectMocks
    private AnomalieController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void scanComplet_shouldReturnAlerts() throws Exception {
        AlerteGroupeDTO alerte = AlerteGroupeDTO.builder()
                .idGroupe(1L)
                .nomGroupe("Alpha")
                .codeGroupe("G1")
                .typeAnomalie("SANS_OBJECTIF")
                .severite("INFO")
                .priorite(3)
                .build();
        when(anomalieService.detecterToutesAnomalies()).thenReturn(List.of(alerte));

        mockMvc.perform(get("/alertes/scan").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].typeAnomalie").value("SANS_OBJECTIF"));
    }

    @Test
    void getResume_shouldReturnSeverityCounts() throws Exception {
        when(anomalieService.detecterToutesAnomalies()).thenReturn(List.of());

        mockMvc.perform(get("/alertes/scan/resume").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}