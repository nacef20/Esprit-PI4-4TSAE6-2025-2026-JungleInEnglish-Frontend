package com.fluently.feedbackservice.repositories;


import com.fluently.feedbackservice.entities.FlaggedFeedback;
import com.fluently.feedbackservice.entities.FlagStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlaggedFeedbackRepository extends JpaRepository<FlaggedFeedback, Long> {

    List<FlaggedFeedback> findByStudentId(String studentId);

    //repeat offender check
    long countByStudentIdAndStatus(String studentId, FlagStatus status);

    // ml most recent confirmed flag : cooldown calculation
    @Query("""
        SELECT f FROM FlaggedFeedback f
        WHERE f.studentId = :studentId
        AND f.status = 'CONFIRMED'
        ORDER BY f.flaggedAt DESC
        LIMIT 1
    """)
    java.util.Optional<FlaggedFeedback> findLatestConfirmedFlag(@Param("studentId") String studentId);

    List<FlaggedFeedback> findByStatus(FlagStatus status);

    // scheduler : expire CONFIRMED flags older than the cooldown window
    @Modifying
    @Transactional
    @Query("UPDATE FlaggedFeedback f SET f.status = com.fluently.feedbackservice.entities.FlagStatus.EXPIRED " +
           "WHERE f.status = com.fluently.feedbackservice.entities.FlagStatus.CONFIRMED " +
           "AND f.flaggedAt < :cutoff")
    void expireOldFlags(@Param("cutoff") java.time.LocalDateTime cutoff);
}