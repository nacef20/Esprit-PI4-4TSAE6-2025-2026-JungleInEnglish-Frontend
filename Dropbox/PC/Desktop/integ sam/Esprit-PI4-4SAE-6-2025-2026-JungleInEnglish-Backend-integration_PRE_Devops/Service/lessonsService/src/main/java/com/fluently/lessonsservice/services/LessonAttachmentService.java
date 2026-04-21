package com.fluently.lessonsservice.services;

import com.fluently.lessonsservice.entities.*;
import com.fluently.lessonsservice.repositories.LessonAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Locale;

@Service
@Transactional
public class LessonAttachmentService implements ILessonAttachmentService {

    @Autowired
    private LessonAttachmentRepository attachmentRepository;

    @Value("${app.upload.base-dir:uploads}")
    private String baseUploadDir;

    private void validateAttachmentType(AttachmentType type, MultipartFile file) {
        String name = file.getOriginalFilename();
        String ct = file.getContentType();

        String filename = (name == null) ? "" : name.toLowerCase(Locale.ROOT);
        String contentType = (ct == null) ? "" : ct.toLowerCase(Locale.ROOT);

        boolean ok;

        switch (type) {
            case PDF:
                ok = filename.endsWith(".pdf") || contentType.equals("application/pdf");
                break;

            case WORD:
                ok = filename.endsWith(".doc") || filename.endsWith(".docx")
                        || contentType.contains("msword")
                        || contentType.contains("officedocument.wordprocessingml");
                break;

            case PPT:
                ok = filename.endsWith(".ppt") || filename.endsWith(".pptx")
                        || contentType.contains("ms-powerpoint")
                        || contentType.contains("officedocument.presentationml");
                break;

            case VIDEO:
                ok = filename.endsWith(".mp4") || filename.endsWith(".webm") || filename.endsWith(".mov")
                        || contentType.startsWith("video/");
                break;

            case OTHER:
            default:
                ok = true;
                break;
        }

        if (!ok) {
            throw new IllegalArgumentException("Select the correct type for the uploaded file.");
        }
    }

    @Override
    public LessonAttachment uploadAttachment(Long lessonId, AttachmentType type, MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        validateAttachmentType(type, file);
        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "_" + originalName;

        Path lessonDir = Paths.get(baseUploadDir, "lessons", String.valueOf(lessonId))
                .toAbsolutePath().normalize();

        Files.createDirectories(lessonDir);

        Path targetPath = lessonDir.resolve(storedName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        LessonAttachment attachment = new LessonAttachment();
        attachment.setLessonId(lessonId);
        attachment.setType(type);
        attachment.setOriginalName(originalName);
        attachment.setStoredName(storedName);
        attachment.setContentType(file.getContentType());
        attachment.setSize(file.getSize());
        attachment.setStoragePath(targetPath.toString());
        attachment.setUploadDate(LocalDateTime.now());

        return attachmentRepository.save(attachment);
    }

    @Override
    public List<LessonAttachment> findAttachmentsByLesson(Long lessonId) {
        return attachmentRepository.findByLessonIdOrderByUploadDateDesc(lessonId);
    }

    @Override
    public LessonAttachment findAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }

    @Override
    public Resource loadAttachmentAsResource(Long attachmentId) {

        LessonAttachment attachment = findAttachmentById(attachmentId);

        try {
            Path path = Paths.get(attachment.getStoragePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("File not found on disk");
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid file path");
        }
    }
    @Override
    public void deleteAttachment(Long attachmentId) {
        attachmentRepository.deleteById(attachmentId);
    }
}