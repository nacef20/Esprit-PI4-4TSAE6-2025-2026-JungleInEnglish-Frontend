package tn.esprit.eventmodule.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.eventmodule.entity.Participation;
import tn.esprit.eventmodule.service.ParticipationService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParticipationController.class)
class ParticipationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParticipationService participationService;

    @Test
    @WithMockUser(roles = "PARTICIPANT")
    void registerToEvent_ShouldReturnCreated() throws Exception {
        when(participationService.registerToEvent(anyString(), anyLong())).thenReturn(new Participation());

        mockMvc.perform(post("/api/participations/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user1\", \"eventId\": 1}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void scanQrCode_ShouldReturnOk() throws Exception {
        when(participationService.scanQrCode("token123")).thenReturn(new Participation());

        mockMvc.perform(post("/api/participations/scan")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"token123\"}"))
                .andExpect(status().isOk());
    }
}
