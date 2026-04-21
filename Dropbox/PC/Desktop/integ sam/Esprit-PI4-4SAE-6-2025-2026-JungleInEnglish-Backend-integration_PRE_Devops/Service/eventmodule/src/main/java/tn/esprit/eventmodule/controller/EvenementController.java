package tn.esprit.eventmodule.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventmodule.entity.Evenement;
import tn.esprit.eventmodule.service.EvenementService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evenements")
public class EvenementController {

    private final EvenementService evenementService;

    public EvenementController(EvenementService evenementService) {
        this.evenementService = evenementService;
    }

    @PostMapping("/add")
    public ResponseEntity<Evenement> create(@Valid @RequestBody Evenement evenement) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evenementService.create(evenement));
    }

    @GetMapping("/find")
    public ResponseEntity<List<Evenement>> findAll() {
        return ResponseEntity.ok(evenementService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evenement> findById(@PathVariable Long id) {
        return ResponseEntity.ok(evenementService.findById(id));
    }

    @PutMapping("/update")
    public ResponseEntity<Evenement> update(@RequestParam Long id, @Valid @RequestBody Evenement evenement) {
        return ResponseEntity.ok(evenementService.update(id, evenement));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteById(@RequestParam Long id) {
        evenementService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/available-seats")
    public ResponseEntity<Map<String, Integer>> getAvailableSeats(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("availableSeats", evenementService.getAvailableSeats(id)));
    }

    @GetMapping("/{id}/is-full")
    public ResponseEntity<Map<String, Boolean>> isFull(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("full", evenementService.isFull(id)));
    }

    @GetMapping("/suggest-time-slot")
    public ResponseEntity<Map<String, LocalDateTime>> suggestTimeSlot(
            @RequestParam LocalDateTime fromDate,
            @RequestParam LocalDateTime toDate,
            @RequestParam int durationInHours,
            @RequestParam String level) {
        
        try {
            LocalDateTime suggestedTime = evenementService.suggestBestTimeSlot(fromDate, toDate, durationInHours, level);
            return ResponseEntity.ok(Map.of("suggestedTime", suggestedTime));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
