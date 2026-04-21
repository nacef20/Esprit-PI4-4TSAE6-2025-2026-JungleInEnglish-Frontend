package tn.esprit.reclamationreponse.dto;

import tn.esprit.reclamationreponse.entities.ComplaintStatus;

import java.util.Map;

public record ComplaintAdminReportStatsDto(
        long totalComplaints,
        Map<ComplaintStatus, Long> complaintsByStatus,
        Map<ComplaintPriorityLevel, Long> complaintsByPriority,
        double averageProcessingHours,
        double unresolvedRate
) {
}
