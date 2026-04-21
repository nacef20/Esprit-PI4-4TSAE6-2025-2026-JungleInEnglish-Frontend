package tn.esprit.reclamationreponse.dto;

public record ComplaintSentimentResponse(
        Long complaintId,
        String tone,
        boolean urgent,
        String reason
) {
}
