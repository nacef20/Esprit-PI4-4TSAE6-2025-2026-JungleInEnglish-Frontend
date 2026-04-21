package tn.esprit.gestion_groups.Repositories;

// ============================================================
// AJOUTS à ton GroupeRepository existant
// (colle ces méthodes dans ton interface GroupeRepository)
// ============================================================

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.gestion_groups.Entites.Groupe;
import tn.esprit.gestion_groups.Entites.StatutGroupe;
import tn.esprit.gestion_groups.Entites.StatutObjectif;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupeRepository extends JpaRepository<Groupe, Long> {

    // ── tes méthodes existantes ─────────────────────────────────
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"objectifs", "reglements"})
    @Query("SELECT g FROM Groupe g WHERE g.idGroupe = :id")
    Optional<Groupe> findByIdWithDetails(@Param("id") Long id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"etudiants"})
    @Query("SELECT g FROM Groupe g WHERE g.idGroupe = :id")
    Optional<Groupe> findByIdWithEtudiants(@Param("id") Long id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"formateurs"})
    @Query("SELECT g FROM Groupe g WHERE g.idGroupe = :id")
    Optional<Groupe> findByIdWithFormateurs(@Param("id") Long id);

    Optional<Groupe> findByCodeGroupe(String codeGroupe);
    List<Groupe> findByStatut(StatutGroupe statut);
    List<Groupe> findByType(tn.esprit.gestion_groups.Entites.TypeGroupe type);
    List<Groupe> findByNiveauId(Long niveauId);
    List<Groupe> findByNomContainingIgnoreCase(String nom);

    @Query("SELECT g FROM Groupe g WHERE g.statut = tn.esprit.gestion_groups.Entites.StatutGroupe.OUVERT " +
           "AND g.capaciteActuelle < g.capaciteMax")
    List<Groupe> findGroupesDisponibles();

    // ============================================================
    // IDÉE 3 — Score de santé composite
    // ============================================================

    /**
     * Récupère en une seule requête JPQL les données brutes nécessaires
     * au calcul du score de santé de chaque groupe actif.
     *
     * 5 sous-requêtes corrélées :
     *   (1) COUNT objectifs ATTEINT
     *   (2) COUNT total objectifs
     *   (3) COUNT règlements obligatoires actifs
     *   (4) COUNT total règlements obligatoires
     *   (5) COUNT changements récents dans HistoriqueGroupe (fenêtre glissante)
     *
     * Les groupes ANNULE et TERMINE sont exclus.
     */
    @Query("SELECT g, " +
           "       (SELECT COUNT(o) FROM ObjectifPedagogique o " +
           "        WHERE o.groupe = g AND o.statut = :statutAtteint), " +
           "       (SELECT COUNT(o) FROM ObjectifPedagogique o " +
           "        WHERE o.groupe = g), " +
           "       (SELECT COUNT(r) FROM ReglementGroupe r " +
           "        WHERE r.groupe = g AND r.estActif = true AND r.estObligatoire = true), " +
           "       (SELECT COUNT(r) FROM ReglementGroupe r " +
           "        WHERE r.groupe = g AND r.estObligatoire = true), " +
           "       (SELECT COUNT(h) FROM HistoriqueGroupe h " +
           "        WHERE h.groupe = g AND h.dateChangement >= :dateReference) " +
           "FROM Groupe g " +
           "WHERE g.statut NOT IN :statutsExclus")
    List<Object[]> getRawDataPourScoreSante(
            @Param("statutAtteint") StatutObjectif statutAtteint,
            @Param("dateReference") LocalDateTime dateReference,
            @Param("statutsExclus") List<StatutGroupe> statutsExclus);

    // ============================================================
    // IDÉE 5 — Recommandation fusion / scission
    // ============================================================

    /**
     * Identifie les groupes candidats à une fusion (sous-remplis)
     * ou à une scission (quasi-saturés ≥ 90% capacité max).
     *
     * Pour chaque groupe retenu :
     *   - nombre d'objectifs EN_COURS (charge pédagogique active)
     *   - somme des heures estimées des objectifs EN_COURS
     *
     * Critère fusion    : capaciteActuelle < capaciteMin
     * Critère scission  : capaciteActuelle × 10 >= capaciteMax × 9  (= ≥ 90%)
     *
     * JPQL : sous-requêtes corrélées + arithmétique + tri ASC sur capacité
     */
    @Query("SELECT g, " +
           "       (SELECT COUNT(o) FROM ObjectifPedagogique o " +
           "        WHERE o.groupe = g AND o.statut = :statutEnCours), " +
           "       (SELECT COALESCE(SUM(o.dureeEstimeeHeures), 0) FROM ObjectifPedagogique o " +
           "        WHERE o.groupe = g AND o.statut = :statutEnCours) " +
           "FROM Groupe g " +
           "WHERE g.statut NOT IN :statutsExclus " +
           "  AND (g.capaciteActuelle < g.capaciteMin " +
           "       OR g.capaciteActuelle * 10 >= g.capaciteMax * 9) " +
           "ORDER BY g.capaciteActuelle ASC")
    List<Object[]> getGroupesCandidatsFusionScission(
            @Param("statutEnCours") StatutObjectif statutEnCours,
            @Param("statutsExclus") List<StatutGroupe> statutsExclus);

    // ============================================================
    // IDÉE 3 — Détection d'anomalies
    // ============================================================

    /** Groupes actifs sans AUCUN formateur actif */
    @Query("SELECT g FROM Groupe g " +
           "WHERE g.statut NOT IN :exclus " +
           "  AND NOT EXISTS (" +
           "    SELECT gf FROM GroupeFormateur gf " +
           "    WHERE gf.groupe = g AND gf.estActif = true" +
           "  )")
    List<Groupe> findGroupesSansFormateur(@Param("exclus") List<StatutGroupe> exclus);

    /** Groupes où capaciteActuelle > capaciteMax (incohérence) */
    @Query("SELECT g FROM Groupe g " +
           "WHERE g.statut NOT IN :exclus " +
           "  AND g.capaciteMax IS NOT NULL " +
           "  AND g.capaciteActuelle > g.capaciteMax")
    List<Groupe> findGroupesCapaciteIncoherente(@Param("exclus") List<StatutGroupe> exclus);

    /** Groupes actifs sans aucun objectif pédagogique défini */
    @Query("SELECT g FROM Groupe g " +
           "WHERE g.statut NOT IN :exclus " +
           "  AND NOT EXISTS (" +
           "    SELECT o FROM ObjectifPedagogique o WHERE o.groupe = g" +
           "  )")
    List<Groupe> findGroupesSansObjectif(@Param("exclus") List<StatutGroupe> exclus);

    // ============================================================
    // IDÉE 6 — Équilibrage automatique par niveau
    // ============================================================

    /**
     * Récupère les groupes d'un même niveau avec leurs métriques
     * pour calculer le plan d'équilibrage.
     */
    @Query("SELECT g, " +
           "  (SELECT COUNT(gf) FROM GroupeFormateur gf " +
           "   WHERE gf.groupe = g AND gf.estActif = true), " +
           "  (SELECT COUNT(o) FROM ObjectifPedagogique o " +
           "   WHERE o.groupe = g AND o.statut = :enCours) " +
           "FROM Groupe g " +
           "WHERE g.niveauId = :niveauId " +
           "  AND g.statut NOT IN :exclus " +
           "ORDER BY g.capaciteActuelle ASC")
    List<Object[]> getGroupesParNiveauPourEquilibrage(
            @Param("niveauId") Long niveauId,
            @Param("enCours") StatutObjectif enCours,
            @Param("exclus") List<StatutGroupe> exclus);

    /** Retourne les niveauId distincts qui ont au moins 2 groupes actifs */
    @Query("SELECT g.niveauId FROM Groupe g " +
           "WHERE g.statut NOT IN :exclus " +
           "  AND g.niveauId IS NOT NULL " +
           "GROUP BY g.niveauId " +
           "HAVING COUNT(g) >= 2")
    List<Long> findNiveauxAvecPlusieursGroupes(@Param("exclus") List<StatutGroupe> exclus);

    // ============================================================
    // IDÉE 2 — Statistiques Globales (Dashboard)
    // ============================================================
    @Query("SELECT " +
           "  (SELECT COUNT(g) FROM Groupe g WHERE g.statut NOT IN :exclus), " +
           "  (SELECT COUNT(ge) FROM GroupeEtudiant ge WHERE ge.statut = tn.esprit.gestion_groups.Entites.StatutAffectation.ACTIF), " +
           "  (SELECT COUNT(gf) FROM GroupeFormateur gf WHERE gf.estActif = true), " +
           "  (SELECT COALESCE(AVG(CASE WHEN g2.capaciteMax > 0 " +
           "         THEN (g2.capaciteActuelle * 100.0 / g2.capaciteMax) ELSE 0 END), 0) " +
           "   FROM Groupe g2 WHERE g2.statut NOT IN :exclus), " +
           "  (SELECT COUNT(g3) FROM Groupe g3 " +
           "   WHERE g3.statut NOT IN :exclus AND g3.capaciteActuelle < g3.capaciteMin), " +
           "  (SELECT COUNT(g4) FROM Groupe g4 " +
           "   WHERE g4.statut NOT IN :exclus AND g4.capaciteMax IS NOT NULL AND g4.capaciteActuelle * 10 >= g4.capaciteMax * 9), " +
           "  (SELECT COUNT(h) FROM HistoriqueGroupe h " +
           "   WHERE h.dateChangement >= :debutMois), " +
           "  (SELECT COUNT(o) FROM ObjectifPedagogique o WHERE o.statut = tn.esprit.gestion_groups.Entites.StatutObjectif.EN_COURS) " +
           "FROM Groupe g0 WHERE g0.idGroupe = (SELECT MIN(g5.idGroupe) FROM Groupe g5)")
       List<Object[]> getStatistiquesGlobales(
            @Param("exclus") List<StatutGroupe> exclus,
            @Param("debutMois") LocalDateTime debutMois);
}
