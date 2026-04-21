package tn.esprit.gestion_groups.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.gestion_groups.Entites.CategorieReglement;
import tn.esprit.gestion_groups.Entites.ReglementGroupe;


import java.util.List;
import java.util.Optional;

@Repository
public interface ReglementGroupeRepository extends JpaRepository<ReglementGroupe, Long> {

    Optional<ReglementGroupe> findByCodeReglement(String codeReglement);

    List<ReglementGroupe> findByGroupeIdGroupe(Long groupeId);

    List<ReglementGroupe> findByCategorie(CategorieReglement categorie);

    List<ReglementGroupe> findByEstActif(Boolean estActif);

    List<ReglementGroupe> findByGroupeIdGroupeAndEstActif(Long groupeId, Boolean estActif);

    List<ReglementGroupe> findByEstObligatoire(Boolean estObligatoire);
}