package com.fluently.feedbackservice.services;

import com.fluently.feedbackservice.entities.BadgeType;
import com.fluently.feedbackservice.entities.ProfessorBadge;
import com.fluently.feedbackservice.repositories.FeedbackRepository;
import com.fluently.feedbackservice.repositories.ProfessorBadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessorBadgeService {

    @Autowired
    private ProfessorBadgeRepository badgeRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Transactional
    public ProfessorBadge recalculateBadge(String professorId) {
        int totalScore = feedbackRepository.calculateTotalScoreForProfessor(professorId);
        BadgeType earned = BadgeType.calculateBadge(totalScore);

        Double avgStars = feedbackRepository.calculateAverageStarRatingForProfessor(professorId);
        String starEarned = calculateStarBadge(avgStars);

        ProfessorBadge badge = badgeRepository.findByProfessorId(professorId)
                .orElseGet(() -> {
                    ProfessorBadge newBadge = new ProfessorBadge();
                    newBadge.setProfessorId(professorId);
                    return newBadge;
                });

        badge.setBadgeType(earned);
        badge.setStarBadge(starEarned);
        return badgeRepository.save(badge);
    }

    private String calculateStarBadge(Double avgStars) {
        if (avgStars >= 4.8) return "LEGEND";
        if (avgStars >= 4.5) return "PLATINUM";
        if (avgStars >= 4.0) return "GOLD";
        if (avgStars >= 3.0) return "SILVER";
        if (avgStars >= 2.0) return "BRONZE";
        if (avgStars > 0) return "ROOKIE";
        return "NONE";
    }

    @Transactional
    public ProfessorBadge getBadgeForProfessor(String professorId) {
        // Force recalculation
        return recalculateBadge(professorId);
    }
}