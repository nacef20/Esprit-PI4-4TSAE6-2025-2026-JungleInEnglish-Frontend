package tn.esprit.gestion_groups.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.gestion_groups.dto.FusionGroupeRequest;
import tn.esprit.gestion_groups.dto.FusionResultatDTO;
import tn.esprit.gestion_groups.Entites.*;
import tn.esprit.gestion_groups.Repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FusionGroupeServiceImpl implements IFusionGroupeService {

    private final GroupeRepository            groupeRepository;
    private final GroupeEtudiantRepository    groupeEtudiantRepository;
    private final GroupeFormateurRepository   groupeFormateurRepository;
    private final ObjectifPedagogiqueRepository objectifRepository;
    private final ReglementGroupeRepository   reglementRepository;
    private final HistoriqueGroupeRepository  historiqueRepository;
        private final GroupeHealthScoreService     groupeHealthScoreService;

    // ================================================================
    // VALIDATION SANS EXÉCUTION
    // ================================================================

    @Override
    public FusionResultatDTO validerFusion(FusionGroupeRequest request) {

        Groupe source = groupeRepository.findById(request.getGroupeSourceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Groupe source introuvable : " + request.getGroupeSourceId()));

        Groupe cible = groupeRepository.findById(request.getGroupeCibleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Groupe cible introuvable : " + request.getGroupeCibleId()));

        // Récupère les étudiants ACTIFS du groupe source
        List<GroupeEtudiant> etudiantsSource =
                groupeEtudiantRepository.findEtudiantsActifsByGroupe(source.getIdGroupe());

        Integer capaciteMaxCible = cible.getCapaciteMax();
        Integer capaciteActuelleCible = cible.getCapaciteActuelle() != null ? cible.getCapaciteActuelle() : 0;

        if (Boolean.TRUE.equals(request.getTransfererEtudiants()) && capaciteMaxCible == null) {
            throw new IllegalStateException(
                    "Le groupe cible " + cible.getCodeGroupe() + " n'a pas de capaciteMax. " +
                    "Renseignez la capacite avant d'executer une fusion avec transfert d'etudiants."
            );
        }

        int placesDisponibles = capaciteMaxCible != null ? Math.max(0, capaciteMaxCible - capaciteActuelleCible) : 0;
        int etudiantsATransferer = Boolean.TRUE.equals(request.getTransfererEtudiants())
                ? etudiantsSource.size() : 0;

        List<Long> etudiantsNonTransferables = new ArrayList<>();
        if (etudiantsATransferer > placesDisponibles) {
            // Calcule combien ne peuvent pas être transférés
            etudiantsSource.stream()
                    .skip(placesDisponibles)
                    .map(GroupeEtudiant::getEtudiantId)
                    .forEach(etudiantsNonTransferables::add);
        }

        String statut = etudiantsNonTransferables.isEmpty() ? "POSSIBLE" : "PARTIEL";
        String message = etudiantsNonTransferables.isEmpty()
                ? "La fusion est possible. " + etudiantsATransferer + " étudiant(s) seront transférés."
                : "Fusion partielle : le groupe cible ne peut accueillir que "
                  + placesDisponibles + "/" + etudiantsATransferer + " étudiant(s). "
                  + etudiantsNonTransferables.size() + " ne pourront pas être transférés.";

        return FusionResultatDTO.builder()
                .groupeSourceId(source.getIdGroupe())
                .groupeSourceCode(source.getCodeGroupe())
                .groupeCibleId(cible.getIdGroupe())
                .groupeCibleCode(cible.getCodeGroupe())
                .nombreEtudiantsTransferes(etudiantsATransferer - etudiantsNonTransferables.size())
                .nombreFormateursTransferes(0)
                .nombreObjectifsTransferes(0)
                .nombreReglementsTransferes(0)
                .etudiantsNonTransferes(etudiantsNonTransferables)
                .capaciteActuelleGroupeCible(capaciteActuelleCible)
                .capaciteMaxGroupeCible(capaciteMaxCible)
                .dateFusion(LocalDateTime.now())
                .statut(statut)
                .message(message)
                .build();
    }

    // ================================================================
    // FUSION RÉELLE — TRANSACTIONNELLE
    // ================================================================

    @Override
    @Transactional
    public FusionResultatDTO fusionnerGroupes(FusionGroupeRequest request) {

        // ── 1. Validation des groupes ───────────────────────────────
        Groupe source = groupeRepository.findById(request.getGroupeSourceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Groupe source introuvable : " + request.getGroupeSourceId()));

        Groupe cible = groupeRepository.findById(request.getGroupeCibleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Groupe cible introuvable : " + request.getGroupeCibleId()));

        validerFusionPossible(source, cible);

        Integer scoreAvantSource = groupeHealthScoreService.calculerScoreTotal(source);

                // Defensive defaults to avoid NullPointerException on legacy/incomplete data.
                if (source.getCapaciteActuelle() == null) {
                        source.setCapaciteActuelle(0);
                }
                if (cible.getCapaciteActuelle() == null) {
                        cible.setCapaciteActuelle(0);
                }
                if (Boolean.TRUE.equals(request.getTransfererEtudiants()) && cible.getCapaciteMax() == null) {
                        throw new IllegalStateException(
                                        "Le groupe cible " + cible.getCodeGroupe() + " n'a pas de capaciteMax. " +
                                        "Renseignez la capacite avant d'executer la fusion."
                        );
                }

        // Compteurs pour le rapport final
        int etudiantsTransferes   = 0;
        int formateursTransferes  = 0;
        int objectifsTransferes   = 0;
        int reglementsTransferes  = 0;
        List<Long> etudiantsNonTransferes = new ArrayList<>();

        // ── 2. Transfert des étudiants ──────────────────────────────
        if (Boolean.TRUE.equals(request.getTransfererEtudiants())) {

            List<GroupeEtudiant> etudiantsActifs =
                    groupeEtudiantRepository.findEtudiantsActifsByGroupe(source.getIdGroupe());

            for (GroupeEtudiant affectation : etudiantsActifs) {

                // Vérifie que le groupe cible n'est pas plein
                                if (cible.getCapaciteMax() != null && cible.getCapaciteActuelle() >= cible.getCapaciteMax()) {
                    etudiantsNonTransferes.add(affectation.getEtudiantId());
                    continue;
                }

                // a) Clôturer l'ancienne affectation
                affectation.setStatut(StatutAffectation.SORTI);
                affectation.setDateSortie(LocalDate.now());
                affectation.setCommentaire("Fusion groupe " + source.getCodeGroupe()
                        + " → " + cible.getCodeGroupe());
                groupeEtudiantRepository.save(affectation);

                // b) Créer la nouvelle affectation dans le groupe cible
                GroupeEtudiant nouvelleAffectation = new GroupeEtudiant();
                nouvelleAffectation.setEtudiantId(affectation.getEtudiantId());
                nouvelleAffectation.setGroupe(cible);
                nouvelleAffectation.setStatut(StatutAffectation.ACTIF);
                nouvelleAffectation.setDateAffectation(LocalDate.now());
                nouvelleAffectation.setScoreEntree(affectation.getScoreEntree());
                nouvelleAffectation.setCommentaire(request.getCommentaire());
                nouvelleAffectation.setAffectePar(request.getDeclenchePar());
                groupeEtudiantRepository.save(nouvelleAffectation);

                cible.setCapaciteActuelle(cible.getCapaciteActuelle() + 1);
                groupeRepository.save(cible);

                Integer scoreApresCible = groupeHealthScoreService.calculerScoreTotal(cible);

                // c) Créer l'entrée HistoriqueGroupe automatiquement
                HistoriqueGroupe historique = new HistoriqueGroupe();
                historique.setEtudiantId(affectation.getEtudiantId());
                historique.setGroupePrecedentId(source.getIdGroupe());
                historique.setGroupe(cible);
                historique.setDateChangement(LocalDateTime.now());
                historique.setRaisonChangement(RaisonChangement.REEQUILIBRAGE);
                historique.setInitiePar(InitiateurChangement.ADMIN);
                historique.setScoreAvant(scoreAvantSource);
                historique.setScoreApres(scoreApresCible);
                historique.setCommentaire("Fusion automatique : "
                        + source.getCodeGroupe() + " → " + cible.getCodeGroupe()
                        + (request.getCommentaire() != null ? " | " + request.getCommentaire() : ""));
                historique.setValidateurId(request.getDeclenchePar());
                historiqueRepository.save(historique);

                etudiantsTransferes++;
            }

            // Met à jour la capacité du groupe cible si complet
                        if (cible.getCapaciteMax() != null && cible.getCapaciteActuelle().equals(cible.getCapaciteMax())) {
                cible.setStatut(StatutGroupe.COMPLET);
            }
        }

        // ── 3. Transfert des formateurs ─────────────────────────────
        if (Boolean.TRUE.equals(request.getTransfererFormateurs())) {

            List<GroupeFormateur> formateurs =
                    groupeFormateurRepository.findFormateursActifsByGroupe(source.getIdGroupe());

            for (GroupeFormateur gf : formateurs) {

                // Skip si le rôle PRINCIPAL est déjà pris dans le groupe cible
                if (gf.getRole() == RoleFormateur.PRINCIPAL
                        && groupeFormateurRepository.existsFormateurPrincipalActif(cible.getIdGroupe())) {
                    // Dégrade en ASSISTANT automatiquement
                    gf.setRole(RoleFormateur.ASSISTANT);
                }

                // Désactive dans le groupe source
                gf.setEstActif(false);
                gf.setDateFin(LocalDate.now());
                groupeFormateurRepository.save(gf);

                // Crée une nouvelle affectation dans le groupe cible
                GroupeFormateur nouvelleAffectation = new GroupeFormateur();
                nouvelleAffectation.setFormateurId(gf.getFormateurId());
                nouvelleAffectation.setGroupe(cible);
                nouvelleAffectation.setRole(gf.getRole());
                nouvelleAffectation.setMatiere(gf.getMatiere());
                nouvelleAffectation.setHeuresParSemaine(gf.getHeuresParSemaine());
                nouvelleAffectation.setDateDebut(LocalDate.now());
                nouvelleAffectation.setEstActif(true);
                nouvelleAffectation.setCommentaire("Transféré depuis fusion "
                        + source.getCodeGroupe());
                groupeFormateurRepository.save(nouvelleAffectation);

                formateursTransferes++;
            }
        }

        // ── 4. Transfert des objectifs ──────────────────────────────
        if (Boolean.TRUE.equals(request.getTransfererObjectifs())) {

            List<ObjectifPedagogique> objectifs =
                    objectifRepository.findByGroupeIdGroupe(source.getIdGroupe());

            for (ObjectifPedagogique obj : objectifs) {
                obj.setGroupe(cible);
                objectifRepository.save(obj);
                objectifsTransferes++;
            }
        }

        // ── 5. Transfert des règlements ─────────────────────────────
        if (Boolean.TRUE.equals(request.getTransfererReglements())) {

            List<ReglementGroupe> reglements =
                    reglementRepository.findByGroupeIdGroupe(source.getIdGroupe());

            for (ReglementGroupe reg : reglements) {
                reg.setGroupe(cible);
                reglementRepository.save(reg);
                reglementsTransferes++;
            }
        }

        // ── 6. Archiver/logiquement fermer le groupe source ─────────
        // Some legacy MySQL schemas define groupes.statut as ENUM without ARCHIVE.
        // Use ANNULE as a compatible persisted status to avoid SQL truncation errors.
        source.setStatut(StatutGroupe.ANNULE);
        source.setCapaciteActuelle(0);
        String nomSource = source.getNom() != null ? source.getNom() : source.getCodeGroupe();
        String codeCible = cible.getCodeGroupe() != null ? cible.getCodeGroupe() : String.valueOf(cible.getIdGroupe());
        source.setNom(nomSource + " [ARCHIVE - fusionne dans " + codeCible + "]");
        groupeRepository.save(source);

        // ── 7. Sauvegarder le groupe cible ──────────────────────────
        groupeRepository.save(cible);

        // ── 8. Construire le rapport ────────────────────────────────
        String statut = etudiantsNonTransferes.isEmpty() ? "SUCCES" : "PARTIEL";
        String message = buildMessage(etudiantsTransferes, formateursTransferes,
                objectifsTransferes, reglementsTransferes, etudiantsNonTransferes);

        return FusionResultatDTO.builder()
                .groupeSourceId(source.getIdGroupe())
                .groupeSourceCode(source.getCodeGroupe())
                .groupeCibleId(cible.getIdGroupe())
                .groupeCibleCode(cible.getCodeGroupe())
                .nombreEtudiantsTransferes(etudiantsTransferes)
                .nombreFormateursTransferes(formateursTransferes)
                .nombreObjectifsTransferes(objectifsTransferes)
                .nombreReglementsTransferes(reglementsTransferes)
                .etudiantsNonTransferes(etudiantsNonTransferes)
                .statutGroupeSource(source.getStatut() != null ? source.getStatut().name() : null)
                .capaciteActuelleGroupeCible(cible.getCapaciteActuelle())
                .capaciteMaxGroupeCible(cible.getCapaciteMax())
                .dateFusion(LocalDateTime.now())
                .statut(statut)
                .message(message)
                .build();
    }

    // ================================================================
    // HELPERS PRIVÉS
    // ================================================================

    private void validerFusionPossible(Groupe source, Groupe cible) {

        if (source.getIdGroupe().equals(cible.getIdGroupe())) {
            throw new IllegalArgumentException(
                    "Le groupe source et le groupe cible ne peuvent pas être identiques.");
        }

        if (source.getStatut() == StatutGroupe.ARCHIVE
                || source.getStatut() == StatutGroupe.ANNULE) {
            throw new IllegalStateException(
                    "Le groupe source est déjà archivé/annulé : " + source.getCodeGroupe()
                    + " (statut : " + source.getStatut() + ")");
        }

        if (cible.getStatut() == StatutGroupe.ANNULE
                || cible.getStatut() == StatutGroupe.TERMINE
                || cible.getStatut() == StatutGroupe.ARCHIVE) {
            throw new IllegalStateException(
                    "Le groupe cible ne peut pas recevoir de membres — statut : "
                    + cible.getStatut());
        }
    }

    private String buildMessage(int etudiants, int formateurs,
                                int objectifs, int reglements,
                                List<Long> nonTransferes) {

        StringBuilder sb = new StringBuilder("Fusion effectuée. ");
        sb.append(etudiants).append(" étudiant(s) transféré(s). ");
        if (formateurs > 0) sb.append(formateurs).append(" formateur(s) transféré(s). ");
        if (objectifs  > 0) sb.append(objectifs).append(" objectif(s) rattaché(s). ");
        if (reglements > 0) sb.append(reglements).append(" règlement(s) rattaché(s). ");
        if (!nonTransferes.isEmpty()) {
            sb.append("⚠ ").append(nonTransferes.size())
              .append(" étudiant(s) non transféré(s) — groupe cible plein.");
        }
        sb.append(" Groupe source archivé.");
        return sb.toString();
    }
}
