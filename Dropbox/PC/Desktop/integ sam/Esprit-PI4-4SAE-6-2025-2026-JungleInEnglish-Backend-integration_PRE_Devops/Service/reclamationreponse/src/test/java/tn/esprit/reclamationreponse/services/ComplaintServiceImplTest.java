package tn.esprit.reclamationreponse.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.reclamationreponse.dto.BinaryAttachmentDto;
import tn.esprit.reclamationreponse.dto.ComplaintDashboardStatsDto;
import tn.esprit.reclamationreponse.dto.ComplaintStatsPeriod;
import tn.esprit.reclamationreponse.entities.Complaint;
import tn.esprit.reclamationreponse.entities.ComplaintStatus;
import tn.esprit.reclamationreponse.entities.Response;
import tn.esprit.reclamationreponse.repositories.ComplaintRepository;
import tn.esprit.reclamationreponse.repositories.ResponseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ComplaintServiceImpl.
 *
 * This class validates business rules in isolation by mocking all collaborators
 * (repositories and helper services), so no database or external component is used.
 */
@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

        // Main persistence dependency used by complaint-related business flows.
    @Mock
    private ComplaintRepository complaintRepository;

        // Used by metrics computations (processing time based on first response).
    @Mock
    private ResponseRepository responseRepository;

        // Sanitization dependency that removes/rewrites forbidden words.
    @Mock
    private BadWordsFilterService badWordsFilterService;

        // Attachment validator that enforces type/size constraints.
    @Mock
    private AttachmentValidationService attachmentValidationService;

        // Generic file input used in tests where attachment upload is part of the scenario.
    @Mock
    private MultipartFile attachment;

        // System under test with all mocks injected by Mockito.
    @InjectMocks
    private ComplaintServiceImpl complaintService;

    @Test
    void addComplaintShouldSanitizeApplyAttachmentAndSave() {
        // Arrange
        Complaint complaint = complaint("stupid title", "stupid long description here");

        when(badWordsFilterService.sanitize("stupid title")).thenReturn("clean title");
        when(badWordsFilterService.sanitize("stupid long description here")).thenReturn("clean long description");

        AttachmentValidationService.AttachmentPayload payload =
                new AttachmentValidationService.AttachmentPayload(
                        "evidence.pdf", "application/pdf", 3L, new byte[]{1, 2, 3}
                );
        when(attachmentValidationService.validateAndRead(attachment)).thenReturn(payload);
        when(complaintRepository.save(complaint)).thenReturn(complaint);

        // Act
        Complaint saved = complaintService.addComplaint(complaint, attachment);

        // Assert
        assertSame(complaint, saved);
        assertEquals("clean title", complaint.getTitle());
        assertEquals("clean long description", complaint.getDescription());
        assertEquals("evidence.pdf", complaint.getAttachmentFileName());
        assertEquals("application/pdf", complaint.getAttachmentContentType());
        assertEquals(3L, complaint.getAttachmentSize());
        assertArrayEquals(new byte[]{1, 2, 3}, complaint.getAttachmentData());

        verify(complaintRepository).save(complaint);
        verify(attachmentValidationService).validateAndRead(attachment);
    }

    @Test
    void addComplaintShouldThrowWhenTitleTooShort() {
        // Arrange
        Complaint complaint = complaint("ab", "valid description with enough characters");
        when(badWordsFilterService.sanitize("ab")).thenReturn("ab");
        when(badWordsFilterService.sanitize("valid description with enough characters"))
                .thenReturn("valid description with enough characters");

        // Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> complaintService.addComplaint(complaint, attachment));

        // Assert
        assertEquals("title must contain at least 3 characters", ex.getMessage());
                // No persistence call should happen when validation fails.
        verify(complaintRepository, never()).save(any());
        verifyNoInteractions(attachmentValidationService);
    }

    @Test
    void updateComplaintShouldThrowWhenComplaintNotFound() {
        // Arrange
        Complaint updatePayload = complaint("Updated title", "Updated description with enough chars");
        when(complaintRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> complaintService.updateComplaint(99L, updatePayload));

        // Assert
        assertEquals("Complaint not found with id: 99", ex.getMessage());
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void getComplaintsPageFilteredShouldUseTrimmedTitleAndSafePagination() {
        // Arrange
        Page<Complaint> expectedPage = new PageImpl<>(List.of(complaint("Login issue", "Description long enough")));
        when(complaintRepository.findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                eq(ComplaintStatus.EN_ATTENTE), eq("login"), any(Pageable.class)
        )).thenReturn(expectedPage);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // Act
        Page<Complaint> result = complaintService.getComplaintsPageFiltered(
                ComplaintStatus.EN_ATTENTE,
                "  login  ",
                -4,
                0
        );

        // Assert
        assertSame(expectedPage, result);
        verify(complaintRepository)
                .findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                        eq(ComplaintStatus.EN_ATTENTE), eq("login"), pageableCaptor.capture()
                );

        // Service should normalize invalid paging values: page >= 0 and size >= 1.
        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(1, pageable.getPageSize());
    }

    @Test
    void deleteComplaintShouldThrowWhenComplaintDoesNotExist() {
        // Arrange
        when(complaintRepository.existsById(17L)).thenReturn(false);

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> complaintService.deleteComplaint(17L));

        // Assert
        assertEquals("Complaint not found with id: 17", ex.getMessage());
        verify(complaintRepository, never()).deleteById(any());
    }

    @Test
    void getComplaintAttachmentShouldReturnDefaultMetadataWhenMissing() {
        // Arrange
        Complaint complaint = complaint("Title valid", "Description valid and long enough");
        complaint.setId(5L);
        complaint.setAttachmentData(new byte[]{9, 8});
        complaint.setAttachmentFileName(" ");
        complaint.setAttachmentContentType(" ");

        when(complaintRepository.findById(5L)).thenReturn(Optional.of(complaint));

        // Act
        BinaryAttachmentDto attachmentDto = complaintService.getComplaintAttachment(5L);

        // Assert
        assertEquals("complaint-attachment", attachmentDto.fileName());
        assertEquals("application/octet-stream", attachmentDto.contentType());
        assertArrayEquals(new byte[]{9, 8}, attachmentDto.data());
    }

    @Test
    void getComplaintDashboardStatsShouldComputeBusinessMetrics() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        Complaint treated = complaint("Payment issue", "Critical payment issue and urgent");
        treated.setId(1L);
        treated.setStatus(ComplaintStatus.TRAITEE);
        treated.setCreatedAt(now.minusHours(5));

        Complaint pending = complaint("Login issue", "Cannot access dashboard quickly");
        pending.setId(2L);
        pending.setStatus(ComplaintStatus.EN_ATTENTE);
        pending.setCreatedAt(now.minusHours(2));

        Response firstResponse = new Response();
        firstResponse.setComplaint(treated);
        firstResponse.setCreatedAt(now.minusHours(3));

        when(complaintRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(treated, pending));
        when(complaintRepository.findByStatus(ComplaintStatus.TRAITEE)).thenReturn(List.of(treated));
        when(responseRepository.findAllByOrderByComplaintIdAscCreatedAtAsc()).thenReturn(List.of(firstResponse));

        // Act
        ComplaintDashboardStatsDto stats = complaintService.getComplaintDashboardStats(ComplaintStatsPeriod.MONTH);

        // Assert
                // Processing hours are computed from complaint creation to first response.
        assertEquals(2L, stats.totalComplaints());
        assertEquals(1L, stats.complaintsByStatus().get(ComplaintStatus.TRAITEE));
        assertEquals(1L, stats.complaintsByStatus().get(ComplaintStatus.EN_ATTENTE));
        assertEquals(2.0, stats.averageProcessingHours());
        assertEquals(50.0, stats.unresolvedRate());
    }

        // Small factory helper to keep tests focused on business intent.
    private Complaint complaint(String title, String description) {
        Complaint complaint = new Complaint();
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setStatus(ComplaintStatus.EN_ATTENTE);
        return complaint;
    }
}
