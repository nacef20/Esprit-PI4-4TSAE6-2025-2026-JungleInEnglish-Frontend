package tn.esprit.recrutement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.recrutement.model.CV;

import java.util.List;

public interface CVRepository extends JpaRepository<CV, Long> {
    List<CV> findByUserId(Integer userId);
} 