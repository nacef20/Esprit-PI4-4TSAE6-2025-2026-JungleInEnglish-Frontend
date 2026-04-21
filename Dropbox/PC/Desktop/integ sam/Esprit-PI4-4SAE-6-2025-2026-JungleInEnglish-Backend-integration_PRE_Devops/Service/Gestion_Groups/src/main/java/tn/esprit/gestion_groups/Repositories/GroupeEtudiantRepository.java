package tn.esprit.gestion_groups.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.gestion_groups.Entites.GroupeEtudiant;
import tn.esprit.gestion_groups.Entites.StatutAffectation;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupeEtudiantRepository extends JpaRepository<GroupeEtudiant, Long> {

    // ── Requêtes de base ────────────────────────────────────────────

    // Tous les étudiants ACTIFS d'un groupe
    @Query("SELECT ge FROM GroupeEtudiant ge " +
           "WHERE ge.groupe.idGroupe = :groupeId " +
           "AND ge.statut = tn.esprit.gestion_groups.Entites.StatutAffectation.ACTIF")
    List<GroupeEtudiant> findEtudiantsActifsByGroupe(@Param("groupeId") Long groupeId);

    // Groupe ACTIF actuel d'un étudiant
    @Query("SELECT ge FROM GroupeEtudiant ge " +
           "WHERE ge.etudiantId = :etudiantId " +
           "AND ge.statut = tn.esprit.gestion_groups.Entites.StatutAffectation.ACTIF")
    Optional<GroupeEtudiant> findGroupeActuelByEtudiant(@Param("etudiantId") Long etudiantId);

    // Historique complet des affectations d'un étudiant (tous statuts)
    @Query("SELECT ge FROM GroupeEtudiant ge " +
           "WHERE ge.etudiantId = :etudiantId " +
           "ORDER BY ge.dateAffectation DESC")
    List<GroupeEtudiant> findHistoriqueAffectationsByEtudiant(@Param("etudiantId") Long etudiantId);

    // Vérifie si un étudiant est déjà ACTIF dans un groupe
    @Query("SELECT COUNT(ge) > 0 FROM GroupeEtudiant ge " +
           "WHERE ge.etudiantId = :etudiantId " +
           "AND ge.statut = tn.esprit.gestion_groups.Entites.StatutAffectation.ACTIF")
    boolean isEtudiantDejaAffecte(@Param("etudiantId") Long etudiantId);

    // ── Requêtes JPQL avancées ──────────────────────────────────────

    /**
     * Compte le nombre d'étudiants ACTIFS par groupe.
     * Utilisé pour synchroniser avec capaciteActuelle.
     */
    @Query("SELECT ge.groupe.idGroupe, COUNT(ge) " +
           "FROM GroupeEtudiant ge " +
           "WHERE ge.statut = tn.esprit.gestion_groups.Entites.StatutAffectation.ACTIF " +
           "GROUP BY ge.groupe.idGroupe")
    List<Object[]> countEtudiantsActifsByGroupe();

    /**
     * Trouve les groupes où un étudiant a déjà été (pour éviter les doublons
     * ou pour analyse de parcours).
     */
    @Query("SELECT DISTINCT ge.groupe FROM GroupeEtudiant ge " +
           "WHERE ge.etudiantId = :etudiantId")
    List<Object[]> findGroupesParcoursEtudiant(@Param("etudiantId") Long etudiantId);

    /**
     * Désactive toutes les affectations ACTIVES d'un étudiant.
     * Utilisé avant de l'affecter à un nouveau groupe.
     */
    @Modifying
    @Transactional
    @Query("UPDATE GroupeEtudiant ge " +
           "SET ge.statut = tn.esprit.gestion_groups.Entites.StatutAffectation.SORTI, " +
           "    ge.dateSortie = CURRENT_DATE " +
           "WHERE ge.etudiantId = :etudiantId " +
           "AND ge.statut = tn.esprit.gestion_groups.Entites.StatutAffectation.ACTIF")
    void desactiverAffectationsActives(@Param("etudiantId") Long etudiantId);

    /**
     * Liste des étudiants sans groupe actif.
     * Sous-requête NOT EXISTS pour trouver les "orphelins".
     */
    @Query("SELECT DISTINCT ge.etudiantId FROM GroupeEtudiant ge " +
           "WHERE ge.etudiantId NOT IN (" +
           "    SELECT ge2.etudiantId FROM GroupeEtudiant ge2 " +
           "    WHERE ge2.statut = tn.esprit.gestion_groups.Entites.StatutAffectation.ACTIF" +
           ")")
    List<Long> findEtudiantsSansGroupeActif();

    // Toutes les affectations d'un groupe (tous statuts confondus)
    List<GroupeEtudiant> findByGroupeIdGroupe(Long groupeId);

    // Par statut
    List<GroupeEtudiant> findByStatut(StatutAffectation statut);

    // ============================================================
    // IDÉE 5 — Audit Trail : parcours étudiant
    // ============================================================

    /**
     * Parcours complet d'un étudiant : toutes ses affectations
     * avec JOIN FETCH sur le groupe pour éviter N+1.
     */
    @Query("SELECT ge FROM GroupeEtudiant ge " +
           "JOIN FETCH ge.groupe g " +
           "WHERE ge.etudiantId = :etudiantId " +
           "ORDER BY ge.dateAffectation ASC")
    List<GroupeEtudiant> findParcoursCompletEtudiant(@Param("etudiantId") Long etudiantId);

    /**
     * Statistiques de rétention par groupe :
     *  - nombre total d'affectations
     *  - nombre de SORTI
     *  - nombre de SUSPENDU
     *  - nombre d'ACTIF
     *
     * JPQL : GROUP BY groupe + SUM(CASE WHEN ...)
     */
    @Query("SELECT ge.groupe.idGroupe, ge.groupe.nom, " +
           "  COUNT(ge), " +
           "  SUM(CASE WHEN ge.statut = tn.esprit.gestion_groups.Entites.StatutAffectation.SORTI THEN 1 ELSE 0 END), " +
           "  SUM(CASE WHEN ge.statut = tn.esprit.gestion_groups.Entites.StatutAffectation.SUSPENDU THEN 1 ELSE 0 END), " +
           "  SUM(CASE WHEN ge.statut = tn.esprit.gestion_groups.Entites.StatutAffectation.ACTIF THEN 1 ELSE 0 END) " +
           "FROM GroupeEtudiant ge " +
           "GROUP BY ge.groupe.idGroupe, ge.groupe.nom " +
           "HAVING COUNT(ge) >= :seuilMinimum " +
           "ORDER BY COUNT(ge) DESC")
    List<Object[]> getStatistiquesRetentionParGroupe(
            @Param("seuilMinimum") long seuilMinimum);

    /**
     * Durée moyenne de séjour (en jours) par groupe.
     * Ne prend en compte que les affectations terminées (dateSortie NOT NULL).
     */
    @Query(value = "SELECT groupe_id, AVG(DATEDIFF(date_sortie, date_affectation)) " +
           "FROM groupe_etudiants " +
           "WHERE date_sortie IS NOT NULL " +
           "GROUP BY groupe_id", nativeQuery = true)
    List<Object[]> getDureeMoyenneSejourParGroupe();
}
