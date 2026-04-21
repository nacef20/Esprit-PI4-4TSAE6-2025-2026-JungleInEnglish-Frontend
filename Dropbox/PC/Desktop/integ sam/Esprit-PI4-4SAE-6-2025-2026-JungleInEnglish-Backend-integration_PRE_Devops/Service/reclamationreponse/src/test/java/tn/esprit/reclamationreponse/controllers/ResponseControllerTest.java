package tn.esprit.reclamationreponse.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.reclamationreponse.dto.BinaryAttachmentDto;
import tn.esprit.reclamationreponse.entities.Response;
import tn.esprit.reclamationreponse.exceptions.ValidationExceptionHandler;
import tn.esprit.reclamationreponse.services.ComplaintAiAssistService;
import tn.esprit.reclamationreponse.services.IResponseService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for ResponseController endpoints.
 *
 * Tests verify endpoint behavior only (HTTP and JSON contracts) while business
 * logic remains mocked behind IResponseService.
 */
@WebMvcTest(controllers = ResponseController.class)
@Import(ValidationExceptionHandler.class)
class ResponseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Serializes/deserializes JSON payloads in MockMvc calls.
    @Autowired
    private ObjectMapper objectMapper;

    // Mocked controller collaborators.
    @MockitoBean
    private IResponseService responseService;

    @MockitoBean
    private ComplaintAiAssistService complaintAiAssistService;

    @Test
    void addResponseShouldReturnCreatedAndJsonBody() throws Exception {
        // Arrange
        Response request = response("We are reviewing your request");
        Response created = response("We are reviewing your request");
        created.setId(1L);

        when(responseService.addResponse(eq(10L), any(Response.class))).thenReturn(created);

        // Act + Assert
        mockMvc.perform(post("/api/responses/complaint/{complaintId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.message").value("We are reviewing your request"));

        verify(responseService).addResponse(eq(10L), any(Response.class));
    }

    @Test
    void getResponseByIdShouldReturnOkAndJsonBody() throws Exception {
        // Arrange
        Response existing = response("Resolution details");
        existing.setId(6L);

        when(responseService.getResponseById(6L)).thenReturn(existing);

        // Act + Assert
        mockMvc.perform(get("/api/responses/{id}", 6L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.message").value("Resolution details"));

        verify(responseService).getResponseById(6L);
    }

    @Test
    void updateResponseShouldReturnOkAndUpdatedJson() throws Exception {
        // Arrange
        Response request = response("Updated response text");
        Response updated = response("Updated response text");
        updated.setId(8L);

        when(responseService.updateResponse(eq(8L), any(Response.class))).thenReturn(updated);

        // Act + Assert
        mockMvc.perform(put("/api/responses/{id}", 8L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.message").value("Updated response text"));

        verify(responseService).updateResponse(eq(8L), any(Response.class));
    }

    @Test
    void deleteResponseShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(responseService).deleteResponse(14L);

        // Act + Assert
        mockMvc.perform(delete("/api/responses/{id}", 14L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(responseService).deleteResponse(14L);
    }

    @Test
    void addResponseShouldReturnConflictWhenServiceThrowsIllegalState() throws Exception {
        // Arrange
        Response request = response("Attempt to respond to rejected complaint");

        when(responseService.addResponse(eq(99L), any(Response.class)))
                .thenThrow(new IllegalStateException("cannot respond to a rejected complaint"));

        // Act + Assert
        mockMvc.perform(post("/api/responses/complaint/{complaintId}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
            // IllegalStateException is translated to 409 by ValidationExceptionHandler.
                .andExpect(jsonPath("$.error").value("cannot respond to a rejected complaint"));
    }

    @Test
    void downloadResponseAttachmentShouldReturnBinaryPayload() throws Exception {
        // Arrange
        BinaryAttachmentDto dto = new BinaryAttachmentDto("answer.pdf", "application/pdf", new byte[]{7, 8});
        when(responseService.getResponseAttachment(25L)).thenReturn(dto);

        // Act + Assert
        mockMvc.perform(get("/api/responses/{id}/attachment", 25L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"answer.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(new byte[]{7, 8}));

        verify(responseService).getResponseAttachment(25L);
    }

    private Response response(String message) {
        // Minimal helper for concise request/expected payload setup.
        Response response = new Response();
        response.setMessage(message);
        return response;
    }
}
