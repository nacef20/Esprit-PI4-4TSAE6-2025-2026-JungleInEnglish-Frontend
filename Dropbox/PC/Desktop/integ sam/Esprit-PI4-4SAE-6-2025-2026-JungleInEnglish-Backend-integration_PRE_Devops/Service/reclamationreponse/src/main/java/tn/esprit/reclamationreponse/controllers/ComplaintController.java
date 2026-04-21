package tn.esprit.reclamationreponse.controllers;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import tn.esprit.reclamationreponse.dto.BinaryAttachmentDto;
import tn.esprit.reclamationreponse.dto.ComplaintAdminReportStatsDto;
import tn.esprit.reclamationreponse.dto.ComplaintAiAssistRequest;
import tn.esprit.reclamationreponse.dto.ComplaintAiAssistResponse;
import tn.esprit.reclamationreponse.dto.ComplaintDashboardStatsDto;
import tn.esprit.reclamationreponse.dto.ComplaintSentimentRequest;
import tn.esprit.reclamationreponse.dto.ComplaintSentimentResponse;
import tn.esprit.reclamationreponse.dto.ComplaintStatsPeriod;
import tn.esprit.reclamationreponse.entities.Complaint;
import tn.esprit.reclamationreponse.entities.ComplaintStatus;
import tn.esprit.reclamationreponse.services.ComplaintAdminReportExcelService;
import tn.esprit.reclamationreponse.services.ComplaintAdminReportPdfService;
import tn.esprit.reclamationreponse.services.ComplaintAiAssistService;
import tn.esprit.reclamationreponse.services.IComplaintService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")

@RequiredArgsConstructor
public class ComplaintController {

    private final IComplaintService complaintService;
    private final ComplaintAiAssistService complaintAiAssistService;
    private final ComplaintAdminReportPdfService complaintAdminReportPdfService;
    private final ComplaintAdminReportExcelService complaintAdminReportExcelService;

    // ==================== CREATE ====================

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Complaint> addComplaint(@Valid @RequestBody Complaint complaint) {
        Complaint created = complaintService.addComplaint(complaint);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Complaint> addComplaintWithAttachment(
            @Valid @RequestPart("complaint") Complaint complaint,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment
    ) {
        Complaint created = complaintService.addComplaint(complaint, attachment);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // ==================== READ ====================

    @GetMapping
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Complaint>> getComplaintsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(name = "status", required = false) ComplaintStatus status,
            @RequestParam(name = "title", required = false) String title) {
        return ResponseEntity.ok(complaintService.getComplaintsPageFiltered(status, title, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintById(id));
    }

    @GetMapping("/{id}/attachment")
    public ResponseEntity<byte[]> downloadComplaintAttachment(@PathVariable Long id) {
        BinaryAttachmentDto attachment = complaintService.getComplaintAttachment(id);
        return buildAttachmentResponse(attachment);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Complaint>> getComplaintsByStatus(@PathVariable ComplaintStatus status) {
        return ResponseEntity.ok(complaintService.getComplaintsByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Complaint>> searchComplaintsByTitle(
            @RequestParam(name = "title", required = false) String title) {
        return ResponseEntity.ok(complaintService.searchComplaintsByTitle(title));
    }

    @GetMapping("/stats/dashboard")
    public ResponseEntity<ComplaintDashboardStatsDto> getComplaintDashboardStats(
            @RequestParam(defaultValue = "MONTH") ComplaintStatsPeriod period) {
        return ResponseEntity.ok(complaintService.getComplaintDashboardStats(period));
    }

    @GetMapping("/stats/admin-report/pdf")
    public ResponseEntity<byte[]> downloadAdminReportPdf() {
        ComplaintAdminReportStatsDto stats = complaintService.getComplaintAdminReportStats();
        byte[] pdf = complaintAdminReportPdfService.generatePdf(stats);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String filename = "complaints-admin-report-" + timestamp + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/stats/admin-report/excel")
    public ResponseEntity<byte[]> downloadAdminReportExcel() {
        ComplaintAdminReportStatsDto stats = complaintService.getComplaintAdminReportStats();
        byte[] excel = complaintAdminReportExcelService.generateExcel(stats);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String filename = "complaints-admin-report-" + timestamp + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @PostMapping("/ai/assist")
    public ResponseEntity<ComplaintAiAssistResponse> assistComplaintText(
            @RequestBody ComplaintAiAssistRequest request) {
        return ResponseEntity.ok(complaintAiAssistService.assist(request));
    }

    @PostMapping("/ai/sentiment")
    public ResponseEntity<ComplaintSentimentResponse> analyzeComplaintSentiment(
            @RequestBody ComplaintSentimentRequest request
    ) {
        return ResponseEntity.ok(complaintAiAssistService.analyzeComplaintSentiment(request));
    }

    // ==================== UPDATE ====================

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Complaint> updateComplaint(@PathVariable Long id, @Valid @RequestBody Complaint complaint) {
        return ResponseEntity.ok(complaintService.updateComplaint(id, complaint));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Complaint> updateComplaintWithAttachment(
            @PathVariable Long id,
            @Valid @RequestPart("complaint") Complaint complaint,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment,
            @RequestParam(name = "removeAttachment", defaultValue = "false") boolean removeAttachment
    ) {
        return ResponseEntity.ok(complaintService.updateComplaint(id, complaint, attachment, removeAttachment));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Complaint> rejectComplaint(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.rejectComplaint(id));
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<byte[]> buildAttachmentResponse(BinaryAttachmentDto attachment) {
        String safeFileName = attachment.fileName().replaceAll("[\\r\\n\\\"]", "_");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFileName + "\"")
                .contentType(MediaType.parseMediaType(attachment.contentType()))
                .body(attachment.data());
    }
}
