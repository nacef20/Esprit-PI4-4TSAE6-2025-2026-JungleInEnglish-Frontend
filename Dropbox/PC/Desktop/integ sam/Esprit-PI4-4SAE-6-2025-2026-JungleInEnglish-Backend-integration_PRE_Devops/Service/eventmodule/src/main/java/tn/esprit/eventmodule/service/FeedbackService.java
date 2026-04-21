package tn.esprit.eventmodule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.eventmodule.entity.Feedback;
import tn.esprit.eventmodule.repository.FeedbackRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EvenementService evenementService;

    public FeedbackService(FeedbackRepository feedbackRepository, EvenementService evenementService) {
        this.feedbackRepository = feedbackRepository;
        this.evenementService = evenementService;
    }

    @Transactional
    public Feedback saveFeedback(Feedback feedback) {
        // Validate event exists
        evenementService.findById(feedback.getEventId());
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getFeedbackByEvent(Long eventId) {
        return feedbackRepository.findByEventId(eventId);
    }

    public Map<String, Object> getSatisfactionStats(Long eventId) {
        evenementService.findById(eventId); // Validate existence
        
        Double avgRating = feedbackRepository.getAverageRatingForEvent(eventId);
        long totalResponses = feedbackRepository.countByEventId(eventId);
        
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, feedbackRepository.countByEventIdAndRating(eventId, i));
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("eventId", eventId);
        stats.put("averageRating", avgRating != null ? avgRating : 0.0);
        stats.put("totalResponses", totalResponses);
        stats.put("ratingDistribution", distribution);
        
        return stats;
    }
}
