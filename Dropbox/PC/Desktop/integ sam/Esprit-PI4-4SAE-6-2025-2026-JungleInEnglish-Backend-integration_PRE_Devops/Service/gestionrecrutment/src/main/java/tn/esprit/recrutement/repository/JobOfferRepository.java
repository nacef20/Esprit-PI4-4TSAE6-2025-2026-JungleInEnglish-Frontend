package tn.esprit.recrutement.repository;

import tn.esprit.recrutement.model.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
} 