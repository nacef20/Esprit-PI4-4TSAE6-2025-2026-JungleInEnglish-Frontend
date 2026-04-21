package tn.esprit.gestion_groups.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_groups.Entites.*;
import tn.esprit.gestion_groups.Services.IAffectationGroupeService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/affectations")
public class AffectationGroupeController {

    private final IAffectationGroupeService affectationService;

    // ================================================================
    // ÉTUDIANTS — Affectation
    // ================================================================

    /**
     * POST /affectations/etudiants/affecter/{etudiantId}/{groupeId}
     * Affecte un étudiant à un groupe.
     * Params optionnels : scoreEntree, commentaire, affectePar
     */
    @PostMapping("/etudiants/affecter/{etudiantId}/{groupeId}")
    public GroupeEtudiant affecterEtudiant(
            @PathVariable Long etudiantId,
            @PathVariable Long groupeId,
            @RequestParam(required = false) Integer scoreEntree,
            @RequestParam(required = false) String commentaire,
            @RequestParam(required = false) Long affectePar) {

        return affectationService.affecterEtudiantAGroupe(
                etudiantId, groupeId, scoreEntree, commentaire, affectePar);
    }

    /**
     * PUT /affectations/etudiants/changer-groupe/{etudiantId}/{nouveauGroupeId}
     * Transfère un étudiant vers un autre groupe.
     * Crée automatiquement l'entrée dans HistoriqueGroupe.
     */
    @PutMapping("/etudiants/changer-groupe/{etudiantId}/{nouveauGroupeId}")
    public GroupeEtudiant changerGroupe(
            @PathVariable Long etudiantId,
            @PathVariable Long nouveauGroupeId,
            @RequestParam(required = false) Integer scoreAvant,
            @RequestParam RaisonChangement raison,
            @RequestParam InitiateurChangement initiePar,
            @RequestParam(required = false) Long validateurId,
            @RequestParam(required = false) String commentaire) {

        return affectationService.changerGroupeEtudiant(
                etudiantId, nouveauGroupeId,
                scoreAvant, raison, initiePar, validateurId, commentaire);
    }

    /**
     * PUT /affectations/etudiants/desaffecter/{etudiantId}
     * Retire un étudiant de son groupe actuel (statut SORTI).
     */
    @PutMapping("/etudiants/desaffecter/{etudiantId}")
    public GroupeEtudiant desaffecterEtudiant(
            @PathVariable Long etudiantId,
            @RequestParam(required = false) String commentaire) {

        return affectationService.desaffecterEtudiant(etudiantId, commentaire);
    }

    /**
     * PUT /affectations/etudiants/suspendre/{etudiantId}
     * Suspend un étudiant (garde sa place dans le groupe).
     */
    @PutMapping("/etudiants/suspendre/{etudiantId}")
    public GroupeEtudiant suspendreEtudiant(
            @PathVariable Long etudiantId,
            @RequestParam(required = false) String commentaire) {

        return affectationService.suspendreEtudiant(etudiantId, commentaire);
    }

    /**
     * PUT /affectations/etudiants/reactiver/{etudiantId}
     * Réactive un étudiant suspendu.
     */
    @PutMapping("/etudiants/reactiver/{etudiantId}")
    public GroupeEtudiant reactiverEtudiant(@PathVariable Long etudiantId) {
        return affectationService.reactiverEtudiant(etudiantId);
    }

    // ================================================================
    // ÉTUDIANTS — Consultation
    // ================================================================

    /**
     * GET /affectations/etudiants/groupe/{groupeId}
     * Liste tous les étudiants ACTIFS dans un groupe.
     */
    @GetMapping("/etudiants/groupe/{groupeId}")
    public List<GroupeEtudiant> getEtudiantsActifsByGroupe(@PathVariable Long groupeId) {
        return affectationService.getEtudiantsActifsByGroupe(groupeId);
    }

    /**
     * GET /affectations/etudiants/{etudiantId}/groupe-actuel
     * Retourne le groupe actuel d'un étudiant (null si sans groupe).
     */
    @GetMapping("/etudiants/{etudiantId}/groupe-actuel")
    public GroupeEtudiant getGroupeActuelEtudiant(@PathVariable Long etudiantId) {
        return affectationService.getGroupeActuelEtudiant(etudiantId);
    }

