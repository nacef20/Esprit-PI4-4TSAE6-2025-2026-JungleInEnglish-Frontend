package tn.esprit.gestion_groups.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.gestion_groups.Entites.GroupeFormateur;
import tn.esprit.gestion_groups.Entites.RoleFormateur;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupeFormateurRepository extends JpaRepository<GroupeFormateur, Long> {

    // ── Requêtes de base ────────────────────────────────────────────

    // Tous les formateurs actifs d'un groupe
    @Query("SELECT gf FROM GroupeFormateur gf " +
           "WHERE gf.groupe.idGroupe = :groupeId " +
           "AND gf.estActif = true")
    List<GroupeFormateur> findFormateursActifsByGroupe(@Param("groupeId") Long groupeId);

    // Formateur PRINCIPAL actif d'un groupe
    @Query("SELECT gf FROM GroupeFormateur gf " +
           "WHERE gf.groupe.idGroupe = :groupeId " +
           "AND gf.role = tn.esprit.gestion_groups.Entites.RoleFormateur.PRINCIPAL " +
           "AND gf.estActif = true")
    Optional<GroupeFormateur> findFormateurPrincipalByGroupe(@Param("groupeId") Long groupeId);

    // Tous les groupes actifs d'un formateur
    @Query("SELECT gf FROM GroupeFormateur gf " +
           "WHERE gf.formateurId = :formateurId " +
           "AND gf.estActif = true " +
           "ORDER BY gf.dateDebut DESC")
    List<GroupeFormateur> findGroupesActifsByFormateur(@Param("formateurId") Long formateurId);

    // ── Requêtes JPQL avancées ──────────────────────────────────────

    /**
     * Charge horaire totale par formateur (somme heuresParSemaine sur tous
     * ses groupes actifs). Utile pour détecter la surcharge.
     *
     * JPQL : GROUP BY formateurId, SUM(heuresParSemaine)
     */
    @Query("SELECT gf.formateurId, SUM(gf.heuresParSemaine) " +
           "FROM GroupeFormateur gf " +
           "WHERE gf.estActif = true " +
           "AND gf.heuresParSemaine IS NOT NULL " +
           "GROUP BY gf.formateurId " +
           "ORDER BY SUM(gf.heuresParSemaine) DESC")
    List<Object[]> getChargeHoraireParFormateur();

    /**
     * Groupes sans formateur PRINCIPAL actif.
     * NOT EXISTS pour trouver les groupes non encadrés.
     */
    @Query("SELECT g FROM Groupe g " +
           "WHERE g.statut NOT IN " +
           "    (tn.esprit.gestion_groups.Entites.StatutGroupe.ANNULE, " +
           "     tn.esprit.gestion_groups.Entites.StatutGroupe.TERMINE) " +
           "AND NOT EXISTS (" +
           "    SELECT gf FROM GroupeFormateur gf " +
           "    WHERE gf.groupe = g " +
           "    AND gf.role = tn.esprit.gestion_groups.Entites.RoleFormateur.PRINCIPAL " +
           "    AND gf.estActif = true" +
           ")")
    List<Object[]> findGroupesSansFormateurPrincipal();

    /**
     * Désactive toutes les affectations actives d'un formateur dans un groupe.
     * Utilisé lors d'un retrait ou remplacement.
     */
    @Modifying
    @Transactional
    @Query("UPDATE GroupeFormateur gf " +
           "SET gf.estActif = false, gf.dateFin = CURRENT_DATE " +
           "WHERE gf.formateurId = :formateurId " +
           "AND gf.groupe.idGroupe = :groupeId " +
           "AND gf.estActif = true")
    void desactiverFormateurDansGroupe(
            @Param("formateurId") Long formateurId,
            @Param("groupeId") Long groupeId);

    // Vérification doublon : formateur déjà PRINCIPAL actif dans un groupe
    @Query("SELECT COUNT(gf) > 0 FROM GroupeFormateur gf " +
           "WHERE gf.groupe.idGroupe = :groupeId " +
           "AND gf.role = tn.esprit.gestion_groups.Entites.RoleFormateur.PRINCIPAL " +
           "AND gf.estActif = true")
    boolean existsFormateurPrincipalActif(@Param("groupeId") Long groupeId);

    // Par rôle
    List<GroupeFormateur> findByRole(RoleFormateur role);
}
