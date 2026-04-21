package tn.esprit.recrutement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.recrutement.model.ProfilAnalyse;
import tn.esprit.recrutement.repository.ProfilAnalyseRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProfilAnalyseService {
    @Autowired
    private ProfilAnalyseRepository profilAnalyseRepository;

    public List<ProfilAnalyse> getAll() {
        return profilAnalyseRepository.findAll();
    }

    public Optional<ProfilAnalyse> getById(Long id) {
        return profilAnalyseRepository.findById(id);
    }

    public ProfilAnalyse save(ProfilAnalyse profilAnalyse) {
        return profilAnalyseRepository.save(profilAnalyse);
    }

    public void delete(Long id) {
        profilAnalyseRepository.deleteById(id);
    }
} 