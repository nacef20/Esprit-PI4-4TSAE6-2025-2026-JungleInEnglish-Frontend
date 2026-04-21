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
import tn.esprit.reclamationreponse.entities.Complaint;
import tn.esprit.reclamationreponse.entities.ComplaintStatus;
import tn.esprit.reclamationreponse.exceptions.ValidationExceptionHandler;
import tn.esprit.reclamationreponse.services.ComplaintAdminReportExcelService;
import tn.esprit.reclamationreponse.services.ComplaintAdminReportPdfService;
import tn.esprit.reclamationreponse.services.ComplaintAiAssistService;
import tn.esprit.reclamationreponse.services.IComplaintService;

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
 * Web-layer tests for ComplaintController.
 *
 * @WebMvcTest loads only MVC components; service dependencies are mocked with
 * @MockitoBean to verify endpoint contracts (HTTP status, JSON payload, headers).
 */
@WebMvcTest(controllers = ComplaintController.class)
@Import(ValidationExceptionHandler.class)
class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Used to serialize request payloads to JSON.
    @Autowired
    private ObjectMapper objectMapper;

    // Controller dependencies mocked at Spring context level.
    @MockitoBean
    private IComplaintService complaintService;

    @MockitoBean
    private ComplaintAiAssistService complaintAiAssistService;

    @MockitoBean
    private ComplaintAdminReportPdfService complaintAdminReportPdfService;

    @MockitoBean
    private ComplaintAdminReportExcelService complaintAdminReportExcelService;

    @Test
    void addComplaintShouldReturnCreatedAndJsonBody() throws Exception {
        // Arrange
        Complaint request = complaint("Login issue", "I cannot access my account dashboard", ComplaintStatus.EN_ATTENTE);
        Complaint created = complaint("Login issue", "I cannot access my account dashboard", ComplaintStatus.EN_ATTENTE);
        created.setId(1L);

        when(complaintService.addComplaint(any(Complaint.class))).thenReturn(created);

        // Act + Assert
        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Login issue"))
                .andExpect(jsonPath("$.status").value("EN_ATTENTE"));

        verify(complaintService).addComplaint(any(Complaint.class));
    }

    @Test
    void getComplaintByIdShouldReturnOkAndJsonBody() throws Exception {
        // Arrange
        Complaint existing = complaint("Payment delay", "Refund has not been processed for several days", ComplaintStatus.EN_ATTENTE);
        existing.setId(7L);

        when(complaintService.getComplaintById(7L)).thenReturn(existing);

        // Act + Assert
        mockMvc.perform(get("/api/complaints/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.title").value("Payment delay"));

        verify(complaintService).getComplaintById(7L);
    }

    @Test
    void updateComplaintShouldReturnOkAndUpdatedJson() throws Exception {
        // Arrange
        Complaint request = complaint("Updated title", "Updated description with enough characters", ComplaintStatus.TRAITEE);
        Complaint updated = complaint("Updated title", "Updated description with enough characters", ComplaintStatus.TRAITEE);
        updated.setId(3L);

        when(complaintService.updateComplaint(eq(3L), any(Complaint.class))).thenReturn(updated);

        // Act + Assert
        mockMvc.perform(put("/api/complaints/{id}", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.status").value("TRAITEE"));

        verify(complaintService).updateComplaint(eq(3L), any(Complaint.class));
    }

    @Test
    void deleteComplaintShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(complaintService).deleteComplaint(11L);

        // Act + Assert
        mockMvc.perform(delete("/api/complaints/{id}", 11L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(complaintService).deleteComplaint(11L);
    }

    @Test
    void addComplaintShouldReturnBadRequestWhenPayloadInvalid() throws Exception {
        // Arrange
        Complaint invalid = complaint("", "short", ComplaintStatus.EN_ATTENTE);

        // Act + Assert
        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
            // ValidationExceptionHandler returns field-level errors as JSON map.
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void downloadComplaintAttachmentShouldReturnBinaryPayload() throws Exception {
        // Arrange
        BinaryAttachmentDto dto = new BinaryAttachmentDto("proof.pdf", "application/pdf", new byte[]{1, 2, 3});
        when(complaintService.getComplaintAttachment(15L)).thenReturn(dto);

        // Act + Assert
        mockMvc.perform(get("/api/complaints/{id}/attachment", 15L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"proof.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));

        verify(complaintService).getComplaintAttachment(15L);
    }

    private Complaint complaint(String title, String description, ComplaintStatus status) {
        // Shared builder helper to reduce repetitive object setup in tests.
        Complaint complaint = new Complaint();
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setStatus(status);
        return complaint;
    }
}
