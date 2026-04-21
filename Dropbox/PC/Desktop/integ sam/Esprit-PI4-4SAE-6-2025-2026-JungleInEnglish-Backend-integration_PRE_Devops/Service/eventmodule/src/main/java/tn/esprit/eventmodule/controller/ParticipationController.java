package tn.esprit.eventmodule.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventmodule.entity.Participation;
import tn.esprit.eventmodule.service.ParticipationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/participations")
public class ParticipationController {

    private final ParticipationService participationService;

    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @PostMapping("/add")
    public ResponseEntity<Participation> create(@Valid @RequestBody Participation participation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(participationService.create(participation));
    }

    @GetMapping("/findall")
    public ResponseEntity<List<Participation>> findAll() {
        return ResponseEntity.ok(participationService.findAll());
    }

    @GetMapping("/find")
    public ResponseEntity<Participation> findById(@RequestParam Long id) {
        return ResponseEntity.ok(participationService.findById(id));
    }

    @PutMapping("/update")
    public ResponseEntity<Participation> update(@RequestParam Long id, @Valid @RequestBody Participation participation) {
        return ResponseEntity.ok(participationService.update(id, participation));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestParam Long id) {
        participationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Participation> registerToEvent(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        Long eventId = body.get("eventId") instanceof Number ? ((Number) body.get("eventId")).longValue() : null;
        if (userId == null || eventId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(participationService.registerToEvent(userId, eventId));
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<Void> unregisterFromEvent(@RequestParam Long id) {
        participationService.unregisterFromEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkin")
    public ResponseEntity<Participation> checkIn(@RequestParam Long id) {
        return ResponseEntity.ok(participationService.checkIn(id));
    }

    @PostMapping("/scan")
    public ResponseEntity<?> scanQrCode(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is missing"));
        }
        try {
            Participation participation = participationService.scanQrCode(token);
            return ResponseEntity.ok(participation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/by-token")
    public ResponseEntity<?> getByToken(@RequestParam String token) {
        try {
            return ResponseEntity.ok(participationService.getByToken(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Token introuvable ou invalide"));
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Participation>> getByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(participationService.getParticipantsByEvent(eventId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Participation>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(participationService.getParticipationsByUser(userId));
    }
}
