package tn.esprit.gestion_planning.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.gestion_planning.Entites.CreneauHoraire;

@Repository
public interface CreneauRepository extends JpaRepository<CreneauHoraire,Long> {
}
