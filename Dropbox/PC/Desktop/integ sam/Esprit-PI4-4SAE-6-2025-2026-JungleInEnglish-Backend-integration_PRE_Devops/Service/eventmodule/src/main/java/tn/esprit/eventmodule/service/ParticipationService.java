package tn.esprit.eventmodule.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.eventmodule.entity.Evenement;
import tn.esprit.eventmodule.entity.Participation;
import tn.esprit.eventmodule.repository.ParticipationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final EvenementService evenementService;
    private final QrCodeGeneratorService qrCodeGeneratorService;

    public ParticipationService(ParticipationRepository participationRepository, EvenementService evenementService, QrCodeGeneratorService qrCodeGeneratorService) {
        this.participationRepository = participationRepository;
        this.evenementService = evenementService;
        this.qrCodeGeneratorService = qrCodeGeneratorService;
    }

    @Transactional
    public Participation create(Participation participation) {
        if (participation.getEvent() != null && participation.getEvent().getEventId() != null) {
            participation.setEvent(evenementService.findById(participation.getEvent().getEventId()));
        }
        return participationRepository.save(participation);
    }

    public List<Participation> findAll() {
        return participationRepository.findAll();
    }

    public Participation findById(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participation not found with id: " + id));
    }

    @Transactional
    public Participation update(Long id, Participation participation) {
        Participation existing = findById(id);
        if (participation.getUserId() != null) {
            existing.setUserId(participation.getUserId());
        }
        if (participation.getEvent() != null && participation.getEvent().getEventId() != null) {
            existing.setEvent(evenementService.findById(participation.getEvent().getEventId()));
        }
        if (participation.getCheckInTime() != null) {
            existing.setCheckInTime(participation.getCheckInTime());
        }
        return participationRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!participationRepository.existsById(id)) {
            throw new RuntimeException("Participation not found with id: " + id);
        }
        participationRepository.deleteById(id);
    }

    @Transactional
    public Participation registerToEvent(String userId, Long eventId) {
        Evenement event = evenementService.findById(eventId);
        if (evenementService.isFull(eventId)) {
            throw new RuntimeException("Event is full");
        }
        Participation participation = new Participation();
        participation.setUserId(userId);
        participation.setEvent(event);
        participation.setRegistrationDate(LocalDateTime.now());

        // Generation du Token public unique securise
        String token = java.util.UUID.randomUUID().toString();
        participation.setPublicToken(token);

        // Generation du QR Code Base64
        String qrPayload = token; // The scanner only needs the token
        String qrBase64 = qrCodeGeneratorService.generateQrCodeBase64(qrPayload);
        participation.setQrCodeUrl(qrBase64);

        return participationRepository.save(participation);
    }

    @Transactional
    public void unregisterFromEvent(Long participationId) {
        deleteById(participationId);
    }

    @Transactional
    public Participation checkIn(Long participationId) {
        Participation p = findById(participationId);
        p.setPresent(true);
        p.setCheckInTime(LocalDateTime.now());
        return participationRepository.save(p);
    }

    @Transactional
    public Participation scanQrCode(String token) {
        Participation p = participationRepository.findByPublicToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid QR Code Token"));
        
        if (p.isPresent()) {
            throw new RuntimeException("Participant already checked in!");
        }
        
        p.setPresent(true);
        p.setCheckInTime(LocalDateTime.now());
        return participationRepository.save(p);
    }

    public Participation getByToken(String token) {
        return participationRepository.findByPublicToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid QR Code Token"));
    }

    public List<Participation> getParticipantsByEvent(Long eventId) {
        return participationRepository.findByEvent_EventId(eventId);
    }

    public List<Participation> getParticipationsByUser(String userId) {
        return participationRepository.findByUserId(userId);
    }
}
