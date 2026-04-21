package tn.esprit.reclamationreponse.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.reclamationreponse.dto.BinaryAttachmentDto;
import tn.esprit.reclamationreponse.dto.ComplaintAdminReportStatsDto;
import tn.esprit.reclamationreponse.dto.ComplaintDashboardStatsDto;
import tn.esprit.reclamationreponse.dto.ComplaintPeriodCountDto;
import tn.esprit.reclamationreponse.dto.ComplaintPriorityLevel;
import tn.esprit.reclamationreponse.dto.ComplaintStatsPeriod;
import tn.esprit.reclamationreponse.entities.Complaint;
import tn.esprit.reclamationreponse.entities.ComplaintStatus;
import tn.esprit.reclamationreponse.entities.Response;
import tn.esprit.reclamationreponse.repositories.ComplaintRepository;
import tn.esprit.reclamationreponse.repositories.ResponseRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements IComplaintService {

    private static final Set<String> HIGH_PRIORITY_KEYWORDS = new HashSet<>(List.of(
        "urgent", "urgence", "asap", "immediately", "critical", "critique", "payment", "paiement",
        "security", "securite", "fraud", "arnaque", "blocked", "bloque", "cannot access", "impossible",
        "error 500", "down", "panne"
    ));

    private static final Set<String> MEDIUM_PRIORITY_KEYWORDS = new HashSet<>(List.of(
        "problem", "probleme", "issue", "failed", "failure", "error", "bug", "slow",
        "refund", "cancel", "delay", "retard", "not working", "ne fonctionne"
    ));

    private final ComplaintRepository complaintRepository;
    private final ResponseRepository responseRepository;
    private final BadWordsFilterService badWordsFilterService;
    private final AttachmentValidationService attachmentValidationService;

    @Override
    public Complaint addComplaint(Complaint complaint) {
        return addComplaint(complaint, null);
    }

    @Override
    public Complaint addComplaint(Complaint complaint, MultipartFile attachment) {
        sanitizeComplaintPayload(complaint);
        validateComplaintPayload(complaint);
        applyComplaintAttachment(complaint, attachment, false);
        return complaintRepository.save(complaint);
    }

    @Override
    public Complaint updateComplaint(Long id, Complaint complaint) {
        return updateComplaint(id, complaint, null, false);
    }

    @Override
    public Complaint updateComplaint(Long id, Complaint complaint, MultipartFile attachment, boolean removeAttachment) {
        Complaint existing = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));

        sanitizeComplaintPayload(complaint);
        validateComplaintPayload(complaint);

        existing.setTitle(complaint.getTitle());
        existing.setDescription(complaint.getDescription());
        existing.setStatus(complaint.getStatus());
        applyComplaintAttachment(existing, attachment, removeAttachment);

        return complaintRepository.save(existing);
    }

    @Override
    public Complaint rejectComplaint(Long id) {
        Complaint existing = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));

        existing.setStatus(ComplaintStatus.REJETEE);
        return complaintRepository.save(existing);
    }

    @Override
    public void deleteComplaint(Long id) {
        if (!complaintRepository.existsById(id)) {
            throw new RuntimeException("Complaint not found with id: " + id);
        }
        complaintRepository.deleteById(id);
    }

    @Override
    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));
    }

    @Override
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Page<Complaint> getComplaintsPage(int page, int size) {
        return complaintRepository.findAllByOrderByCreatedAtDesc(buildPageable(page, size));
    }

    @Override
    public List<Complaint> getComplaintsByStatus(ComplaintStatus status) {
        return complaintRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public Page<Complaint> getComplaintsPageByStatus(ComplaintStatus status, int page, int size) {
        return complaintRepository.findByStatusOrderByCreatedAtDesc(status, buildPageable(page, size));
    }

    @Override
    public Page<Complaint> getComplaintsPageFiltered(ComplaintStatus status, String title, int page, int size) {
        Pageable pageable = buildPageable(page, size);
        String safeTitle = title == null ? "" : title.trim();

        if (status != null && !safeTitle.isBlank()) {
            return complaintRepository.findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(status, safeTitle, pageable);
        }

        if (status != null) {
            return complaintRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }

        if (!safeTitle.isBlank()) {
            return complaintRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(safeTitle, pageable);
        }

        return complaintRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public List<Complaint> searchComplaintsByTitle(String title) {
        String safeTitle = title == null ? "" : title.trim();
        return complaintRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(safeTitle);
    }

    @Override
    public BinaryAttachmentDto getComplaintAttachment(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with id: " + id));

        if (complaint.getAttachmentData() == null || complaint.getAttachmentData().length == 0) {
            throw new IllegalArgumentException("No attachment found for complaint id: " + id);
        }

        String fileName = complaint.getAttachmentFileName() == null || complaint.getAttachmentFileName().isBlank()
                ? "complaint-attachment"
                : complaint.getAttachmentFileName();
        String contentType = complaint.getAttachmentContentType() == null || complaint.getAttachmentContentType().isBlank()
                ? "application/octet-stream"
                : complaint.getAttachmentContentType();

        return new BinaryAttachmentDto(fileName, contentType, complaint.getAttachmentData());
    }

    @Override
    public ComplaintDashboardStatsDto getComplaintDashboardStats(ComplaintStatsPeriod period) {
        List<Complaint> allComplaints = complaintRepository.findAllByOrderByCreatedAtDesc();

        long totalComplaints = allComplaints.size();
        Map<ComplaintStatus, Long> complaintsByStatus = computeStatusCounts(allComplaints);
        List<ComplaintPeriodCountDto> complaintsByPeriod = computePeriodCounts(allComplaints, period);
        double averageProcessingHours = computeAverageProcessingHours();
        double unresolvedRate = computeUnresolvedRate(totalComplaints, complaintsByStatus);

        return new ComplaintDashboardStatsDto(
                totalComplaints,
                complaintsByStatus,
                complaintsByPeriod,
                averageProcessingHours,
                unresolvedRate
        );
    }

    @Override
    public ComplaintAdminReportStatsDto getComplaintAdminReportStats() {
        List<Complaint> allComplaints = complaintRepository.findAllByOrderByCreatedAtDesc();

        long totalComplaints = allComplaints.size();
        Map<ComplaintStatus, Long> complaintsByStatus = computeStatusCounts(allComplaints);
        Map<ComplaintPriorityLevel, Long> complaintsByPriority = computePriorityCounts(allComplaints);
        double averageProcessingHours = computeAverageProcessingHours();
        double unresolvedRate = computeUnresolvedRate(totalComplaints, complaintsByStatus);

        return new ComplaintAdminReportStatsDto(
                totalComplaints,
                complaintsByStatus,
                complaintsByPriority,
                averageProcessingHours,
                unresolvedRate
        );
    }

    private void validateComplaintPayload(Complaint complaint) {
        if (complaint == null) {
            throw new IllegalArgumentException("complaint payload is required");
        }

        if (complaint.getTitle() == null || complaint.getTitle().trim().length() < 3) {
            throw new IllegalArgumentException("title must contain at least 3 characters");
        }

        if (complaint.getDescription() == null || complaint.getDescription().trim().length() < 10) {
            throw new IllegalArgumentException("description must contain at least 10 characters");
        }
    }

    private void sanitizeComplaintPayload(Complaint complaint) {
        if (complaint == null) {
            return;
        }

        complaint.setTitle(badWordsFilterService.sanitize(complaint.getTitle()));
        complaint.setDescription(badWordsFilterService.sanitize(complaint.getDescription()));
    }

    private void applyComplaintAttachment(Complaint complaint, MultipartFile attachment, boolean removeAttachment) {
        AttachmentValidationService.AttachmentPayload payload = attachmentValidationService.validateAndRead(attachment);

        if (payload != null) {
            complaint.setAttachmentFileName(payload.fileName());
            complaint.setAttachmentContentType(payload.contentType());
            complaint.setAttachmentSize(payload.size());
            complaint.setAttachmentData(payload.data());
            return;
        }

        if (removeAttachment) {
            complaint.setAttachmentFileName(null);
            complaint.setAttachmentContentType(null);
            complaint.setAttachmentSize(null);
            complaint.setAttachmentData(null);
        }
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        return PageRequest.of(safePage, safeSize);
    }

    private Map<ComplaintStatus, Long> computeStatusCounts(List<Complaint> complaints) {
        Map<ComplaintStatus, Long> counts = new EnumMap<>(ComplaintStatus.class);
        for (ComplaintStatus status : ComplaintStatus.values()) {
            counts.put(status, 0L);
        }

        for (Complaint complaint : complaints) {
            ComplaintStatus status = complaint.getStatus();
            if (status != null) {
                counts.put(status, counts.get(status) + 1L);
            }
        }

        return counts;
    }

    private Map<ComplaintPriorityLevel, Long> computePriorityCounts(List<Complaint> complaints) {
        Map<ComplaintPriorityLevel, Long> counts = new EnumMap<>(ComplaintPriorityLevel.class);
        for (ComplaintPriorityLevel level : ComplaintPriorityLevel.values()) {
            counts.put(level, 0L);
        }

        for (Complaint complaint : complaints) {
            ComplaintPriorityLevel priority = computePriority(complaint);
            counts.put(priority, counts.get(priority) + 1L);
        }

        return counts;
    }

    private ComplaintPriorityLevel computePriority(Complaint complaint) {
        if (complaint == null) {
            return ComplaintPriorityLevel.LOW;
        }

        String text = (safe(complaint.getTitle()) + " " + safe(complaint.getDescription())).toLowerCase(Locale.ROOT);
        ComplaintStatus status = complaint.getStatus() == null ? ComplaintStatus.EN_ATTENTE : complaint.getStatus();

        if (containsAny(text, HIGH_PRIORITY_KEYWORDS)) {
            return ComplaintPriorityLevel.HIGH;
        }

        if (status == ComplaintStatus.EN_ATTENTE && containsAny(text, MEDIUM_PRIORITY_KEYWORDS)) {
            return ComplaintPriorityLevel.HIGH;
        }

        if (status == ComplaintStatus.EN_ATTENTE || containsAny(text, MEDIUM_PRIORITY_KEYWORDS)) {
            return ComplaintPriorityLevel.MEDIUM;
        }

        return ComplaintPriorityLevel.LOW;
    }

    private boolean containsAny(String text, Set<String> keywords) {
        if (text == null || text.isBlank()) {
            return false;
        }

        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private List<ComplaintPeriodCountDto> computePeriodCounts(List<Complaint> complaints, ComplaintStatsPeriod period) {
        ComplaintStatsPeriod safePeriod = period == null ? ComplaintStatsPeriod.MONTH : period;
        Map<String, Long> groupedCounts = new TreeMap<>();

        for (Complaint complaint : complaints) {
            LocalDateTime createdAt = complaint.getCreatedAt();
            if (createdAt == null) {
                continue;
            }

            String key = formatPeriod(createdAt, safePeriod);
            groupedCounts.put(key, groupedCounts.getOrDefault(key, 0L) + 1L);
        }

        List<ComplaintPeriodCountDto> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : groupedCounts.entrySet()) {
            result.add(new ComplaintPeriodCountDto(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    private String formatPeriod(LocalDateTime dateTime, ComplaintStatsPeriod period) {
        if (period == ComplaintStatsPeriod.DAY) {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        if (period == ComplaintStatsPeriod.YEAR) {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy"));
        }

        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    private double computeAverageProcessingHours() {
        List<Complaint> treatedComplaints = complaintRepository.findByStatus(ComplaintStatus.TRAITEE);
        if (treatedComplaints.isEmpty()) {
            return 0.0;
        }

        List<Response> orderedResponses = responseRepository.findAllByOrderByComplaintIdAscCreatedAtAsc();
        Map<Long, LocalDateTime> firstResponseByComplaintId = new HashMap<>();
        for (Response response : orderedResponses) {
            if (response.getComplaint() == null || response.getComplaint().getId() == null) {
                continue;
            }

            Long complaintId = response.getComplaint().getId();
            firstResponseByComplaintId.putIfAbsent(complaintId, response.getCreatedAt());
        }

        double totalHours = 0.0;
        int processedCount = 0;

        for (Complaint complaint : treatedComplaints) {
            if (complaint.getId() == null || complaint.getCreatedAt() == null) {
                continue;
            }

            LocalDateTime firstResponseDate = firstResponseByComplaintId.get(complaint.getId());
            if (firstResponseDate == null || firstResponseDate.isBefore(complaint.getCreatedAt())) {
                continue;
            }

            double hours = Duration.between(complaint.getCreatedAt(), firstResponseDate).toMinutes() / 60.0;
            totalHours += hours;
            processedCount++;
        }

        if (processedCount == 0) {
            return 0.0;
        }

        return roundToTwoDecimals(totalHours / processedCount);
    }

    private double computeUnresolvedRate(long totalComplaints, Map<ComplaintStatus, Long> complaintsByStatus) {
        if (totalComplaints == 0) {
            return 0.0;
        }

        long unresolved = complaintsByStatus.getOrDefault(ComplaintStatus.EN_ATTENTE, 0L);
        return roundToTwoDecimals((unresolved * 100.0) / totalComplaints);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
