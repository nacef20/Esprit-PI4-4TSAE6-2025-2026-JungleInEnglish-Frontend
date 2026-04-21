package tn.esprit.eventmodule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.eventmodule.entity.Participation;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    long countByEvent_EventId(Long eventId);

    List<Participation> findByEvent_EventId(Long eventId);

    List<Participation> findByUserId(String userId);

    Optional<Participation> findByPublicToken(String publicToken);
}
