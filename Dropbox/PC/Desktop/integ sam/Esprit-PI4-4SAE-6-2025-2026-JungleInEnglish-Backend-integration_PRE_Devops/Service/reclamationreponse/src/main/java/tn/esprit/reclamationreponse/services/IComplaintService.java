package tn.esprit.reclamationreponse.services;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.reclamationreponse.dto.BinaryAttachmentDto;
import tn.esprit.reclamationreponse.dto.ComplaintAdminReportStatsDto;
import tn.esprit.reclamationreponse.dto.ComplaintDashboardStatsDto;
import tn.esprit.reclamationreponse.dto.ComplaintStatsPeriod;
import tn.esprit.reclamationreponse.entities.Complaint;
import tn.esprit.reclamationreponse.entities.ComplaintStatus;

import java.util.List;

public interface IComplaintService {

    Complaint addComplaint(Complaint complaint);

    Complaint addComplaint(Complaint complaint, MultipartFile attachment);

    Complaint updateComplaint(Long id, Complaint complaint);

    Complaint updateComplaint(Long id, Complaint complaint, MultipartFile attachment, boolean removeAttachment);

    Complaint rejectComplaint(Long id);

    void deleteComplaint(Long id);

    Complaint getComplaintById(Long id);

    List<Complaint> getAllComplaints();

    Page<Complaint> getComplaintsPage(int page, int size);

    List<Complaint> getComplaintsByStatus(ComplaintStatus status);

    Page<Complaint> getComplaintsPageByStatus(ComplaintStatus status, int page, int size);

    Page<Complaint> getComplaintsPageFiltered(ComplaintStatus status, String title, int page, int size);

    List<Complaint> searchComplaintsByTitle(String title);

    BinaryAttachmentDto getComplaintAttachment(Long id);

    ComplaintDashboardStatsDto getComplaintDashboardStats(ComplaintStatsPeriod period);

    ComplaintAdminReportStatsDto getComplaintAdminReportStats();
}
