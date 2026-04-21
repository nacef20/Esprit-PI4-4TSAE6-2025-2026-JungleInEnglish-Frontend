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
import tn.esprit.gestion_groups.Entites.CategorieReglement;
import tn.esprit.gestion_groups.Entites.ReglementGroupe;
import tn.esprit.gestion_groups.Services.IReglementGroupeService;
import tn.esprit.gestion_groups.dto.ReglementSimpleDTO;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReglementGroupeControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IReglementGroupeService reglementService;

    @InjectMocks
    private ReglementGroupeController reglementController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reglementController).build();
    }

    @Test
    void addReglement_shouldReturnSavedEntity() throws Exception {
        ReglementGroupe reglement = new ReglementGroupe();
        reglement.setIdReglement(1L);
        reglement.setCodeReglement("REG-1");
        when(reglementService.addReglement(any(ReglementGroupe.class))).thenReturn(reglement);

        mockMvc.perform(post("/reglements/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reglement)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeReglement").value("REG-1"));
    }

    @Test
    void getAllReglements_shouldReturnSimpleDtos() throws Exception {
        ReglementSimpleDTO dto = ReglementSimpleDTO.builder().idReglement(1L).codeReglement("REG-1").titre("Ponctualite").build();
        when(reglementService.getAllReglementsSimple()).thenReturn(List.of(dto));

        mockMvc.perform(get("/reglements/all").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titre").value("Ponctualite"));
    }

    @Test
    void getReglementsByCategorie_shouldReturnEntities() throws Exception {
        ReglementGroupe reglement = new ReglementGroupe();
        reglement.setIdReglement(1L);
        reglement.setCategorie(CategorieReglement.COMPORTEMENT);
        when(reglementService.getReglementsByCategorie(CategorieReglement.COMPORTEMENT)).thenReturn(List.of(reglement));

        mockMvc.perform(get("/reglements/categorie/COMPORTEMENT").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categorie").value("COMPORTEMENT"));
    }
}