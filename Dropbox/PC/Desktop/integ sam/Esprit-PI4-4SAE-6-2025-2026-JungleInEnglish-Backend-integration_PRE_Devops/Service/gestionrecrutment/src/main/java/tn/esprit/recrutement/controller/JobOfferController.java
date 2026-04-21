package tn.esprit.recrutement.controller;

import tn.esprit.recrutement.model.JobOffer;
import tn.esprit.recrutement.model.CVMatch;
import tn.esprit.recrutement.service.JobOfferService;
import tn.esprit.recrutement.model.User;
import tn.esprit.recrutement.repository.UserRepository;
import tn.esprit.recrutement.repository.CVMatchRepository;
import tn.esprit.recrutement.service.CVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;
import tn.esprit.recrutement.repository.JobOfferRepository;

@RestController
@RequestMapping("/api/jobs")
public class JobOfferController {

    @Autowired
    private JobOfferService jobOfferService;

    @Autowired
    private CVService cvService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CVMatchRepository cvMatchRepository;

    @Autowired
    private JobOfferRepository jobOfferRepository;

    @PostMapping("/add")
    public ResponseEntity<JobOffer> createJobOffer(@RequestBody JobOffer jobOffer,
            @RequestParam("employerId") String employerId) {
        if (jobOffer.getClosingDate() != null && jobOffer.getClosingDate().before(new Date())) {
            return ResponseEntity.badRequest().build();
        }
        
        User employer = userRepository.findByKeycloakUserId(employerId).orElseGet(() -> {
            User newUser = new User();
            newUser.setKeycloakUserId(employerId);
            newUser.setName("Keycloak User");
            newUser.setEmail(employerId + "@placeholder.com"); // satisfy unique constraint temporarily
            newUser.setPassword("N/A");
            return userRepository.save(newUser);
        });
        
        JobOffer created = jobOfferService.createJobOffer(jobOffer, employer);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<JobOffer>> getActiveJobOffers() {
        List<JobOffer> offers = jobOfferService.getActiveJobOffers();
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/all-with-inactive")
    public ResponseEntity<List<JobOffer>> getAllJobOffersWithInactive() {
        List<JobOffer> offers = jobOfferRepository.findAll();
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/getEmployer/{id}")
    public ResponseEntity<List<JobOffer>> getEmployerJobOffers(@PathVariable Integer id) {
        User employer = userRepository.findById(id).orElse(null);
        if (employer == null) {
            return ResponseEntity.badRequest().body(null);
        }
        List<JobOffer> offers = jobOfferService.getEmployerJobOffers(employer);
        return ResponseEntity.ok(offers);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<JobOffer> updateJobOffer(@PathVariable Long id,
            @RequestBody JobOffer jobOffer) {
        if (jobOffer.getClosingDate() != null && jobOffer.getClosingDate().before(new Date())) {
            return ResponseEntity.badRequest().build();
        }
        jobOffer.setId(id);
        JobOffer updated = jobOfferService.updateJobOffer(jobOffer);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<JobOffer> updateJobOfferStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        JobOffer jobOffer = jobOfferRepository.findById(id).orElse(null);
        if (jobOffer == null) {
            return ResponseEntity.notFound().build();
        }
        if (body.containsKey("active")) {
            jobOffer.setActive(Boolean.parseBoolean(body.get("active").toString()));
            jobOfferRepository.save(jobOffer);
        }
        return ResponseEntity.ok(jobOffer);
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateJobOffer(@PathVariable Long id) {
        jobOfferService.deactivateJobOffer(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteJobOffer(@PathVariable Long id) {
        jobOfferService.deleteJobOffer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<List<Map<String, Object>>> getJobMatches(@PathVariable Long id) {
        // Find all CVMatch entries for this job offer
        List<CVMatch> matches = cvMatchRepository.findAll().stream()
                .filter(m -> m.getJobOffer() != null && m.getJobOffer().getId().equals(id))
                .toList();
        // Group by candidateId and keep only the latest match (by matchDate)
        Map<Long, CVMatch> latestMatchByCandidate = new HashMap<>();
        boolean needsUpdate = false;
        for (CVMatch m : matches) {
            if (m.getCv() != null && m.getCv().getUser() != null) {
                Long candidateId = Long.valueOf(m.getCv().getUser().getId());

                // Detect if score is legacy/stale and trigger FULL recalculation
                // If sub-scores are 0 but matchScore > 0, it's definitely stale
                if (m.getMatchScore() > 0 &&
                        m.getSkillsMatch() == 0 && m.getExperienceMatch() == 0) {
                    needsUpdate = true;
                }

                if (!latestMatchByCandidate.containsKey(candidateId) ||
                        m.getMatchDate().after(latestMatchByCandidate.get(candidateId).getMatchDate())) {
                    latestMatchByCandidate.put(candidateId, m);
                }
            }
        }

        if (needsUpdate) {
            JobOffer job = jobOfferRepository.findById(id).orElse(null);
            if (job != null) {
                cvService.updateMatchesForJob(job);
                // Refetch updated matches
                matches = cvMatchRepository.findByJobOfferId(id);
                latestMatchByCandidate.clear();
                for (CVMatch m : matches) {
                    if (m.getCv() != null && m.getCv().getUser() != null) {
                        Long candidateId = Long.valueOf(m.getCv().getUser().getId());
                        if (!latestMatchByCandidate.containsKey(candidateId) ||
                                m.getMatchDate().after(latestMatchByCandidate.get(candidateId).getMatchDate())) {
                            latestMatchByCandidate.put(candidateId, m);
                        }
                    }
                }
            }
        }
        // Map to candidate name, email, score, and extracted CV content
        List<Map<String, Object>> result = latestMatchByCandidate.values().stream()
                .sorted((m1, m2) -> Double.compare(m2.getMatchScore(), m1.getMatchScore()))
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    if (m.getCv() != null && m.getCv().getUser() != null) {
                        map.put("candidateName", m.getCv().getUser().getName());
                        map.put("candidateEmail", m.getCv().getUser().getEmail());
                        map.put("candidateId", m.getCv().getUser().getId());
                    }
                    map.put("matchId", m.getId());
                    map.put("matchScore", m.getMatchScore());
                    map.put("skillsMatch", m.getSkillsMatch());
                    map.put("experienceMatch", m.getExperienceMatch());
                    map.put("educationMatch", m.getEducationMatch());
                    map.put("languageMatch", m.getLanguageMatch());
                    map.put("certificationMatch", m.getCertificationMatch());
                    map.put("status", m.getStatus());
                    map.put("cvId", m.getCv() != null ? m.getCv().getId() : null);
                    map.put("cvContent", m.getCv() != null ? m.getCv().getContent() : null);
                    return map;
                }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/shortlisted-candidates")
    public ResponseEntity<List<Map<String, Object>>> getShortlistedCandidates() {
        List<CVMatch> shortlisted = cvMatchRepository.findByStatus("SHORTLISTED");
        List<Map<String, Object>> result = shortlisted.stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    if (m.getCv() != null && m.getCv().getUser() != null) {
                        map.put("candidateName", m.getCv().getUser().getName());
                        map.put("candidateEmail", m.getCv().getUser().getEmail());
                    }
                    if (m.getJobOffer() != null) {
                        map.put("jobTitle", m.getJobOffer().getTitle());
                    }
                    map.put("matchId", m.getId());
                    map.put("matchScore", m.getMatchScore());
                    map.put("skillsMatch", m.getSkillsMatch());
                    map.put("experienceMatch", m.getExperienceMatch());
                    map.put("educationMatch", m.getEducationMatch());
                    map.put("languageMatch", m.getLanguageMatch());
                    map.put("certificationMatch", m.getCertificationMatch());
                    map.put("status", m.getStatus());
                    map.put("cvContent", m.getCv() != null ? m.getCv().getContent() : null);
                    return map;
                }).toList();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/matches/{matchId}/status")
    public ResponseEntity<Void> updateMatchStatus(@PathVariable Long matchId, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        cvMatchRepository.findById(matchId).ifPresent(match -> {
            match.setStatus(status);
            cvMatchRepository.save(match);
        });
        return ResponseEntity.noContent().build();
    }
}
