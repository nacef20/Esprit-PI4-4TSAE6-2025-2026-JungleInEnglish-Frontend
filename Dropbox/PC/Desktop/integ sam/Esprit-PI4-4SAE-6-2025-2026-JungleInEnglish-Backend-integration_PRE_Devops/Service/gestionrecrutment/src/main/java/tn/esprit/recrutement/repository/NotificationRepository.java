package tn.esprit.recrutement.repository;

import tn.esprit.recrutement.model.Notification;
import tn.esprit.recrutement.model.RecipientRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientRoleAndEmployerIdOrderByApplicationDateDesc(
            RecipientRole recipientRole, Integer employerId);

    List<Notification> findByRecipientRoleOrderByApplicationDateDesc(RecipientRole recipientRole);

    long countByRecipientRoleAndEmployerIdAndReadFalse(
            RecipientRole recipientRole, Integer employerId);

    long countByRecipientRoleAndReadFalse(RecipientRole recipientRole);
}
