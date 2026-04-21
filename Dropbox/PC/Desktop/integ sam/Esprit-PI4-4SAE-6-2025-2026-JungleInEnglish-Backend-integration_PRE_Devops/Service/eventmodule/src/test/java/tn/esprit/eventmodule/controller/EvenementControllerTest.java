package tn.esprit.eventmodule.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.eventmodule.entity.Evenement;
import tn.esprit.eventmodule.service.EvenementService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvenementController.class)
class EvenementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvenementService evenementService;

    @Test
    @WithMockUser(roles = "PARTICIPANT")
    void findAll_ShouldReturnOk() throws Exception {
        when(evenementService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/evenements/find"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_ShouldReturnCreated_WhenAdmin() throws Exception {
        Evenement e = new Evenement();
        e.setTitle("New Event");
        when(evenementService.create(any())).thenReturn(e);

        mockMvc.perform(post("/api/evenements/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"New Event\", \"capacity\": 10, \"startTime\": \"2026-05-15T10:00:00\", \"endTime\": \"2026-05-15T12:00:00\", \"location\": \"Room 1\", \"level\": \"A1\", \"organizerId\": 1}"))
                .andExpect(status().isCreated());
    }

}
