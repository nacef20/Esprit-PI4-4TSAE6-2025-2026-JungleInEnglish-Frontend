package tn.esprit.recrutement.repository;

import tn.esprit.recrutement.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
} 