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
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.GroupeEtudiant;
import tn.esprit.gestion_groups.Entites.StatutAffectation;
import tn.esprit.gestion_groups.Services.IAffectationGroupeService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AffectationGroupeControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IAffectationGroupeService affectationService;

    @InjectMocks
    private AffectationGroupeController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void affecterEtudiant_shouldReturnCreatedAssignment() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);

        GroupeEtudiant affectation = new GroupeEtudiant();
        affectation.setEtudiantId(7L);
        affectation.setGroupe(groupe);
        affectation.setStatut(StatutAffectation.ACTIF);

        when(affectationService.affecterEtudiantAGroupe(eq(7L), eq(1L), eq(70), eq("OK"), eq(3L))).thenReturn(affectation);

        mockMvc.perform(post("/affectations/etudiants/affecter/7/1")
                        .param("scoreEntree", "70")
                        .param("commentaire", "OK")
                        .param("affectePar", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etudiantId").value(7));
    }

    @Test
    void getEtudiantsSansGroupe_shouldReturnIds() throws Exception {
        when(affectationService.getEtudiantsSansGroupe()).thenReturn(List.of(5L, 6L));

        mockMvc.perform(get("/affectations/etudiants/sans-groupe").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(5));
    }
}