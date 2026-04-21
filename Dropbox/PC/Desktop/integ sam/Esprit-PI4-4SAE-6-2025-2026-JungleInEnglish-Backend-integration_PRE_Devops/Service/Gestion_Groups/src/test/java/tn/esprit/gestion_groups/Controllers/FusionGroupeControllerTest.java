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
import tn.esprit.gestion_groups.Services.IFusionGroupeService;
import tn.esprit.gestion_groups.dto.FusionGroupeRequest;
import tn.esprit.gestion_groups.dto.FusionResultatDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FusionGroupeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IFusionGroupeService fusionService;

    @InjectMocks
    private FusionGroupeController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void validerFusion_shouldReturnResult() throws Exception {
        FusionResultatDTO dto = FusionResultatDTO.builder().statut("POSSIBLE").build();
        when(fusionService.validerFusion(any(FusionGroupeRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/fusion/valider")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupeSourceId\":1,\"groupeCibleId\":2,\"transfererEtudiants\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("POSSIBLE"));
    }
}
