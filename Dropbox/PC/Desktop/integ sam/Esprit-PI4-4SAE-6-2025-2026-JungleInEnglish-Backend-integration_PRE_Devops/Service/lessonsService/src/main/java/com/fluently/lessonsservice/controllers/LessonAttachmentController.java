package com.fluently.lessonsservice.controllers;

import com.fluently.lessonsservice.entities.AttachmentType;
import com.fluently.lessonsservice.entities.LessonAttachment;
import com.fluently.lessonsservice.services.ILessonAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonAttachmentController {

    @Autowired
    private ILessonAttachmentService attachmentService;

    @PostMapping(value = "/{lessonId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAttachment(
            @PathVariable Long lessonId,
            @RequestParam AttachmentType type,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        try {
            LessonAttachment saved = attachmentService.uploadAttachment(lessonId, type, file);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "message", ex.getMessage()
            ));
        }
    }

    @GetMapping("/{lessonId}/attachments")
    public ResponseEntity<List<LessonAttachment>> getAttachments(@PathVariable Long lessonId) {

        return ResponseEntity.ok(
                attachmentService.findAttachmentsByLesson(lessonId)
        );
    }

    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {

        LessonAttachment meta = attachmentService.findAttachmentById(attachmentId);
        Resource resource = attachmentService.loadAttachmentAsResource(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + meta.getOriginalName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
        LessonAttachment meta = attachmentService.findAttachmentById(attachmentId);

        // delete file from disk
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(meta.getStoragePath()).toAbsolutePath().normalize();
            java.nio.file.Files.deleteIfExists(path);
        } catch (Exception e) {
            // if disk delete fails, still allow DB delete (optional)
        }

        // delete from DB
        attachmentService.deleteAttachment(attachmentId);

        return ResponseEntity.noContent().build();
    }
}