package tn.esprit.reclamationreponse.controllers;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.reclamationreponse.dto.BinaryAttachmentDto;
import tn.esprit.reclamationreponse.dto.ResponseAiAssistRequest;
import tn.esprit.reclamationreponse.dto.ResponseAiAssistResponse;
import tn.esprit.reclamationreponse.entities.Response;
import tn.esprit.reclamationreponse.services.ComplaintAiAssistService;
import tn.esprit.reclamationreponse.services.IResponseService;

import java.util.List;

@RestController
@RequestMapping("/api/responses")

@RequiredArgsConstructor
public class ResponseController {

    private final IResponseService responseService;
    private final ComplaintAiAssistService complaintAiAssistService;

    // ==================== CREATE ====================

    @PostMapping(value = "/complaint/{complaintId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> addResponse(@PathVariable Long complaintId, @Valid @RequestBody Response response) {
        Response created = responseService.addResponse(complaintId, response);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping(value = "/complaint/{complaintId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> addResponseWithAttachment(
            @PathVariable Long complaintId,
            @Valid @RequestPart("response") Response response,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment
    ) {
        Response created = responseService.addResponse(complaintId, response, attachment);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // ==================== READ ====================

    @GetMapping
    public ResponseEntity<List<Response>> getAllResponses() {
        return ResponseEntity.ok(responseService.getAllResponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getResponseById(@PathVariable Long id) {
        return ResponseEntity.ok(responseService.getResponseById(id));
    }

    @GetMapping("/{id}/attachment")
    public ResponseEntity<byte[]> downloadResponseAttachment(@PathVariable Long id) {
        BinaryAttachmentDto attachment = responseService.getResponseAttachment(id);
        return buildAttachmentResponse(attachment);
    }

    @GetMapping("/complaint/{complaintId}")
    public ResponseEntity<List<Response>> getResponsesByComplaintId(@PathVariable Long complaintId) {
        return ResponseEntity.ok(responseService.getResponsesByComplaintId(complaintId));
    }

    @PostMapping("/ai/assist")
    public ResponseEntity<ResponseAiAssistResponse> assistResponseText(
            @RequestBody ResponseAiAssistRequest request
    ) {
        return ResponseEntity.ok(complaintAiAssistService.assistResponse(request));
    }

    // ==================== UPDATE ====================

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> updateResponse(@PathVariable Long id, @Valid @RequestBody Response response) {
        return ResponseEntity.ok(responseService.updateResponse(id, response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> updateResponseWithAttachment(
            @PathVariable Long id,
            @Valid @RequestPart("response") Response response,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment,
            @RequestParam(name = "removeAttachment", defaultValue = "false") boolean removeAttachment
    ) {
        return ResponseEntity.ok(responseService.updateResponse(id, response, attachment, removeAttachment));
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResponse(@PathVariable Long id) {
        responseService.deleteResponse(id);
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
