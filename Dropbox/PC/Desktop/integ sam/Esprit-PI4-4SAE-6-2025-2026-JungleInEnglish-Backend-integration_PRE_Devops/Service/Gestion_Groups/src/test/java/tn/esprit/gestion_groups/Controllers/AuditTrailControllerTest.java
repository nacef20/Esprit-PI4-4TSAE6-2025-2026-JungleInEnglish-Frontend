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
import tn.esprit.gestion_groups.Services.IAuditTrailService;
import tn.esprit.gestion_groups.dto.RetentionGroupeDTO;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuditTrailControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAuditTrailService auditTrailService;

    @InjectMocks
    private AuditTrailController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getStatistiquesRetention_shouldReturnRows() throws Exception {
        RetentionGroupeDTO dto = RetentionGroupeDTO.builder()
                .idGroupe(1L)
                .nomGroupe("Alpha")
                .totalAffectations(10L)
                .tauxRetention(70.0)
                .build();
        when(auditTrailService.getStatistiquesRetention(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/audit/retention").param("seuil", "1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tauxRetention").value(70.0));
    }
}
