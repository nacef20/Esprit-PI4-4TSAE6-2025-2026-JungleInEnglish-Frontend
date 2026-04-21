package com.fluently.lessonsservice.services;

import com.fluently.lessonsservice.entities.AttachmentType;
import com.fluently.lessonsservice.entities.LessonAttachment;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ILessonAttachmentService {

    LessonAttachment uploadAttachment(Long lessonId, AttachmentType type, MultipartFile file) throws IOException;
    List<LessonAttachment> findAttachmentsByLesson(Long lessonId);
    LessonAttachment findAttachmentById(Long attachmentId);
    Resource loadAttachmentAsResource(Long attachmentId);
    void deleteAttachment(Long attachmentId);
}