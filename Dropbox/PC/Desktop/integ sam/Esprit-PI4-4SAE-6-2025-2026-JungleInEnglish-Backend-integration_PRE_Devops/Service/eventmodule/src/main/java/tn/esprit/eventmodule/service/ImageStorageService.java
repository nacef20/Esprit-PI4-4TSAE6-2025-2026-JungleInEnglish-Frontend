package tn.esprit.eventmodule.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Saves large base64 data URLs to files and returns a short URL.
 * Use this so the database only stores a URL (small string) and we avoid MySQL max_allowed_packet limits.
 */
@Service
public class ImageStorageService {

    private static final Pattern DATA_URL = Pattern.compile("^data:([^;]+);base64,(.+)$");
    private static final int LARGE_THRESHOLD = 900_000; // under 1MB: keep in DB to avoid file I/O for small images

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private Path eventsDir;

    @PostConstruct
    public void init() throws IOException {
        eventsDir = Paths.get(uploadDir, "events");
        Files.createDirectories(eventsDir);
    }

    /**
     * If imageUrl is a large base64 data URL, save it to a file and return the public URL path.
     * Otherwise return the original imageUrl (short URLs or small base64).
     */
    public String shrinkIfNeeded(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return imageUrl;
        }
        if (imageUrl.length() <= LARGE_THRESHOLD) {
            return imageUrl;
        }
        if (!imageUrl.startsWith("data:")) {
            return imageUrl;
        }
        Matcher m = DATA_URL.matcher(imageUrl);
        if (!m.matches()) {
            return imageUrl;
        }
        String mime = m.group(1);
        String base64 = m.group(2);
        String ext = extensionFromMime(mime);
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            if (bytes == null || bytes.length == 0) {
                return imageUrl;
            }
            String filename = UUID.randomUUID() + ext;
            Path file = eventsDir.resolve(filename);
            Files.write(file, bytes);
            return "/uploads/events/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save event image: " + e.getMessage(), e);
        }
    }

    private static String extensionFromMime(String mime) {
        if (mime == null) return ".bin";
        return switch (mime.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".bin";
        };
    }
}
