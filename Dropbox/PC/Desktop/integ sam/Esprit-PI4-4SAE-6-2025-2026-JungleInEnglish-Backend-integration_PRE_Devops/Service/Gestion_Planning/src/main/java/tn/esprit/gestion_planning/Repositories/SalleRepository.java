package tn.esprit.gestion_planning.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.gestion_planning.Entites.Salle;

@Repository
public interface SalleRepository extends JpaRepository<Salle, Long> {
}
