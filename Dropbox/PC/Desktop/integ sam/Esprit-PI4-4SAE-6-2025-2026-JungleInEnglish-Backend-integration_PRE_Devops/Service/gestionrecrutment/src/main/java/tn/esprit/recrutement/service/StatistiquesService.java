package tn.esprit.recrutement.service;

import tn.esprit.recrutement.model.Statistiques;
import tn.esprit.recrutement.repository.CVRepository;
import tn.esprit.recrutement.repository.StatistiquesRepository;
import tn.esprit.recrutement.repository.UserRepository;
import tn.esprit.recrutement.repository.CVMatchRepository;
import tn.esprit.recrutement.model.User;
import tn.esprit.recrutement.model.CVMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.recrutement.model.CV;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class StatistiquesService {
    @Autowired
    private StatistiquesRepository statistiquesRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CVMatchRepository cvMatchRepository;
    @Autowired
    private CVRepository cvRepository;

    public List<Statistiques> getAll() {
        return statistiquesRepository.findAll();
    }

    public Optional<Statistiques> getById(Long id) {
        return statistiquesRepository.findById(id);
    }

    public Statistiques save(Statistiques statistiques) {
        return statistiquesRepository.save(statistiques);
    }

    public void delete(Long id) {
        statistiquesRepository.deleteById(id);
    }

    // Dashboard statistics
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        // Number of candidates
        long nbrCandidat = userRepository.findAll().stream().filter(u -> u.getRole().name().equals("CANDIDATE")).count();
        stats.put("nbrCandidat", nbrCandidat);
        // Average match rate
        List<CVMatch> matches = cvMatchRepository.findAll();
        double tauxMoyenMatching = matches.isEmpty() ? 0 : matches.stream().mapToDouble(CVMatch::getMatchScore).average().orElse(0);
        stats.put("tauxMoyenMatching", tauxMoyenMatching);
        // Popular skills
        List<CV> cvs = cvRepository.findAll();
        Map<String, Long> skillCounts = cvs.stream()
            .flatMap(cv -> cv.getSkills() != null ? cv.getSkills().stream() : java.util.stream.Stream.<String>empty())
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        List<String> competencePop = skillCounts.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .limit(5)
            .map(Map.Entry::getKey)
            .toList();
        stats.put("competencePop", competencePop);
        // Profile trends (e.g., most common education)
        Map<String, Long> educationCounts = cvs.stream()
            .flatMap(cv -> cv.getEducation() != null ? cv.getEducation().stream() : java.util.stream.Stream.<String>empty())
            .filter(e -> e != null && !e.isEmpty())
            .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        String tendancesProfils = educationCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("");
        stats.put("tendancesProfils", tendancesProfils);
        return stats;
    }
} 