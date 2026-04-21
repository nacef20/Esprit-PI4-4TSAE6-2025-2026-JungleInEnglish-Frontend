package tn.esprit.reclamationreponse.dto;

public record ResponseAiAssistRequest(
        String complaintTitle,
        String complaintDescription,
        String draftResponse,
        String mode
) {
}
