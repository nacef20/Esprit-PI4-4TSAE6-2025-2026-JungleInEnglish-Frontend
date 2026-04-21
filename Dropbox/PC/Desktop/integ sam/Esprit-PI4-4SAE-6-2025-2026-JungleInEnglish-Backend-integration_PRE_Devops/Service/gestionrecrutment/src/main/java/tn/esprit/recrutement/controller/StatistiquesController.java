package tn.esprit.recrutement.controller;

import tn.esprit.recrutement.model.Statistiques;
import tn.esprit.recrutement.service.StatistiquesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/statistiques")
public class StatistiquesController {
    @Autowired
    private StatistiquesService statistiquesService;

    @GetMapping("/all")
    public List<Statistiques> getAll() {
        return statistiquesService.getAll();
    }

    @GetMapping("/{id}")
    public Optional<Statistiques> getById(@PathVariable Long id) {
        return statistiquesService.getById(id);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        return ResponseEntity.ok(statistiquesService.getDashboardStats());
    }

    @PostMapping("/add")
    public Statistiques create(@RequestBody Statistiques statistiques) {
        return statistiquesService.save(statistiques);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        statistiquesService.delete(id);
    }
} 