package tn.esprit.recrutement.controller;

import tn.esprit.recrutement.model.Competence;
import tn.esprit.recrutement.service.CompetenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/competence")
public class CompetenceController {
    @Autowired
    private CompetenceService competenceService;

    @GetMapping("/all")
    public List<Competence> getAll() {
        return competenceService.getAll();
    }

    @GetMapping("/get/{id}")
    public Optional<Competence> getById(@PathVariable Long id) {
        return competenceService.getById(id);
    }

    @PostMapping("/add")
    public Competence create(@RequestBody Competence competence) {
        return competenceService.save(competence);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        competenceService.delete(id);
    }
} 