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
import tn.esprit.gestion_groups.Entites.RoleFormateur;
import tn.esprit.gestion_groups.Entites.StatutGroupe;
import tn.esprit.gestion_groups.Entites.TypeGroupe;
import tn.esprit.gestion_groups.Services.IGroupeService;
import tn.esprit.gestion_groups.dto.FormateurEnrichiDTO;
import tn.esprit.gestion_groups.dto.GroupeEnrichiDTO;
import tn.esprit.gestion_groups.dto.GroupeSimpleDTO;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GroupeControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IGroupeService groupeService;

    @InjectMocks
    private GroupeController groupeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupeController).build();
    }

    @Test
    void addGroupe_shouldReturnSavedGroup() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setCodeGroupe("G1");
        groupe.setNom("Groupe 1");
        when(groupeService.addGroupe(any(Groupe.class))).thenReturn(groupe);

        mockMvc.perform(post("/groupes/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeGroupe").value("G1"));
    }

    @Test
    void getAllGroupes_shouldReturnSimpleDtos() throws Exception {
        GroupeSimpleDTO dto = GroupeSimpleDTO.builder().idGroupe(1L).codeGroupe("G1").nom("Groupe 1").build();
        when(groupeService.getAllGroupesSimple()).thenReturn(List.of(dto));

        mockMvc.perform(get("/groupes/all").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codeGroupe").value("G1"));
    }

    @Test
    void updateStatutGroupe_shouldReturnUpdatedGroup() throws Exception {
        Groupe groupe = new Groupe();
        groupe.setIdGroupe(1L);
        groupe.setStatut(StatutGroupe.COMPLET);
        when(groupeService.updateStatutGroupe(eq(1L), eq(StatutGroupe.COMPLET))).thenReturn(groupe);

        mockMvc.perform(put("/groupes/update-statut/1/COMPLET")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("COMPLET"));
    }

                    @Test
                    void getGroupeEnrichi_shouldReturnUserEnrichedPayload() throws Exception {
                    GroupeEnrichiDTO dto = GroupeEnrichiDTO.builder()
                        .idGroupe(1L)
                        .codeGroupe("G1")
                        .nom("Groupe 1")
                        .type(TypeGroupe.ETUDIANT)
                        .statut(StatutGroupe.EN_COURS)
                        .nombreFormateursActifs(1)
                        .formateurs(List.of(
                            FormateurEnrichiDTO.builder()
                                .formateurId(42L)
                                .username("teacher42")
                                .firstName("Amine")
                                .lastName("Ben Ali")
                                .email("amine@example.com")
                                .role(RoleFormateur.PRINCIPAL)
                                .dateDebut(LocalDate.of(2026, 4, 1))
                                .estActif(true)
                                .matiere("Oral")
                                .heuresParSemaine(4)
                                .build()))
                        .build();

                    when(groupeService.getGroupeEnrichi(1L)).thenReturn(dto);

                    mockMvc.perform(get("/groupes/1/enrichi").accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.codeGroupe").value("G1"))
                        .andExpect(jsonPath("$.formateurs[0].username").value("teacher42"))
                        .andExpect(jsonPath("$.formateurs[0].email").value("amine@example.com"));
                    }
}