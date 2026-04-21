package tn.esprit.gestion_groups.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.gestion_groups.Entites.HistoriqueGroupe;
import tn.esprit.gestion_groups.Entites.InitiateurChangement;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriqueGroupeRepository extends JpaRepository<HistoriqueGroupe, Long> {

    // ============================================================
    // IDÉE 2 — Analyse d'impact des transferts d'étudiants
    // ============================================================

    /**
     * Pour chaque groupe SOURCE (qui perd des étudiants), calcule :
     *  - le nombre de transferts partis
     *  - l'impact moyen sur le score (positif = bonne raison, négatif = problème)
     *  - la répartition améliorations / dégradations / neutres
     *
     * JPQL : GROUP BY groupePrecedentId + raisonChangement + initiePar
     *        AVG(scoreApres - scoreAvant), SUM(CASE WHEN ...)
     *        filtre optionnel sur initiePar
     */
    @Query("SELECT h.groupePrecedentId, " +
           "       h.raisonChangement, " +
           "       h.initiePar, " +
           "       COUNT(h), " +
           "       COALESCE(AVG(h.scoreApres - h.scoreAvant), 0.0), " +
           "       SUM(CASE WHEN h.scoreApres > h.scoreAvant THEN 1 ELSE 0 END), " +
           "       SUM(CASE WHEN h.scoreApres < h.scoreAvant THEN 1 ELSE 0 END), " +
           "       SUM(CASE WHEN h.scoreApres = h.scoreAvant THEN 1 ELSE 0 END) " +
           "FROM HistoriqueGroupe h " +
           "WHERE (:initiePar IS NULL OR h.initiePar = :initiePar) " +
           "  AND h.dateChangement BETWEEN :dateDebut AND :dateFin " +
           "  AND h.scoreAvant IS NOT NULL " +
           "  AND h.scoreApres IS NOT NULL " +
           "GROUP BY h.groupePrecedentId, h.raisonChangement, h.initiePar " +
           "ORDER BY COUNT(h) DESC")
    List<Object[]> analyserImpactTransfertsParRaison(
            @Param("initiePar") InitiateurChangement initiePar,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    /**
     * Classe les groupes DESTINATION par leur capacité à améliorer les scores.
     * Inclut uniquement les groupes où l'amélioration moyenne est positive.
     *
     * JPQL : GROUP BY groupe destination, HAVING AVG > 0
     *        MAX et MIN pour mesurer la dispersion
     */
    @Query("SELECT h.groupe.idGroupe, " +
           "       h.groupe.nom, " +
           "       COUNT(h), " +
           "       AVG(h.scoreApres - h.scoreAvant), " +
           "       MAX(h.scoreApres - h.scoreAvant), " +
           "       MIN(h.scoreApres - h.scoreAvant) " +
           "FROM HistoriqueGroupe h " +
           "WHERE h.dateChangement BETWEEN :dateDebut AND :dateFin " +
           "  AND h.scoreAvant IS NOT NULL " +
           "  AND h.scoreApres IS NOT NULL " +
           "GROUP BY h.groupe.idGroupe, h.groupe.nom " +
           "HAVING AVG(h.scoreApres - h.scoreAvant) > 0 " +
           "ORDER BY AVG(h.scoreApres - h.scoreAvant) DESC")
    List<Object[]> getGroupesDestinationMeilleureAmelioration(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT COUNT(h) FROM HistoriqueGroupe h " +
           "WHERE h.groupe.idGroupe = :groupeId " +
           "  AND h.dateChangement >= :dateReference")
    long countChangementsRecentsByGroupe(
            @Param("groupeId") Long groupeId,
            @Param("dateReference") LocalDateTime dateReference);

    /**
     * Récupère l'historique complet d'un étudiant par ordre chronologique.
     */
    List<HistoriqueGroupe> findByEtudiantIdOrderByDateChangementAsc(Long etudiantId);
}
