package com.fluently.feedbackservice.repositories;

import com.fluently.feedbackservice.entities.FeedbackReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackReactionRepository extends JpaRepository<FeedbackReaction, Long> {
    Optional<FeedbackReaction> findByFeedbackIdAndReactorStudentId(Long feedbackId, String reactorStudentId);
}
