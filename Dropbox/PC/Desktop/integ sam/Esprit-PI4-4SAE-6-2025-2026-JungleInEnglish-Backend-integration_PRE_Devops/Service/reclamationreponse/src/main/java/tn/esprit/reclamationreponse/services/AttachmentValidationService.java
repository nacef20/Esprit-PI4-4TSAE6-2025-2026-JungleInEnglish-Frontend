package tn.esprit.reclamationreponse.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

@Service
public class AttachmentValidationService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "pdf");
    private static final String IMAGE_PNG = "image/png";
    private static final String IMAGE_JPEG = "image/jpeg";
    private static final String APPLICATION_PDF = "application/pdf";

    private final long maxSizeBytes;

    public AttachmentValidationService(@Value("${attachments.max-size-bytes:5242880}") long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public AttachmentPayload validateAndRead(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String fileName = sanitizeFileName(file.getOriginalFilename());
        if (fileName.isBlank()) {
            throw new IllegalArgumentException("attachment filename is required");
        }

        String extension = extractExtension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only PNG, JPG/JPEG, and PDF files are allowed");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("Attachment size exceeds maximum allowed size");
        }

        String contentType = resolveAndValidateContentType(file.getContentType(), extension);

        try {
            return new AttachmentPayload(fileName, contentType, file.getSize(), file.getBytes());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read uploaded attachment");
        }
    }

    private String sanitizeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "";
        }

        return Paths.get(originalName).getFileName().toString().trim();
    }

    private String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    private String resolveAndValidateContentType(String providedContentType, String extension) {
        String normalized = normalizeContentType(providedContentType);

        if (normalized.isBlank() || "application/octet-stream".equals(normalized)) {
            return defaultContentType(extension);
        }

        if ("png".equals(extension) && IMAGE_PNG.equals(normalized)) {
            return IMAGE_PNG;
        }

        if (("jpg".equals(extension) || "jpeg".equals(extension))
                && (IMAGE_JPEG.equals(normalized) || "image/jpg".equals(normalized))) {
            return IMAGE_JPEG;
        }

        if ("pdf".equals(extension) && APPLICATION_PDF.equals(normalized)) {
            return APPLICATION_PDF;
        }

        throw new IllegalArgumentException("Invalid content type for the provided attachment");
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }

        return contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
    }

    private String defaultContentType(String extension) {
        return switch (extension) {
            case "png" -> IMAGE_PNG;
            case "jpg", "jpeg" -> IMAGE_JPEG;
            case "pdf" -> APPLICATION_PDF;
            default -> "application/octet-stream";
        };
    }

    public record AttachmentPayload(
            String fileName,
            String contentType,
            long size,
            byte[] data
    ) {
    }
}
