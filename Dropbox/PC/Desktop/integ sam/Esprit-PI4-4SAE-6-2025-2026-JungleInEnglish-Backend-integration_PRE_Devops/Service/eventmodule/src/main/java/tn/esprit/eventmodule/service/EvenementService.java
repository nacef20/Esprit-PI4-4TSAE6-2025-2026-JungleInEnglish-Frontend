package tn.esprit.eventmodule.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.eventmodule.entity.Evenement;
import tn.esprit.eventmodule.repository.EvenementRepository;
import tn.esprit.eventmodule.repository.ParticipationRepository;
import tn.esprit.eventmodule.exception.EventConflictException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvenementService {

    private final EvenementRepository evenementRepository;
    private final ParticipationRepository participationRepository;
    private final ImageStorageService imageStorageService;

    public EvenementService(EvenementRepository evenementRepository, ParticipationRepository participationRepository, ImageStorageService imageStorageService) {
        this.evenementRepository = evenementRepository;
        this.participationRepository = participationRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional
    public Evenement create(Evenement evenement) {
        if (evenementRepository.hasConflict(evenement.getLevel(), evenement.getStartTime(), evenement.getEndTime(), null)) {
            throw new EventConflictException("An event with level " + evenement.getLevel() + " already exists in the given time frame.");
        }
        evenement.setImageUrl(imageStorageService.shrinkIfNeeded(evenement.getImageUrl()));
        return evenementRepository.save(evenement);
    }

    private int calculateAvailableSeats(Evenement event) {
        long participants = participationRepository.countByEvent_EventId(event.getEventId());
        return Math.max(0, event.getCapacity() - (int) participants);
    }

    public List<Evenement> findAll() {
        List<Evenement> evs = evenementRepository.findAll();
        evs.forEach(e -> e.setAvailableSeats(calculateAvailableSeats(e)));
        return evs;
    }

    public Evenement findById(Long id) {
        Evenement e = evenementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evenement not found with id: " + id));
        e.setAvailableSeats(calculateAvailableSeats(e));
        return e;
    }

    @Transactional
    public Evenement update(Long id, Evenement evenement) {
        if (evenementRepository.hasConflict(evenement.getLevel(), evenement.getStartTime(), evenement.getEndTime(), id)) {
            throw new EventConflictException("An event with level " + evenement.getLevel() + " already exists in the given time frame.");
        }
        
        Evenement existing = findById(id);
        existing.setTitle(evenement.getTitle());
        existing.setDescription(evenement.getDescription());
        existing.setLevel(evenement.getLevel());
        existing.setStartTime(evenement.getStartTime());
        existing.setEndTime(evenement.getEndTime());
        existing.setLocation(evenement.getLocation());
        existing.setCapacity(evenement.getCapacity());
        existing.setReminderSent(evenement.isReminderSent());
        existing.setImageUrl(imageStorageService.shrinkIfNeeded(evenement.getImageUrl()));
        if (evenement.getOrganizerId() != null) {
            existing.setOrganizerId(evenement.getOrganizerId());
        }
        return evenementRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!evenementRepository.existsById(id)) {
            throw new RuntimeException("Evenement not found with id: " + id);
        }
        evenementRepository.deleteById(id);
    }

    public int getAvailableSeats(Long eventId) {
        Evenement event = evenementRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evenement not found with id: " + eventId));
        return calculateAvailableSeats(event);
    }

    public boolean isFull(Long eventId) {
        return getAvailableSeats(eventId) == 0;
    }

    /**
     * Métier avancé: Algorithme de suggestion de créneau disponible.
     * Cherche le premier créneau de {durationInHours} heures disponible entre fromDate et toDate
     * où aucun événement du même {level} n'est déjà planifié.
     */
    public LocalDateTime suggestBestTimeSlot(LocalDateTime fromDate, LocalDateTime toDate, int durationInHours, String level) {
        LocalDateTime now = LocalDateTime.now();
        if (fromDate.isBefore(now)) {
            fromDate = now;
        }
        if (toDate.isBefore(fromDate)) {
            throw new RuntimeException("Dates must be in the future and valid.");
        }

        List<Evenement> overlappingEvents = evenementRepository.findEventsByLevelAndDateRange(level, fromDate, toDate);

        LocalDateTime currentStartTime = fromDate;

        for (Evenement event : overlappingEvents) {
            LocalDateTime proposedEndTime = currentStartTime.plusHours(durationInHours);

            // Si le temps avant le début de cet événement est suffisant pour notre durée
            if (!proposedEndTime.isAfter(event.getStartTime())) {
                return currentStartTime; // On a trouvé un créneau !
            }

            // Sinon, on décale l'heure de recherche à la fin de cet événement
            // (on prend le max pour ne pas reculer si des événements se superposent)
            if (event.getEndTime().isAfter(currentStartTime)) {
                currentStartTime = event.getEndTime();
            }
        }

        // Vérification du dernier créneau après le dernier événement
        if (!currentStartTime.plusHours(durationInHours).isAfter(toDate)) {
            return currentStartTime;
        }

        // Aucun créneau disponible trouvé
        throw new RuntimeException("Aucun créneau disponible de " + durationInHours + " heures trouvé pour ce niveau de " + fromDate + " à " + toDate);
    }
}
