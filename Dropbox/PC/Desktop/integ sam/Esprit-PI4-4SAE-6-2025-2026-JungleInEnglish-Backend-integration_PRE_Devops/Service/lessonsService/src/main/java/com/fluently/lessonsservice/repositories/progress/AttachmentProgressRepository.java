package com.fluently.lessonsservice.repositories.progress;

import com.fluently.lessonsservice.entities.progress.AttachmentProgress;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AttachmentProgressRepository extends JpaRepository<AttachmentProgress, Long> {

    Optional<AttachmentProgress> findByUserIdAndAttachmentId(String userId, Long attachmentId);

    @Query("select count(ap) from AttachmentProgress ap " +
            "where ap.courseId = :courseId and ap.userId = :userId")
    long countCompletedAttachmentsByCourse(@Param("courseId") Long courseId,
                                           @Param("userId") String userId);
}