    /**
     * GET /affectations/etudiants/{etudiantId}/historique
     * Historique complet des groupes d'un étudiant (tous statuts).
     */
    @GetMapping("/etudiants/{etudiantId}/historique")
    public List<GroupeEtudiant> getHistoriqueEtudiant(@PathVariable Long etudiantId) {
        return affectationService.getHistoriqueEtudiant(etudiantId);
    }

    /**
     * GET /affectations/etudiants/sans-groupe
     * IDs des étudiants qui n'ont aucune affectation active.
     */
    @GetMapping("/etudiants/sans-groupe")
    public List<Long> getEtudiantsSansGroupe() {
        return affectationService.getEtudiantsSansGroupe();
    }

    // ================================================================
    // FORMATEURS — Affectation
    // ================================================================

    /**
     * POST /affectations/formateurs/affecter/{formateurId}/{groupeId}
     * Affecte un formateur à un groupe avec un rôle.
     */
    @PostMapping("/formateurs/affecter/{formateurId}/{groupeId}")
    public GroupeFormateur affecterFormateur(
            @PathVariable Long formateurId,
            @PathVariable Long groupeId,
            @RequestParam RoleFormateur role,
            @RequestParam(required = false) String matiere,
            @RequestParam(required = false) Integer heuresParSemaine,
            @RequestParam(required = false) String commentaire) {

        return affectationService.affecterFormateurAGroupe(
                formateurId, groupeId, role, matiere, heuresParSemaine, commentaire);
    }

    /**
     * PUT /affectations/formateurs/retirer/{formateurId}/{groupeId}
     * Retire un formateur d'un groupe.
     */
    @PutMapping("/formateurs/retirer/{formateurId}/{groupeId}")
    public GroupeFormateur retirerFormateur(
            @PathVariable Long formateurId,
            @PathVariable Long groupeId) {

        return affectationService.retirerFormateurDuGroupe(formateurId, groupeId);
    }

    /**
     * PUT /affectations/formateurs/changer-role/{formateurId}/{groupeId}/{nouveauRole}
     * Change le rôle d'un formateur dans un groupe.
     */
    @PutMapping("/formateurs/changer-role/{formateurId}/{groupeId}/{nouveauRole}")
    public GroupeFormateur changerRoleFormateur(
            @PathVariable Long formateurId,
            @PathVariable Long groupeId,
            @PathVariable RoleFormateur nouveauRole) {

        return affectationService.changerRoleFormateur(formateurId, groupeId, nouveauRole);
    }

    // ================================================================
    // FORMATEURS — Consultation
    // ================================================================

    /**
     * GET /affectations/formateurs/groupe/{groupeId}
     * Tous les formateurs actifs d'un groupe.
     */
    @GetMapping("/formateurs/groupe/{groupeId}")
    public List<GroupeFormateur> getFormateursActifsByGroupe(@PathVariable Long groupeId) {
        return affectationService.getFormateursActifsByGroupe(groupeId);
    }

    /**
     * GET /affectations/formateurs/groupe/{groupeId}/principal
     * Formateur PRINCIPAL d'un groupe.
     */
    @GetMapping("/formateurs/groupe/{groupeId}/principal")
    public GroupeFormateur getFormateurPrincipal(@PathVariable Long groupeId) {
        return affectationService.getFormateurPrincipal(groupeId);
    }

    /**
     * GET /affectations/formateurs/{formateurId}/groupes
     * Tous les groupes actifs d'un formateur.
     */
    @GetMapping("/formateurs/{formateurId}/groupes")
    public List<GroupeFormateur> getGroupesActifsByFormateur(@PathVariable Long formateurId) {
        return affectationService.getGroupesActifsByFormateur(formateurId);
    }

    /**
     * GET /affectations/formateurs/groupes-sans-principal
     * Groupes qui n'ont pas de formateur PRINCIPAL affecté.
     */
    @GetMapping("/formateurs/groupes-sans-principal")
    public List<Object[]> getGroupesSansFormateurPrincipal() {
        return affectationService.getGroupesSansFormateurPrincipal();
    }

    /**
     * GET /affectations/formateurs/charge-horaire
     * Charge horaire totale par formateur (trié par charge décroissante).
     */
    @GetMapping("/formateurs/charge-horaire")
    public List<Object[]> getChargeHoraireParFormateur() {
        return affectationService.getChargeHoraireParFormateur();
    }
}
