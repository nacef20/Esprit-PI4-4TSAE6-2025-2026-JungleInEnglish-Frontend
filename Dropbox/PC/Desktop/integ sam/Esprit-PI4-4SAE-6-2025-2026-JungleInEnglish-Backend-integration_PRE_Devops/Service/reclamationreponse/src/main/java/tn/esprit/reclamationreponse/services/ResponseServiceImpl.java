package tn.esprit.reclamationreponse.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.reclamationreponse.dto.BinaryAttachmentDto;
import tn.esprit.reclamationreponse.entities.Complaint;
import tn.esprit.reclamationreponse.entities.ComplaintStatus;
import tn.esprit.reclamationreponse.entities.Response;
import tn.esprit.reclamationreponse.repositories.ComplaintRepository;
import tn.esprit.reclamationreponse.repositories.ResponseRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResponseServiceImpl implements IResponseService {

    private final ResponseRepository responseRepository;
    private final ComplaintRepository complaintRepository;
    private final AttachmentValidationService attachmentValidationService;

    @Override
    public Response addResponse(Long complaintId, Response response) {
        return addResponse(complaintId, response, null);
    }

    @Override
    public Response addResponse(Long complaintId, Response response, MultipartFile attachment) {
        validateResponsePayload(response);

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + complaintId));

        if (complaint.getStatus() == ComplaintStatus.REJETEE) {
            throw new IllegalStateException("cannot respond to a rejected complaint");
        }

        response.setComplaint(complaint);
        applyResponseAttachment(response, attachment, false);

        // Automatically mark complaint as TRAITEE when a response is added
        complaint.setStatus(ComplaintStatus.TRAITEE);
        complaintRepository.save(complaint);

        return responseRepository.save(response);
    }

    @Override
    public Response updateResponse(Long id, Response response) {
        return updateResponse(id, response, null, false);
    }

    @Override
    public Response updateResponse(Long id, Response response, MultipartFile attachment, boolean removeAttachment) {
        validateResponsePayload(response);

        Response existing = responseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response not found with id: " + id));

        existing.setMessage(response.getMessage());
        applyResponseAttachment(existing, attachment, removeAttachment);

        return responseRepository.save(existing);
    }

    @Override
    public void deleteResponse(Long id) {
        if (!responseRepository.existsById(id)) {
            throw new RuntimeException("Response not found with id: " + id);
        }
        responseRepository.deleteById(id);
    }

    @Override
    public Response getResponseById(Long id) {
        return responseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response not found with id: " + id));
    }

    @Override
    public List<Response> getAllResponses() {
        return responseRepository.findAll();
    }

    @Override
    public List<Response> getResponsesByComplaintId(Long complaintId) {
        if (!complaintRepository.existsById(complaintId)) {
            throw new RuntimeException("Complaint not found with id: " + complaintId);
        }
        return responseRepository.findByComplaintIdOrderByCreatedAtDesc(complaintId);
    }

    @Override
    public BinaryAttachmentDto getResponseAttachment(Long id) {
        Response response = responseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response not found with id: " + id));

        if (response.getAttachmentData() == null || response.getAttachmentData().length == 0) {
            throw new IllegalArgumentException("No attachment found for response id: " + id);
        }

        String fileName = response.getAttachmentFileName() == null || response.getAttachmentFileName().isBlank()
                ? "response-attachment"
                : response.getAttachmentFileName();
        String contentType = response.getAttachmentContentType() == null || response.getAttachmentContentType().isBlank()
                ? "application/octet-stream"
                : response.getAttachmentContentType();

        return new BinaryAttachmentDto(fileName, contentType, response.getAttachmentData());
    }

    private void applyResponseAttachment(Response response, MultipartFile attachment, boolean removeAttachment) {
        AttachmentValidationService.AttachmentPayload payload = attachmentValidationService.validateAndRead(attachment);

        if (payload != null) {
            response.setAttachmentFileName(payload.fileName());
            response.setAttachmentContentType(payload.contentType());
            response.setAttachmentSize(payload.size());
            response.setAttachmentData(payload.data());
            return;
        }

        if (removeAttachment) {
            response.setAttachmentFileName(null);
            response.setAttachmentContentType(null);
            response.setAttachmentSize(null);
            response.setAttachmentData(null);
        }
    }

    private void validateResponsePayload(Response response) {
        if (response == null) {
            throw new IllegalArgumentException("response payload is required");
        }

        if (response.getMessage() == null || response.getMessage().trim().length() < 2) {
            throw new IllegalArgumentException("message must contain at least 2 characters");
        }
    }
}
