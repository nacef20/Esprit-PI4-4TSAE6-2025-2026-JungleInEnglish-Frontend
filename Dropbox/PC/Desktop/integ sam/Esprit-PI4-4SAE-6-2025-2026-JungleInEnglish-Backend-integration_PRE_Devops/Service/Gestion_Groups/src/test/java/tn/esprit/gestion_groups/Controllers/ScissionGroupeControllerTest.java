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
import tn.esprit.gestion_groups.Services.IScissionGroupeService;
import tn.esprit.gestion_groups.dto.ScissionGroupeRequest;
import tn.esprit.gestion_groups.dto.ScissionResultatDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScissionGroupeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IScissionGroupeService scissionService;

    @InjectMocks
    private ScissionGroupeController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void executerScission_shouldReturnResult() throws Exception {
        ScissionResultatDTO dto = ScissionResultatDTO.builder().statut("SUCCES").build();
        when(scissionService.scinderGroupes(any(ScissionGroupeRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/scission/executer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupeSourceId\":1,\"groupeCibleId\":2,\"nombreEtudiantsADeplacer\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("SUCCES"));
    }
}
