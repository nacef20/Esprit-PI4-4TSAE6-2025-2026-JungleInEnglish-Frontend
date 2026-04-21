package tn.esprit.reclamationreponse.services;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.reclamationreponse.dto.BinaryAttachmentDto;
import tn.esprit.reclamationreponse.entities.Response;

import java.util.List;

public interface IResponseService {

    Response addResponse(Long complaintId, Response response);

    Response addResponse(Long complaintId, Response response, MultipartFile attachment);

    Response updateResponse(Long id, Response response);

    Response updateResponse(Long id, Response response, MultipartFile attachment, boolean removeAttachment);

    void deleteResponse(Long id);

    Response getResponseById(Long id);

    List<Response> getAllResponses();

    List<Response> getResponsesByComplaintId(Long complaintId);

    BinaryAttachmentDto getResponseAttachment(Long id);
}
