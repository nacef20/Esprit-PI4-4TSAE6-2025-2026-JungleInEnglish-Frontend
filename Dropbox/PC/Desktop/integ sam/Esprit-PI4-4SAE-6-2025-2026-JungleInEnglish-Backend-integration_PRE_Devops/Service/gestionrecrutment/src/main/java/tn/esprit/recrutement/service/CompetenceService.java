package tn.esprit.recrutement.service;

import tn.esprit.recrutement.model.Competence;
import tn.esprit.recrutement.repository.CompetenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompetenceService {
    @Autowired
    private CompetenceRepository competenceRepository;

    public List<Competence> getAll() {
        return competenceRepository.findAll();
    }

    public Optional<Competence> getById(Long id) {
        return competenceRepository.findById(id);
    }

    public Competence save(Competence competence) {
        return competenceRepository.save(competence);
    }

    public void delete(Long id) {
        competenceRepository.deleteById(id);
    }
} 