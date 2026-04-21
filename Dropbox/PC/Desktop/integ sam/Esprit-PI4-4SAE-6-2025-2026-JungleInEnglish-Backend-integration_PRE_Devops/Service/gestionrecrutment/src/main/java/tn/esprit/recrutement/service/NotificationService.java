package tn.esprit.recrutement.service;

import tn.esprit.recrutement.model.*;
import tn.esprit.recrutement.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Called when a candidate successfully applies (CV uploaded + match saved).
     * Creates one notification for the employer and one for admin.
     */
    @Transactional
    public void createNotificationsForApplication(User candidate, JobOffer jobOffer, CVMatch match) {
        String candidateFullName = candidate.getName() != null ? candidate.getName() : candidate.getEmail();
        String jobOfferTitle = jobOffer.getTitle() != null ? jobOffer.getTitle() : "Job Offer";
        double score = match.getMatchScore();
        java.util.Date applicationDate = match.getMatchDate() != null ? match.getMatchDate() : new java.util.Date();
        User employer = jobOffer.getEmployer();
        Integer employerId = employer != null ? employer.getId() : null;

        Long jobOfferId = jobOffer.getId() != null ? jobOffer.getId() : null;
        Notification forEmployer = Notification.builder()
                .candidateFullName(candidateFullName)
                .jobOfferTitle(jobOfferTitle)
                .matchingScore(score)
                .applicationDate(applicationDate)
                .read(false)
                .recipientRole(RecipientRole.EMPLOYER)
                .employerId(employerId)
                .jobOfferId(jobOfferId)
                .build();
        notificationRepository.save(forEmployer);

        Notification forAdmin = Notification.builder()
                .candidateFullName(candidateFullName)
                .jobOfferTitle(jobOfferTitle)
                .matchingScore(score)
                .applicationDate(applicationDate)
                .read(false)
                .recipientRole(RecipientRole.ADMIN)
                .employerId(null)
                .jobOfferId(jobOfferId)
                .build();
        notificationRepository.save(forAdmin);
    }

    /**
     * Returns notifications visible to the given user (by role).
     * EMPLOYER: only notifications for their job offers (employerId = user.id).
     * ADMIN: all notifications.
     * CANDIDATE: empty list (candidates do not see these notifications).
     */
    public List<Notification> getNotificationsForUser(User user) {
        if (user == null || user.getRole() == null) {
            return List.of();
        }
        if (user.getRole() == Roles.CANDIDATE) {
            return List.of();
        }
        if (user.getRole() == Roles.EMPLOYER) {
            return notificationRepository.findByRecipientRoleAndEmployerIdOrderByApplicationDateDesc(
                    RecipientRole.EMPLOYER, user.getId());
        }
        if (user.getRole() == Roles.ADMIN) {
            return notificationRepository.findByRecipientRoleOrderByApplicationDateDesc(RecipientRole.ADMIN);
        }
        return List.of();
    }

    public long countUnreadForUser(User user) {
        if (user == null || user.getRole() == null) {
            return 0;
        }
        if (user.getRole() == Roles.CANDIDATE) {
            return 0;
        }
        if (user.getRole() == Roles.EMPLOYER) {
            return notificationRepository.countByRecipientRoleAndEmployerIdAndReadFalse(
                    RecipientRole.EMPLOYER, user.getId());
        }
        if (user.getRole() == Roles.ADMIN) {
            return notificationRepository.countByRecipientRoleAndReadFalse(RecipientRole.ADMIN);
        }
        return 0;
    }

    @Transactional
    public Notification markAsRead(Long id, User user) {
        return notificationRepository.findById(id)
                .filter(n -> isVisibleToUser(n, user))
                .map(n -> {
                    n.setRead(true);
                    return notificationRepository.save(n);
                })
                .orElse(null);
    }

    private boolean isVisibleToUser(Notification n, User user) {
        if (user == null || user.getRole() == null) return false;
        if (user.getRole() == Roles.CANDIDATE) return false;
        if (n.getRecipientRole() == RecipientRole.EMPLOYER) {
            return user.getRole() == Roles.EMPLOYER && user.getId() == n.getEmployerId();
        }
        if (n.getRecipientRole() == RecipientRole.ADMIN) {
            return user.getRole() == Roles.ADMIN;
        }
        return false;
    }
}
