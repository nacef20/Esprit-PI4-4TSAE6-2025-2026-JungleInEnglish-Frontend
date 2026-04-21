package tn.esprit.gestion_groups.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.gestion_groups.Entites.CompetenceCible;
import tn.esprit.gestion_groups.Entites.ObjectifPedagogique;
import tn.esprit.gestion_groups.Entites.StatutObjectif;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ObjectifPedagogiqueRepository extends JpaRepository<ObjectifPedagogique, Long> {

    Optional<ObjectifPedagogique> findByCodeObjectif(String codeObjectif);

    List<ObjectifPedagogique> findByGroupeIdGroupe(Long groupeId);

    List<ObjectifPedagogique> findByStatut(StatutObjectif statut);

    List<ObjectifPedagogique> findByCompetenceCible(CompetenceCible competenceCible);

    List<ObjectifPedagogique> findByGroupeIdGroupeAndStatut(Long groupeId, StatutObjectif statut);

    List<ObjectifPedagogique> findByPriorite(Integer priorite);

    List<ObjectifPedagogique> findByGroupeIdGroupeOrderByPrioriteAsc(Long groupeId);

    // ============================================================
    // IDÉE 3 — Objectifs en retard (anomalie)
    // ============================================================

    /**
     * Objectifs dont dateFinPrevue est dépassée mais statut encore EN_COURS.
     * Retourne l'objectif complet pour analyse.
     */
    @Query("SELECT o FROM ObjectifPedagogique o " +
           "JOIN FETCH o.groupe g " +
           "WHERE o.statut = :enCours " +
           "  AND o.dateFinPrevue IS NOT NULL " +
           "  AND o.dateFinPrevue < :aujourdhui " +
           "ORDER BY o.dateFinPrevue ASC")
    List<ObjectifPedagogique> findObjectifsEnRetard(
            @Param("enCours") StatutObjectif enCours,
            @Param("aujourdhui") LocalDate aujourdhui);
}