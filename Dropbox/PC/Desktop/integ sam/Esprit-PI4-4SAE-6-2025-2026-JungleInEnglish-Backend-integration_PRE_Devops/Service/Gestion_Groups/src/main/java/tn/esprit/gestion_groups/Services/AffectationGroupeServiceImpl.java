package tn.esprit.gestion_groups.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.gestion_groups.Entites.*;
import tn.esprit.gestion_groups.Repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class AffectationGroupeServiceImpl implements IAffectationGroupeService {

    private final GroupeEtudiantRepository groupeEtudiantRepository;
    private final GroupeFormateurRepository groupeFormateurRepository;
    private final GroupeRepository groupeRepository;
    private final HistoriqueGroupeRepository historiqueRepository;
    private final GroupeHealthScoreService groupeHealthScoreService;

    // ================================================================
    // ÉTUDIANTS
    // ================================================================

    @Override
    public GroupeEtudiant affecterEtudiantAGroupe(
            Long etudiantId, Long groupeId,
            Integer scoreEntree, String commentaire, Long affectePar) {

        // ── Garde-fous ──────────────────────────────────────────────
        if (groupeEtudiantRepository.isEtudiantDejaAffecte(etudiantId)) {
            throw new IllegalStateException(
                "L'étudiant " + etudiantId + " est déjà actif dans un groupe. " +
                "Utilisez changerGroupeEtudiant() pour le transférer."
            );
        }

        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable : " + groupeId));

        validerGroupeOuvert(groupe);
        validerCapaciteDisponible(groupe);

        // ── Création de l'affectation ───────────────────────────────
        GroupeEtudiant affectation = new GroupeEtudiant();
        affectation.setEtudiantId(etudiantId);
        affectation.setGroupe(groupe);
        affectation.setStatut(StatutAffectation.ACTIF);
        affectation.setDateAffectation(LocalDate.now());
        affectation.setScoreEntree(scoreEntree);
        affectation.setCommentaire(commentaire);
        affectation.setAffectePar(affectePar);

        GroupeEtudiant saved = groupeEtudiantRepository.save(affectation);

        // ── Mise à jour capacité ────────────────────────────────────
        groupe.setCapaciteActuelle(groupe.getCapaciteActuelle() + 1);
        if (groupe.getCapaciteActuelle().equals(groupe.getCapaciteMax())) {
            groupe.setStatut(StatutGroupe.COMPLET);
        }
        groupeRepository.save(groupe);

        return saved;
    }

    @Override
    public GroupeEtudiant changerGroupeEtudiant(
            Long etudiantId, Long nouveauGroupeId,
            Integer scoreAvant,
            RaisonChangement raison, InitiateurChangement initiePar,
            Long validateurId, String commentaire) {

        // ── 1. Récupérer l'affectation actuelle ─────────────────────
        GroupeEtudiant affectationActuelle = groupeEtudiantRepository
                .findGroupeActuelByEtudiant(etudiantId)
                .orElseThrow(() -> new IllegalStateException(
                        "L'étudiant " + etudiantId + " n'a pas de groupe actif. " +
                        "Utilisez affecterEtudiantAGroupe() pour une première affectation."
                ));

        Long ancienGroupeId = affectationActuelle.getGroupe().getIdGroupe();
        Groupe ancienGroupe = affectationActuelle.getGroupe();
        Integer scoreAvantGroupe = groupeHealthScoreService.calculerScoreTotal(ancienGroupe);

        Groupe nouveauGroupe = groupeRepository.findById(nouveauGroupeId)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable : " + nouveauGroupeId));

        validerGroupeOuvert(nouveauGroupe);
        validerCapaciteDisponible(nouveauGroupe);

        // ── 2. Clôturer l'ancienne affectation ──────────────────────
        affectationActuelle.setStatut(StatutAffectation.SORTI);
        affectationActuelle.setDateSortie(LocalDate.now());
        affectationActuelle.setCommentaire(commentaire);
        groupeEtudiantRepository.save(affectationActuelle);

        // ── 3. Décrémente l'ancien groupe ───────────────────────────
        ancienGroupe.setCapaciteActuelle(Math.max(0, ancienGroupe.getCapaciteActuelle() - 1));
        if (ancienGroupe.getStatut() == StatutGroupe.COMPLET) {
            ancienGroupe.setStatut(StatutGroupe.OUVERT);
        }
        groupeRepository.save(ancienGroupe);

        // ── 4. Créer la nouvelle affectation ────────────────────────
        GroupeEtudiant nouvelleAffectation = new GroupeEtudiant();
        nouvelleAffectation.setEtudiantId(etudiantId);
        nouvelleAffectation.setGroupe(nouveauGroupe);
        nouvelleAffectation.setStatut(StatutAffectation.ACTIF);
        nouvelleAffectation.setDateAffectation(LocalDate.now());
        nouvelleAffectation.setScoreEntree(scoreAvant);
        nouvelleAffectation.setCommentaire(commentaire);
        groupeEtudiantRepository.save(nouvelleAffectation);

        // ── 5. Incrémenter le nouveau groupe ────────────────────────
        nouveauGroupe.setCapaciteActuelle(nouveauGroupe.getCapaciteActuelle() + 1);
        if (nouveauGroupe.getCapaciteActuelle().equals(nouveauGroupe.getCapaciteMax())) {
            nouveauGroupe.setStatut(StatutGroupe.COMPLET);
        }
        groupeRepository.save(nouveauGroupe);

        Integer scoreApresGroupe = groupeHealthScoreService.calculerScoreTotal(nouveauGroupe);

        // ── 6. HistoriqueGroupe alimenté automatiquement ─────────────
        HistoriqueGroupe historique = new HistoriqueGroupe();
        historique.setEtudiantId(etudiantId);
        historique.setGroupePrecedentId(ancienGroupeId);
        historique.setGroupe(nouveauGroupe);
        historique.setDateChangement(LocalDateTime.now());
        historique.setRaisonChangement(raison);
        historique.setInitiePar(initiePar);
        historique.setScoreAvant(scoreAvant != null ? scoreAvant : scoreAvantGroupe);
        historique.setScoreApres(scoreApresGroupe);
        historique.setCommentaire(commentaire);
        historique.setValidateurId(validateurId);
        historiqueRepository.save(historique);

        return nouvelleAffectation;
    }

    @Override
    public GroupeEtudiant desaffecterEtudiant(Long etudiantId, String commentaire) {

        GroupeEtudiant affectation = groupeEtudiantRepository
                .findGroupeActuelByEtudiant(etudiantId)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune affectation active pour l'étudiant " + etudiantId
                ));

        affectation.setStatut(StatutAffectation.SORTI);
        affectation.setDateSortie(LocalDate.now());
        affectation.setCommentaire(commentaire);
        GroupeEtudiant saved = groupeEtudiantRepository.save(affectation);

        // Décrémente capacité
        Groupe groupe = affectation.getGroupe();
        groupe.setCapaciteActuelle(Math.max(0, groupe.getCapaciteActuelle() - 1));
        if (groupe.getStatut() == StatutGroupe.COMPLET) {
            groupe.setStatut(StatutGroupe.OUVERT);
        }
        groupeRepository.save(groupe);

        return saved;
    }

    @Override
    public GroupeEtudiant suspendreEtudiant(Long etudiantId, String commentaire) {
        GroupeEtudiant affectation = groupeEtudiantRepository
                .findGroupeActuelByEtudiant(etudiantId)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune affectation active pour l'étudiant " + etudiantId
                ));

        affectation.setStatut(StatutAffectation.SUSPENDU);
        affectation.setCommentaire(commentaire);
        // NOTE : on ne touche PAS capaciteActuelle — la place reste réservée
        return groupeEtudiantRepository.save(affectation);
    }

    @Override
    public GroupeEtudiant reactiverEtudiant(Long etudiantId) {
        // Cherche une affectation SUSPENDU (pas ACTIF)
        List<GroupeEtudiant> affectations = groupeEtudiantRepository
                .findByStatut(StatutAffectation.SUSPENDU)
                .stream()
                .filter(a -> a.getEtudiantId().equals(etudiantId))
                .toList();

        if (affectations.isEmpty()) {
            throw new IllegalStateException(
                    "Aucune affectation suspendue pour l'étudiant " + etudiantId
            );
        }

        GroupeEtudiant affectation = affectations.get(0);
        affectation.setStatut(StatutAffectation.ACTIF);
        return groupeEtudiantRepository.save(affectation);
    }

    // ── Consultation étudiants ──────────────────────────────────────

    @Override
    public List<GroupeEtudiant> getEtudiantsActifsByGroupe(Long groupeId) {
        return groupeEtudiantRepository.findEtudiantsActifsByGroupe(groupeId);
    }

    @Override
    public GroupeEtudiant getGroupeActuelEtudiant(Long etudiantId) {
        return groupeEtudiantRepository.findGroupeActuelByEtudiant(etudiantId).orElse(null);
    }

    @Override
    public List<GroupeEtudiant> getHistoriqueEtudiant(Long etudiantId) {
        return groupeEtudiantRepository.findHistoriqueAffectationsByEtudiant(etudiantId);
    }

    @Override
    public List<Long> getEtudiantsSansGroupe() {
        return groupeEtudiantRepository.findEtudiantsSansGroupeActif();
    }

    // ================================================================
    // FORMATEURS
    // ================================================================

    @Override
    public GroupeFormateur affecterFormateurAGroupe(
            Long formateurId, Long groupeId, RoleFormateur role,
            String matiere, Integer heuresParSemaine, String commentaire) {

        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable : " + groupeId));

        // Un seul PRINCIPAL actif par groupe
        if (role == RoleFormateur.PRINCIPAL
                && groupeFormateurRepository.existsFormateurPrincipalActif(groupeId)) {
            throw new IllegalStateException(
                "Le groupe " + groupeId + " a déjà un formateur PRINCIPAL actif. " +
                "Retirez-le d'abord ou affectez avec un rôle ASSISTANT / REMPLACANT."
            );
        }

        GroupeFormateur affectation = new GroupeFormateur();
        affectation.setFormateurId(formateurId);
        affectation.setGroupe(groupe);
        affectation.setRole(role);
        affectation.setMatiere(matiere);
        affectation.setHeuresParSemaine(heuresParSemaine);
        affectation.setDateDebut(LocalDate.now());
        affectation.setEstActif(true);
        affectation.setCommentaire(commentaire);

        return groupeFormateurRepository.save(affectation);
    }

    @Override
    public GroupeFormateur retirerFormateurDuGroupe(Long formateurId, Long groupeId) {
        groupeFormateurRepository.desactiverFormateurDansGroupe(formateurId, groupeId);
        return groupeFormateurRepository
                .findFormateursActifsByGroupe(groupeId)
                .stream()
                .filter(gf -> gf.getFormateurId().equals(formateurId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public GroupeFormateur changerRoleFormateur(Long formateurId, Long groupeId, RoleFormateur nouveauRole) {
        if (nouveauRole == RoleFormateur.PRINCIPAL
                && groupeFormateurRepository.existsFormateurPrincipalActif(groupeId)) {
            throw new IllegalStateException(
                    "Le groupe a déjà un PRINCIPAL actif. Retirez-le d'abord."
            );
        }

        GroupeFormateur gf = groupeFormateurRepository
                .findFormateursActifsByGroupe(groupeId)
                .stream()
                .filter(f -> f.getFormateurId().equals(formateurId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Formateur " + formateurId + " non actif dans le groupe " + groupeId
                ));

        gf.setRole(nouveauRole);
        return groupeFormateurRepository.save(gf);
    }

    // ── Consultation formateurs ─────────────────────────────────────

    @Override
    public List<GroupeFormateur> getFormateursActifsByGroupe(Long groupeId) {
        return groupeFormateurRepository.findFormateursActifsByGroupe(groupeId);
    }

    @Override
    public GroupeFormateur getFormateurPrincipal(Long groupeId) {
        return groupeFormateurRepository.findFormateurPrincipalByGroupe(groupeId).orElse(null);
    }

    @Override
    public List<GroupeFormateur> getGroupesActifsByFormateur(Long formateurId) {
        return groupeFormateurRepository.findGroupesActifsByFormateur(formateurId);
    }

    @Override
    public List<Object[]> getGroupesSansFormateurPrincipal() {
        return groupeFormateurRepository.findGroupesSansFormateurPrincipal();
    }

    @Override
    public List<Object[]> getChargeHoraireParFormateur() {
        return groupeFormateurRepository.getChargeHoraireParFormateur();
    }

    // ================================================================
    // HELPERS PRIVÉS
    // ================================================================

    private void validerGroupeOuvert(Groupe groupe) {
        if (groupe.getStatut() == StatutGroupe.ANNULE
                || groupe.getStatut() == StatutGroupe.TERMINE) {
            throw new IllegalStateException(
                    "Impossible d'affecter au groupe " + groupe.getCodeGroupe() +
                    " — statut : " + groupe.getStatut()
            );
        }
    }

    private void validerCapaciteDisponible(Groupe groupe) {
        if (groupe.getCapaciteMax() != null
                && groupe.getCapaciteActuelle() >= groupe.getCapaciteMax()) {
            throw new IllegalStateException(
                    "Le groupe " + groupe.getCodeGroupe() + " est complet (" +
                    groupe.getCapaciteActuelle() + "/" + groupe.getCapaciteMax() + ")"
            );
        }
    }
}
