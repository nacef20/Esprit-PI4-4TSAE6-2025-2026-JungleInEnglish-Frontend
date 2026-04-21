package tn.esprit.gestion_groups.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.gestion_groups.Entites.CompetenceCible;
import tn.esprit.gestion_groups.Entites.ObjectifPedagogique;
import tn.esprit.gestion_groups.Entites.StatutObjectif;
import tn.esprit.gestion_groups.Services.IObjectifPedagogiqueService;
import tn.esprit.gestion_groups.dto.ObjectifSimpleDTO;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ObjectifPedagogiqueControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IObjectifPedagogiqueService objectifService;

    @InjectMocks
    private ObjectifPedagogiqueController objectifController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(objectifController).build();
    }

    @Test
    void addObjectif_shouldReturnSavedEntity() throws Exception {
        ObjectifPedagogique objectif = new ObjectifPedagogique();
        objectif.setIdObjectif(1L);
        objectif.setCodeObjectif("OBJ-1");
        when(objectifService.addObjectif(any(ObjectifPedagogique.class))).thenReturn(objectif);

        mockMvc.perform(post("/objectifs/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(objectif)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeObjectif").value("OBJ-1"));
    }

    @Test
    void getAllObjectifs_shouldReturnSimpleDtos() throws Exception {
        ObjectifSimpleDTO dto = ObjectifSimpleDTO.builder().idObjectif(1L).codeObjectif("OBJ-1").titre("Lire").build();
        when(objectifService.getAllObjectifsSimple()).thenReturn(List.of(dto));

        mockMvc.perform(get("/objectifs/all").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titre").value("Lire"));
    }

    @Test
    void getObjectifsByStatut_shouldReturnEntities() throws Exception {
        ObjectifPedagogique objectif = new ObjectifPedagogique();
        objectif.setIdObjectif(1L);
        objectif.setStatut(StatutObjectif.EN_COURS);
        objectif.setCompetenceCible(CompetenceCible.READING);
        when(objectifService.getObjectifsByStatut(StatutObjectif.EN_COURS)).thenReturn(List.of(objectif));

        mockMvc.perform(get("/objectifs/statut/EN_COURS").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statut").value("EN_COURS"));
    }
}