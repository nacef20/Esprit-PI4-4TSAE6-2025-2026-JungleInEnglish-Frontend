package tn.esprit.recrutement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.recrutement.model.ProfilAnalyse;
import tn.esprit.recrutement.service.ProfilAnalyseService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/profil-analyse")
public class ProfilAnalyseController {
    @Autowired
    private ProfilAnalyseService profilAnalyseService;

    @GetMapping("/all")
    public List<ProfilAnalyse> getAll() {
        return profilAnalyseService.getAll();
    }

    @GetMapping("/get/{id}")
    public Optional<ProfilAnalyse> getById(@PathVariable Long id) {
        return profilAnalyseService.getById(id);
    }

    @PostMapping("/add")
    public ProfilAnalyse create(@RequestBody ProfilAnalyse profilAnalyse) {
        return profilAnalyseService.save(profilAnalyse);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        profilAnalyseService.delete(id);
    }
} 