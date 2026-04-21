package tn.esprit.gestion_groups.Services;

import tn.esprit.gestion_groups.Entites.*;

import java.util.List;

public interface IAffectationGroupeService {

    // ================================================================
    // ÉTUDIANTS
    // ================================================================

    /**
     * Affecte un étudiant à un groupe.
     * Vérifie :
     *  - que l'étudiant n'est pas déjà ACTIF dans un autre groupe
     *  - que le groupe n'est pas COMPLET / ANNULE / TERMINE
     *  - que capaciteActuelle < capaciteMax
     * Met à jour capaciteActuelle du groupe automatiquement.
     */
    GroupeEtudiant affecterEtudiantAGroupe(
            Long etudiantId,
            Long groupeId,
            Integer scoreEntree,
            String commentaire,
            Long affectePar);

    /**
     * Change un étudiant de groupe.
     * 1. Marque l'ancienne affectation comme SORTI
     * 2. Crée une nouvelle affectation ACTIF dans le nouveau groupe
     * 3. Crée automatiquement un HistoriqueGroupe
     * 4. Met à jour les capaciteActuelle des deux groupes
     */
    GroupeEtudiant changerGroupeEtudiant(
            Long etudiantId,
            Long nouveauGroupeId,
            Integer scoreAvant,
            RaisonChangement raison,
            InitiateurChangement initiePar,
            Long validateurId,
            String commentaire);

    /**
     * Désaffecte un étudiant de son groupe actuel.
     * Marque l'affectation SORTI + décrémente capaciteActuelle.
     */
    GroupeEtudiant desaffecterEtudiant(Long etudiantId, String commentaire);

    /**
     * Suspend temporairement un étudiant (SUSPENDU).
     * Ne décrémente PAS la capacité (la place est réservée).
     */
    GroupeEtudiant suspendreEtudiant(Long etudiantId, String commentaire);

    /**
     * Réactive un étudiant suspendu (SUSPENDU → ACTIF).
     */
    GroupeEtudiant reactiverEtudiant(Long etudiantId);

    // ── Consultation étudiants ──────────────────────────────────────

    /** Tous les étudiants ACTIFS dans un groupe */
    List<GroupeEtudiant> getEtudiantsActifsByGroupe(Long groupeId);

    /** Groupe actuel (ACTIF) d'un étudiant — null si sans groupe */
    GroupeEtudiant getGroupeActuelEtudiant(Long etudiantId);

    /** Historique complet des groupes d'un étudiant */
    List<GroupeEtudiant> getHistoriqueEtudiant(Long etudiantId);

    /** Étudiants qui n'ont aucun groupe actif */
    List<Long> getEtudiantsSansGroupe();

    // ================================================================
    // FORMATEURS
    // ================================================================

    /**
     * Affecte un formateur à un groupe avec un rôle.
     * Vérifie qu'il n'y a pas déjà un PRINCIPAL actif si role = PRINCIPAL.
     */
    GroupeFormateur affecterFormateurAGroupe(
            Long formateurId,
            Long groupeId,
            RoleFormateur role,
            String matiere,
            Integer heuresParSemaine,
            String commentaire);

    /**
     * Retire un formateur d'un groupe (estActif = false).
     */
    GroupeFormateur retirerFormateurDuGroupe(Long formateurId, Long groupeId);

    /**
     * Change le rôle d'un formateur dans un groupe.
     */
    GroupeFormateur changerRoleFormateur(Long formateurId, Long groupeId, RoleFormateur nouveauRole);

    // ── Consultation formateurs ─────────────────────────────────────

    /** Tous les formateurs actifs d'un groupe */
    List<GroupeFormateur> getFormateursActifsByGroupe(Long groupeId);

    /** Formateur PRINCIPAL d'un groupe */
    GroupeFormateur getFormateurPrincipal(Long groupeId);

    /** Tous les groupes actifs d'un formateur */
    List<GroupeFormateur> getGroupesActifsByFormateur(Long formateurId);

    /** Groupes sans formateur PRINCIPAL */
    List<Object[]> getGroupesSansFormateurPrincipal();

    /** Charge horaire de tous les formateurs actifs */
    List<Object[]> getChargeHoraireParFormateur();
}
