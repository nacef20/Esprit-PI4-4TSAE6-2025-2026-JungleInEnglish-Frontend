package tn.esprit.reclamationreponse.dto;

public record ComplaintSentimentRequest(
        Long complaintId,
        String title,
        String description
) {
}
