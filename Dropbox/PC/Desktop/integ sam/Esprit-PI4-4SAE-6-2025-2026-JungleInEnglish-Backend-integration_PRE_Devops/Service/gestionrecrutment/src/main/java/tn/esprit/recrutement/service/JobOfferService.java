package tn.esprit.recrutement.service;

import tn.esprit.recrutement.model.JobOffer;
import tn.esprit.recrutement.model.User;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import tn.esprit.recrutement.repository.JobOfferRepository;
import tn.esprit.recrutement.repository.CVMatchRepository;

@Service
public class JobOfferService {
    @Autowired
    private JobOfferRepository jobOfferRepository;

    @Autowired
    private CVService cvService;

    @Autowired
    private CVMatchRepository cvMatchRepository;

    public JobOffer createJobOffer(JobOffer jobOffer, User employer) {
        jobOffer.setEmployer(employer);
        if (jobOffer.getPostingDate() == null) {
            jobOffer.setPostingDate(new Date());
        }
        return jobOfferRepository.save(jobOffer);
    }

    public List<JobOffer> getActiveJobOffers() {
        return jobOfferRepository.findAll().stream().filter(JobOffer::isActive).toList();
    }

    public List<JobOffer> getEmployerJobOffers(User employer) {
        return jobOfferRepository.findAll().stream().filter(j -> j.getEmployer().getId() == employer.getId()).toList();
    }

    public JobOffer updateJobOffer(JobOffer jobOffer) {
        return jobOfferRepository.findById(jobOffer.getId()).map(existing -> {
            existing.setTitle(jobOffer.getTitle());
            existing.setDescription(jobOffer.getDescription());
            existing.setLocation(jobOffer.getLocation());
            existing.setEmploymentType(jobOffer.getEmploymentType());
            existing.setExperienceLevel(jobOffer.getExperienceLevel());
            existing.setSalary(jobOffer.getSalary());
            existing.setCurrency(jobOffer.getCurrency());
            existing.setPostingDate(jobOffer.getPostingDate());
            existing.setClosingDate(jobOffer.getClosingDate());
            existing.setActive(jobOffer.isActive());

            // Explicitly sync requirement sets
            existing.setRequiredSkills(jobOffer.getRequiredSkills());
            existing.setRequiredQualifications(jobOffer.getRequiredQualifications());
            existing.setRequiredLanguages(jobOffer.getRequiredLanguages());
            existing.setRequiredCertifications(jobOffer.getRequiredCertifications());

            JobOffer saved = jobOfferRepository.save(existing);
            cvService.updateMatchesForJob(saved);
            return saved;
        }).orElse(null);
    }

    public void deactivateJobOffer(Long id) {
        jobOfferRepository.findById(id).ifPresent(offer -> {
            offer.setActive(false);
            jobOfferRepository.save(offer);
        });
    }

    public void deleteJobOffer(Long id) {
        cvMatchRepository.deleteByJobOfferId(id);
        jobOfferRepository.deleteById(id);
    }
}
