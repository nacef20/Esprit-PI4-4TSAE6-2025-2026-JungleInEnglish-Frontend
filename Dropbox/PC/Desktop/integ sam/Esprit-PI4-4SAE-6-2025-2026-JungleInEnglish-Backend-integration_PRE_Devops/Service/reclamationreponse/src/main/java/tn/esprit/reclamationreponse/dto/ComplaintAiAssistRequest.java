package tn.esprit.reclamationreponse.dto;

public record ComplaintAiAssistRequest(
        String title,
        String description,
        String mode
) {
}
