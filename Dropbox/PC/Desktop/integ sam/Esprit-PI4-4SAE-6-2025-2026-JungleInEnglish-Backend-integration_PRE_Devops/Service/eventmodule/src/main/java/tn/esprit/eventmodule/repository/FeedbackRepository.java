package tn.esprit.eventmodule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.eventmodule.entity.Feedback;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    List<Feedback> findByEventId(Long eventId);
    
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.eventId = :eventId")
    Double getAverageRatingForEvent(Long eventId);

    long countByEventIdAndRating(Long eventId, int rating);
    
    long countByEventId(Long eventId);
}
