package tn.esprit.recrutement.service;

import tn.esprit.recrutement.model.Experience;
import tn.esprit.recrutement.repository.ExperienceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExperienceService {
    @Autowired
    private ExperienceRepository experienceRepository;

    public List<Experience> getAll() {
        return experienceRepository.findAll();
    }

    public Optional<Experience> getById(Long id) {
        return experienceRepository.findById(id);
    }

    public Experience save(Experience experience) {
        return experienceRepository.save(experience);
    }

    public void delete(Long id) {
        experienceRepository.deleteById(id);
    }
} 