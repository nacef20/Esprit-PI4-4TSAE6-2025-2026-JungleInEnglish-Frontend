package tn.esprit.gestion_groups.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.gestion_groups.Entites.*;
import tn.esprit.gestion_groups.Repositories.*;
import tn.esprit.gestion_groups.dto.ScissionGroupeRequest;
import tn.esprit.gestion_groups.dto.ScissionResultatDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class ScissionGroupeServiceImpl implements IScissionGroupeService {

    private final GroupeRepository groupeRepository;
    private final GroupeEtudiantRepository groupeEtudiantRepository;
    private final GroupeFormateurRepository groupeFormateurRepository;
    private final ObjectifPedagogiqueRepository objectifRepository;
    private final ReglementGroupeRepository reglementRepository;
    private final HistoriqueGroupeRepository historiqueRepository;
    private final GroupeHealthScoreService groupeHealthScoreService;

    @Override
    public ScissionResultatDTO validerScission(ScissionGroupeRequest request) {
        ValidationScissionContext context = validerEtPreparer(request);
        Groupe source = context.source();
        Groupe cible = context.cible();

        String statut = context.etudiantsNonTransferables().isEmpty() ? "POSSIBLE" : "PARTIEL";
        String message = context.etudiantsNonTransferables().isEmpty()
            ? "La scission est possible. " + context.etudiantsADeplacer() + " étudiant(s) seront déplacés."
                : "Scission partielle : le groupe cible ne peut accueillir que "
              + context.placesDisponibles() + "/" + context.etudiantsADeplacer() + " étudiant(s). "
              + context.etudiantsNonTransferables().size() + " ne pourront pas être déplacés.";

        return ScissionResultatDTO.builder()
            .groupeSourceId(source.getIdGroupe())
            .groupeSourceCode(source.getCodeGroupe())
            .groupeCibleId(cible.getIdGroupe())
            .groupeCibleCode(cible.getCodeGroupe())
            .nombreEtudiantsTransferes(context.etudiantsADeplacer() - context.etudiantsNonTransferables().size())
                .nombreFormateursTransferes(0)
                .nombreObjectifsTransferes(0)
                .nombreReglementsTransferes(0)
            .etudiantsNonTransferes(context.etudiantsNonTransferables())
            .statutGroupeSource(source.getStatut() != null ? source.getStatut().name() : null)
            .statutGroupeCible(cible.getStatut() != null ? cible.getStatut().name() : null)
            .capaciteActuelleGroupeSource(context.sourceCapaciteActuelle())
            .capaciteMaxGroupeSource(context.sourceCapaciteMax())
            .capaciteActuelleGroupeCible(context.cibleCapaciteActuelle())
            .capaciteMaxGroupeCible(context.cibleCapaciteMax())
                .dateScission(LocalDateTime.now())
                .statut(statut)
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public ScissionResultatDTO scinderGroupes(ScissionGroupeRequest request) {
        ValidationScissionContext context = validerEtPreparer(request);
        Groupe source = context.source();
        Groupe cible = context.cible();
        int sourceCapaciteActuelle = context.sourceCapaciteActuelle() != null ? context.sourceCapaciteActuelle() : 0;
        int cibleCapaciteActuelle = context.cibleCapaciteActuelle() != null ? context.cibleCapaciteActuelle() : 0;
        Integer scoreAvantSource = groupeHealthScoreService.calculerScoreTotal(source);

        int etudiantsTransferes = 0;
        int formateursTransferes = 0;
        int objectifsTransferes = 0;
        int reglementsTransferes = 0;

        List<GroupeEtudiant> etudiantsAMigrer = context.etudiantsSource().stream()
                .sorted(Comparator
                        .comparing(GroupeEtudiant::getDateAffectation, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(GroupeEtudiant::getIdGroupeEtudiant, Comparator.nullsLast(Comparator.naturalOrder())))
            .limit(context.etudiantsADeplacer())
                .toList();

        for (GroupeEtudiant affectation : etudiantsAMigrer) {
            affectation.setStatut(StatutAffectation.SORTI);
            affectation.setDateSortie(LocalDate.now());
            affectation.setCommentaire("Scission groupe " + source.getCodeGroupe()
                + " → " + cible.getCodeGroupe());
            groupeEtudiantRepository.save(affectation);

            sourceCapaciteActuelle = Math.max(0, sourceCapaciteActuelle - 1);

            GroupeEtudiant nouvelleAffectation = new GroupeEtudiant();
            nouvelleAffectation.setEtudiantId(affectation.getEtudiantId());
            nouvelleAffectation.setGroupe(cible);
            nouvelleAffectation.setStatut(StatutAffectation.ACTIF);
            nouvelleAffectation.setDateAffectation(LocalDate.now());
            nouvelleAffectation.setScoreEntree(affectation.getScoreEntree());
            nouvelleAffectation.setCommentaire(request.getCommentaire());
            nouvelleAffectation.setAffectePar(request.getDeclenchePar());
            groupeEtudiantRepository.save(nouvelleAffectation);

            cibleCapaciteActuelle++;
            source.setCapaciteActuelle(sourceCapaciteActuelle);
            cible.setCapaciteActuelle(cibleCapaciteActuelle);
            groupeRepository.save(source);
            groupeRepository.save(cible);

            Integer scoreApresCible = groupeHealthScoreService.calculerScoreTotal(cible);

            HistoriqueGroupe historique = new HistoriqueGroupe();
            historique.setEtudiantId(affectation.getEtudiantId());
            historique.setGroupePrecedentId(source.getIdGroupe());
            historique.setGroupe(cible);
            historique.setDateChangement(LocalDateTime.now());
            historique.setRaisonChangement(RaisonChangement.REEQUILIBRAGE);
            historique.setInitiePar(InitiateurChangement.ADMIN);
            historique.setScoreAvant(scoreAvantSource);
            historique.setScoreApres(scoreApresCible);
            historique.setCommentaire("Scission automatique : "
                + source.getCodeGroupe() + " → " + cible.getCodeGroupe()
                    + (request.getCommentaire() != null ? " | " + request.getCommentaire() : ""));
            historique.setValidateurId(request.getDeclenchePar());
            historiqueRepository.save(historique);
            etudiantsTransferes++;
        }

        if (Boolean.TRUE.equals(request.getTransfererFormateurs())) {
            formateursTransferes = copierFormateursVersCible(source, cible, request.getCommentaire());
        }

        if (Boolean.TRUE.equals(request.getTransfererObjectifs())) {
            objectifsTransferes = copierObjectifsVersCible(source, cible, request.getCommentaire());
        }

        if (Boolean.TRUE.equals(request.getTransfererReglements())) {
            reglementsTransferes = copierReglementsVersCible(source, cible, request.getCommentaire(), request.getDeclenchePar());
        }

        source.setCapaciteActuelle(sourceCapaciteActuelle);
        cible.setCapaciteActuelle(cibleCapaciteActuelle);

        if (cible.getCapaciteMax() != null
            && cible.getCapaciteActuelle() != null
            && cible.getCapaciteActuelle().equals(cible.getCapaciteMax())) {
            cible.setStatut(StatutGroupe.COMPLET);
        }

        if (source.getCapaciteMax() != null
            && source.getCapaciteActuelle() != null
            && source.getCapaciteActuelle() < source.getCapaciteMax()) {
            source.setStatut(StatutGroupe.OUVERT);
        }

        groupeRepository.save(source);
        groupeRepository.save(cible);

        String statut = context.etudiantsNonTransferables().isEmpty() ? "SUCCES" : "PARTIEL";
        String message = buildMessage(etudiantsTransferes, formateursTransferes, objectifsTransferes,
            reglementsTransferes, context.etudiantsNonTransferables(), source, cible);

        return ScissionResultatDTO.builder()
            .groupeSourceId(source.getIdGroupe())
            .groupeSourceCode(source.getCodeGroupe())
            .groupeCibleId(cible.getIdGroupe())
            .groupeCibleCode(cible.getCodeGroupe())
                .nombreEtudiantsTransferes(etudiantsTransferes)
                .nombreFormateursTransferes(formateursTransferes)
                .nombreObjectifsTransferes(objectifsTransferes)
                .nombreReglementsTransferes(reglementsTransferes)
            .etudiantsNonTransferes(context.etudiantsNonTransferables())
            .statutGroupeSource(source.getStatut() != null ? source.getStatut().name() : null)
            .statutGroupeCible(cible.getStatut() != null ? cible.getStatut().name() : null)
            .capaciteActuelleGroupeSource(source.getCapaciteActuelle())
            .capaciteMaxGroupeSource(source.getCapaciteMax())
            .capaciteActuelleGroupeCible(cible.getCapaciteActuelle())
            .capaciteMaxGroupeCible(cible.getCapaciteMax())
                .dateScission(LocalDateTime.now())
                .statut(statut)
                .message(message)
                .build();
    }

    private ValidationScissionContext validerEtPreparer(ScissionGroupeRequest request) {
        if (request.getNombreEtudiantsADeplacer() == null || request.getNombreEtudiantsADeplacer() <= 0) {
            throw new IllegalStateException("Veuillez renseigner un nombre d'étudiants à déplacer valide.");
        }

        Groupe source = groupeRepository.findById(request.getGroupeSourceId())
                .orElseThrow(() -> new IllegalArgumentException("Groupe source introuvable : " + request.getGroupeSourceId()));

        Groupe cible = groupeRepository.findById(request.getGroupeCibleId())
                .orElseThrow(() -> new IllegalArgumentException("Groupe cible introuvable : " + request.getGroupeCibleId()));

        if (source.getIdGroupe().equals(cible.getIdGroupe())) {
            throw new IllegalArgumentException("Le groupe source et le groupe cible ne peuvent pas être identiques.");
        }

        if (source.getStatut() == StatutGroupe.ANNULE
                || source.getStatut() == StatutGroupe.TERMINE
                || source.getStatut() == StatutGroupe.ARCHIVE) {
            throw new IllegalStateException("Le groupe source ne peut pas être scindé — statut : " + source.getStatut());
        }

        if (cible.getStatut() == StatutGroupe.ANNULE
                || cible.getStatut() == StatutGroupe.TERMINE
                || cible.getStatut() == StatutGroupe.ARCHIVE) {
            throw new IllegalStateException("Le groupe cible ne peut pas recevoir de membres — statut : " + cible.getStatut());
        }

        List<GroupeEtudiant> etudiantsSource = groupeEtudiantRepository.findEtudiantsActifsByGroupe(source.getIdGroupe())
                .stream()
                .sorted(Comparator
                        .comparing(GroupeEtudiant::getDateAffectation, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(GroupeEtudiant::getIdGroupeEtudiant, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        int etudiantsADeplacer = request.getNombreEtudiantsADeplacer();
        if (etudiantsADeplacer > etudiantsSource.size()) {
            throw new IllegalStateException(
                    "La scission demande " + etudiantsADeplacer + " étudiant(s) mais le groupe source n'en contient que "
                            + etudiantsSource.size() + "."
            );
        }

        Integer sourceCapaciteActuelle = source.getCapaciteActuelle() != null ? source.getCapaciteActuelle() : etudiantsSource.size();
        Integer cibleCapaciteActuelle = cible.getCapaciteActuelle() != null ? cible.getCapaciteActuelle() : 0;
        Integer cibleCapaciteMax = cible.getCapaciteMax();

        if (Boolean.TRUE.equals(request.getTransfererEtudiants()) && cibleCapaciteMax == null) {
            throw new IllegalStateException("Le groupe cible " + cible.getCodeGroupe() + " n'a pas de capaciteMax.");
        }

        int placesDisponibles = cibleCapaciteMax != null ? Math.max(0, cibleCapaciteMax - cibleCapaciteActuelle) : 0;
        int transfertsPossibles = Boolean.TRUE.equals(request.getTransfererEtudiants())
                ? Math.min(etudiantsADeplacer, placesDisponibles)
                : 0;

        List<Long> nonTransferes = new ArrayList<>();
        if (Boolean.TRUE.equals(request.getTransfererEtudiants()) && etudiantsADeplacer > placesDisponibles) {
            etudiantsSource.stream()
                    .skip(placesDisponibles)
                    .limit(etudiantsADeplacer - placesDisponibles)
                    .map(GroupeEtudiant::getEtudiantId)
                    .forEach(nonTransferes::add);
        }

        return new ValidationScissionContext(
                source,
                cible,
                etudiantsSource,
                sourceCapaciteActuelle,
                source.getCapaciteMax(),
                cibleCapaciteActuelle,
                cibleCapaciteMax,
                etudiantsADeplacer,
                transfertsPossibles,
                placesDisponibles,
                nonTransferes
        );
    }

    private int copierFormateursVersCible(Groupe source, Groupe cible, String commentaire) {
        int copies = 0;
        List<GroupeFormateur> formateurs = groupeFormateurRepository.findFormateursActifsByGroupe(source.getIdGroupe());
        for (GroupeFormateur gf : formateurs) {
            GroupeFormateur copie = new GroupeFormateur();
            copie.setFormateurId(gf.getFormateurId());
            copie.setGroupe(cible);
            copie.setRole(gf.getRole());
            copie.setMatiere(gf.getMatiere());
            copie.setHeuresParSemaine(gf.getHeuresParSemaine());
            copie.setDateDebut(LocalDate.now());
            copie.setEstActif(true);
            copie.setCommentaire("Scission depuis " + source.getCodeGroupe()
                    + (commentaire != null && !commentaire.isBlank() ? " | " + commentaire : ""));

            if (copie.getRole() == RoleFormateur.PRINCIPAL && groupeFormateurRepository.existsFormateurPrincipalActif(cible.getIdGroupe())) {
                copie.setRole(RoleFormateur.ASSISTANT);
            }

            groupeFormateurRepository.save(copie);
            copies++;
        }
        return copies;
    }

    private int copierObjectifsVersCible(Groupe source, Groupe cible, String commentaire) {
        int copies = 0;
        List<ObjectifPedagogique> objectifs = objectifRepository.findByGroupeIdGroupe(source.getIdGroupe());
        for (ObjectifPedagogique obj : objectifs) {
            ObjectifPedagogique copie = new ObjectifPedagogique();
            copie.setCodeObjectif(genererCodeUniqueObjectif(obj.getCodeObjectif(), cible.getCodeGroupe()));
            copie.setTitre(obj.getTitre());
            copie.setDescription(obj.getDescription());
            copie.setCompetenceCible(obj.getCompetenceCible());
            copie.setNiveauDifficulte(obj.getNiveauDifficulte());
            copie.setPriorite(obj.getPriorite());
            copie.setDateDebutPrevue(obj.getDateDebutPrevue());
            copie.setDateFinPrevue(obj.getDateFinPrevue());
            copie.setDureeEstimeeHeures(obj.getDureeEstimeeHeures());
            copie.setPrerequis(obj.getPrerequis());
            copie.setStatut(obj.getStatut());
            copie.setTauxReussiteGroupe(obj.getTauxReussiteGroupe());
            copie.setNombreSeancesNecessaires(obj.getNombreSeancesNecessaires());
            copie.setSupportsPedagogiques(obj.getSupportsPedagogiques());
            copie.setMethodeEvaluation(obj.getMethodeEvaluation());
            copie.setCommentaireProfesseur(obj.getCommentaireProfesseur());
            copie.setGroupe(cible);
            if (commentaire != null && !commentaire.isBlank()) {
                copie.setCommentaireProfesseur((copie.getCommentaireProfesseur() != null ? copie.getCommentaireProfesseur() + " | " : "") + commentaire);
            }
            objectifRepository.save(copie);
            copies++;
        }
        return copies;
    }

    private int copierReglementsVersCible(Groupe source, Groupe cible, String commentaire, Long declenchePar) {
        int copies = 0;
        List<ReglementGroupe> reglements = reglementRepository.findByGroupeIdGroupe(source.getIdGroupe());
        for (ReglementGroupe reg : reglements) {
            ReglementGroupe copie = new ReglementGroupe();
            copie.setCodeReglement(genererCodeUniqueReglement(reg.getCodeReglement(), cible.getCodeGroupe()));
            copie.setTitre(reg.getTitre());
            copie.setCategorie(reg.getCategorie());
            copie.setDescriptionRegle(reg.getDescriptionRegle());
            copie.setConsequence(reg.getConsequence());
            copie.setEstObligatoire(reg.getEstObligatoire());
            copie.setTolerance(reg.getTolerance());
            copie.setSanction(reg.getSanction());
            copie.setSeuilApplication(reg.getSeuilApplication());
            copie.setEstActif(reg.getEstActif());
            copie.setDateApplication(reg.getDateApplication());
            copie.setCreatedBy(declenchePar != null ? declenchePar : reg.getCreatedBy());
            copie.setGroupe(cible);
            if (commentaire != null && !commentaire.isBlank()) {
                copie.setConsequence((copie.getConsequence() != null ? copie.getConsequence() + " | " : "") + commentaire);
            }
            reglementRepository.save(copie);
            copies++;
        }
        return copies;
    }

    private String genererCodeUniqueObjectif(String baseCode, String suffix) {
        String sanitizedBase = baseCode != null ? baseCode.trim() : "OBJ";
        String sanitizedSuffix = suffix != null ? suffix.replaceAll("[^A-Za-z0-9]", "") : "SC";
        int index = 1;
        String candidate;
        do {
            candidate = sanitizedBase + "-SC-" + sanitizedSuffix + "-" + index;
            index++;
        } while (objectifRepository.findByCodeObjectif(candidate).isPresent());
        return candidate;
    }

    private String genererCodeUniqueReglement(String baseCode, String suffix) {
        String sanitizedBase = baseCode != null ? baseCode.trim() : "REG";
        String sanitizedSuffix = suffix != null ? suffix.replaceAll("[^A-Za-z0-9]", "") : "SC";
        int index = 1;
        String candidate;
        do {
            candidate = sanitizedBase + "-SC-" + sanitizedSuffix + "-" + index;
            index++;
        } while (reglementRepository.findByCodeReglement(candidate).isPresent());
        return candidate;
    }

    private String buildMessage(int etudiants, int formateurs, int objectifs,
                                int reglements, List<Long> nonTransferes,
                                Groupe source, Groupe cible) {
        StringBuilder sb = new StringBuilder("Scission effectuée. ");
        sb.append(etudiants).append(" étudiant(s) déplacé(s). ");
        if (formateurs > 0) sb.append(formateurs).append(" formateur(s) copié(s). ");
        if (objectifs > 0) sb.append(objectifs).append(" objectif(s) copié(s). ");
        if (reglements > 0) sb.append(reglements).append(" règlement(s) copié(s). ");
        if (!nonTransferes.isEmpty()) {
            sb.append("⚠ ").append(nonTransferes.size())
              .append(" étudiant(s) n'ont pas pu être déplacés — capacité cible insuffisante. ");
        }
        sb.append("Groupes source et cible restent actifs.");
        return sb.toString();
    }

    private record ValidationScissionContext(
            Groupe source,
            Groupe cible,
            List<GroupeEtudiant> etudiantsSource,
            Integer sourceCapaciteActuelle,
            Integer sourceCapaciteMax,
            Integer cibleCapaciteActuelle,
            Integer cibleCapaciteMax,
            int etudiantsADeplacer,
            int etudiantsADeplacerEffectifs,
            int placesDisponibles,
            List<Long> etudiantsNonTransferables
    ) {
    }
}