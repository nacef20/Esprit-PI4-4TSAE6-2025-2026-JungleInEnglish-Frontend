package tn.esprit.reclamationreponse.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.reclamationreponse.dto.BinaryAttachmentDto;
import tn.esprit.reclamationreponse.entities.Complaint;
import tn.esprit.reclamationreponse.entities.ComplaintStatus;
import tn.esprit.reclamationreponse.entities.Response;
import tn.esprit.reclamationreponse.repositories.ComplaintRepository;
import tn.esprit.reclamationreponse.repositories.ResponseRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ResponseServiceImpl.
 *
 * The tests focus on response business rules (status transitions, validation,
 * attachment behavior) with mocked repositories/services only.
 */
@ExtendWith(MockitoExtension.class)
class ResponseServiceImplTest {

    // Persists response entities.
    @Mock
    private ResponseRepository responseRepository;

    // Used to fetch and update parent complaint state.
    @Mock
    private ComplaintRepository complaintRepository;

    // Applies attachment validation and content extraction.
    @Mock
    private AttachmentValidationService attachmentValidationService;

    // Mock upload input for attachment scenarios.
    @Mock
    private MultipartFile attachment;

    // System under test with injected mocks.
    @InjectMocks
    private ResponseServiceImpl responseService;

    @Test
    void addResponseShouldAttachComplaintSetStatusAndSave() {
        // Arrange
        Complaint complaint = new Complaint();
        complaint.setId(10L);
        complaint.setStatus(ComplaintStatus.EN_ATTENTE);

        Response response = new Response();
        response.setMessage("We are investigating your issue");

        when(complaintRepository.findById(10L)).thenReturn(Optional.of(complaint));
        when(attachmentValidationService.validateAndRead(attachment)).thenReturn(null);
        when(responseRepository.save(response)).thenReturn(response);

        // Act
        Response saved = responseService.addResponse(10L, response, attachment);

        // Assert
        assertSame(response, saved);
        assertSame(complaint, response.getComplaint());
        // Adding a response auto-marks complaint as resolved.
        assertEquals(ComplaintStatus.TRAITEE, complaint.getStatus());

        verify(complaintRepository).save(complaint);
        verify(responseRepository).save(response);
    }

    @Test
    void addResponseShouldThrowWhenComplaintIsRejected() {
        // Arrange
        Complaint rejected = new Complaint();
        rejected.setId(12L);
        rejected.setStatus(ComplaintStatus.REJETEE);

        Response response = new Response();
        response.setMessage("Message valid");

        when(complaintRepository.findById(12L)).thenReturn(Optional.of(rejected));

        // Act
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> responseService.addResponse(12L, response));

        // Assert
        assertEquals("cannot respond to a rejected complaint", ex.getMessage());
        verify(responseRepository, never()).save(any());
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void updateResponseShouldClearAttachmentWhenRemoveAttachmentIsTrue() {
        // Arrange
        Response existing = new Response();
        existing.setId(3L);
        existing.setMessage("Old message");
        existing.setAttachmentFileName("proof.pdf");
        existing.setAttachmentContentType("application/pdf");
        existing.setAttachmentSize(123L);
        existing.setAttachmentData(new byte[]{1, 2});

        Response updatePayload = new Response();
        updatePayload.setMessage("Updated message");

        when(responseRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(attachmentValidationService.validateAndRead(null)).thenReturn(null);
        when(responseRepository.save(existing)).thenReturn(existing);

        // Act
        Response updated = responseService.updateResponse(3L, updatePayload, null, true);

        // Assert
        assertSame(existing, updated);
        assertEquals("Updated message", updated.getMessage());
        assertNull(updated.getAttachmentFileName());
        assertNull(updated.getAttachmentContentType());
        assertNull(updated.getAttachmentSize());
        assertNull(updated.getAttachmentData());

        verify(responseRepository).save(existing);
    }

    @Test
    void getResponsesByComplaintIdShouldThrowWhenComplaintNotFound() {
        // Arrange
        when(complaintRepository.existsById(66L)).thenReturn(false);

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> responseService.getResponsesByComplaintId(66L));

        // Assert
        assertEquals("Complaint not found with id: 66", ex.getMessage());
        // Query must not continue when complaint does not exist.
        verify(responseRepository, never()).findByComplaintIdOrderByCreatedAtDesc(any());
    }

    @Test
    void getResponseAttachmentShouldThrowWhenNoAttachmentExists() {
        // Arrange
        Response response = new Response();
        response.setId(7L);
        response.setMessage("Valid");
        response.setAttachmentData(new byte[0]);

        when(responseRepository.findById(7L)).thenReturn(Optional.of(response));

        // Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> responseService.getResponseAttachment(7L));

        // Assert
        assertEquals("No attachment found for response id: 7", ex.getMessage());
    }

    @Test
    void getResponseAttachmentShouldFallbackToDefaultMetadata() {
        // Arrange
        Response response = new Response();
        response.setId(19L);
        response.setMessage("Valid response text");
        response.setAttachmentData(new byte[]{4, 5, 6});
        response.setAttachmentFileName(" ");
        response.setAttachmentContentType(" ");

        when(responseRepository.findById(19L)).thenReturn(Optional.of(response));

        // Act
        BinaryAttachmentDto dto = responseService.getResponseAttachment(19L);

        // Assert
        assertEquals("response-attachment", dto.fileName());
        assertEquals("application/octet-stream", dto.contentType());
        assertArrayEquals(new byte[]{4, 5, 6}, dto.data());
    }

    @Test
    void deleteResponseShouldThrowWhenResponseNotFound() {
        // Arrange
        when(responseRepository.existsById(30L)).thenReturn(false);

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> responseService.deleteResponse(30L));

        // Assert
        assertEquals("Response not found with id: 30", ex.getMessage());
        verify(responseRepository, never()).deleteById(any());
    }
}
