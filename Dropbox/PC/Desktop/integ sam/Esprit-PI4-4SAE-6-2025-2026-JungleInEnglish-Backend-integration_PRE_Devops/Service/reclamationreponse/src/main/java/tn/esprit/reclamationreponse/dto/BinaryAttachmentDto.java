package tn.esprit.reclamationreponse.dto;

public record BinaryAttachmentDto(
        String fileName,
        String contentType,
        byte[] data
) {
}
