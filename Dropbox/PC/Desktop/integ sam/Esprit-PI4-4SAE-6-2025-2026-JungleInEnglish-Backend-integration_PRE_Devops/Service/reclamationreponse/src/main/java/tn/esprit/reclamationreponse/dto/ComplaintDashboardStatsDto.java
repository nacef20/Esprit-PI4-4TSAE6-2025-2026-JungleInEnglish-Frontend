package tn.esprit.reclamationreponse.dto;

import tn.esprit.reclamationreponse.entities.ComplaintStatus;

import java.util.List;
import java.util.Map;

public record ComplaintDashboardStatsDto(
        long totalComplaints,
        Map<ComplaintStatus, Long> complaintsByStatus,
        List<ComplaintPeriodCountDto> complaintsByPeriod,
        double averageProcessingHours,
        double unresolvedRate
) {
}
