package tn.esprit.recrutement.controller;

import tn.esprit.recrutement.model.CV;
import tn.esprit.recrutement.model.CVMatch;
import tn.esprit.recrutement.model.Roles;
import tn.esprit.recrutement.repository.CVMatchRepository;
import tn.esprit.recrutement.service.CVService;
import tn.esprit.recrutement.model.User;
import tn.esprit.recrutement.repository.CVRepository;
import tn.esprit.recrutement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import tn.esprit.recrutement.service.KeycloakUserClient;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/cvs")
public class CVController {

    @Autowired
    private CVService cvService;
    @Autowired
    private CVRepository cvRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CVMatchRepository cvMatchRepository;
    @Autowired
    private KeycloakUserClient keycloakUserClient;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file,
            @RequestParam("candidateId") String keycloakUserId,
            @RequestParam("jobOfferId") Long jobOfferId,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "email", required = false) String email) {
        try {
            User candidate = userRepository.findByKeycloakUserId(keycloakUserId).orElse(null);
            
            // Auto-sync candidate if not found in local database
            if (candidate == null) {
                try {
                    // Try to use provided info first (robust fallback)
                    if (email != null && firstName != null) {
                        candidate = new User();
                        candidate.setKeycloakUserId(keycloakUserId);
                        candidate.setEmail(email);
                        candidate.setName(firstName + " " + (lastName != null ? lastName : ""));
                        candidate.setRole(Roles.CANDIDATE);
                        candidate = userRepository.save(candidate);
                    } else {
                        // Fallback to service-to-service if client didn't provide info
                        Map<String, Object> keycloakUser = keycloakUserClient.getUserById(keycloakUserId);
                        if (keycloakUser != null) {
                            candidate = new User();
                            candidate.setKeycloakUserId(keycloakUserId);
                            candidate.setEmail((String) keycloakUser.get("email"));
                            candidate.setName((String) keycloakUser.get("firstName") + " " + (String) keycloakUser.get("lastName"));
                            candidate.setRole(Roles.CANDIDATE);
                            candidate = userRepository.save(candidate);
                        }
                    }
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Candidate profile synchronization failed. Please ensure your account details are complete.");
                }
            }

            if (candidate == null) {
                return ResponseEntity.badRequest().body("Candidate not found mapped to this Keycloak ID and synchronization failed.");
            }

            /*
             * // Check if already uploaded for this job offer using optimized query
             * List<Long> existingJobIds =
             * cvMatchRepository.findJobOfferIdsByCandidateId(candidateId);
             * if (existingJobIds.contains(jobOfferId)) {
             * return ResponseEntity.status(409).
             * body("You have already uploaded your CV for this job offer.");
             * }
             */

            // Use the new method to get both the CV and extracted attributes for a specific
            // job offer
            var result = cvService.processCVWithAttributesForJob(file.getBytes(), candidate, jobOfferId);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("File upload error.");
        }
    }

    @GetMapping("/matches")
    public ResponseEntity<List<CVMatch>> getCVMatches(@RequestParam Long cvId) {
        CV cv = cvRepository.findById(cvId).orElse(null);
        if (cv == null) {
            return ResponseEntity.notFound().build();
        }
        List<CVMatch> matches = cvService.matchCVWithAllJobs(cv);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/matches/by-candidate")
    public ResponseEntity<List<CVMatch>> getLatestMatchesByCandidate(@RequestParam String candidateId) {
        User candidate = userRepository.findByKeycloakUserId(candidateId).orElse(null);
        if (candidate == null)
            return ResponseEntity.ok(new ArrayList<>());
        List<CVMatch> matches = cvMatchRepository.findByCandidateIdWithFetch(candidate.getId());

        // Auto-sync stale scores (fix data from 0-1 range to 0-100 range)
        boolean needsUpdate = false;
        for (CVMatch match : matches) {
            double currentScore = match.getMatchScore();
            double freshScore = cvService.recalculateScore(match);
            if (Math.abs(currentScore - freshScore) > 0.01) {
                match.setMatchScore(freshScore);
                needsUpdate = true;
            }
        }
        if (needsUpdate) {
            cvMatchRepository.saveAll(matches);
        }

        // Filter to get only the latest match per job
        Map<Long, CVMatch> latestMatchByJob = new HashMap<>();
        for (CVMatch match : matches) {
            Long jobId = match.getJobOffer().getId();
            if (!latestMatchByJob.containsKey(jobId) ||
                    match.getMatchDate().after(latestMatchByJob.get(jobId).getMatchDate())) {
                latestMatchByJob.put(jobId, match);
            }
        }
        return ResponseEntity.ok(new ArrayList<>(latestMatchByJob.values()));
    }

    @GetMapping("/uploaded-job-ids")
    public ResponseEntity<List<Long>> getUploadedJobIdsByCandidate(@RequestParam String candidateId) {
        User candidate = userRepository.findByKeycloakUserId(candidateId).orElse(null);
        if (candidate == null)
            return ResponseEntity.ok(new ArrayList<>());
        List<Long> jobOfferIds = cvMatchRepository.findJobOfferIdsByCandidateId(candidate.getId());
        return ResponseEntity.ok(jobOfferIds);
    }

    @GetMapping("/candidates/{id}")
    public ResponseEntity<List<CV>> getCandidateCVs(@PathVariable Long id) {
        // TODO: Get CVs for candidate
        return ResponseEntity.ok(List.of()); // Placeholder
    }
}
