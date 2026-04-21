package tn.esprit.recrutement.repository;

import tn.esprit.recrutement.model.Statistiques;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface StatistiquesRepository extends JpaRepository<Statistiques, Long> {
} 