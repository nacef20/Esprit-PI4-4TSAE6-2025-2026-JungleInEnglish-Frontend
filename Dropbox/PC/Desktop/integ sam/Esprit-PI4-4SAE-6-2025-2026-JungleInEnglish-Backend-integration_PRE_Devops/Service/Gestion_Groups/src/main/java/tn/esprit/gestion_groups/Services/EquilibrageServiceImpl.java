package tn.esprit.gestion_groups.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.gestion_groups.dto.MouvementEquilibrageDTO;
import tn.esprit.gestion_groups.dto.PlanEquilibrageDTO;
import tn.esprit.gestion_groups.Entites.*;
import tn.esprit.gestion_groups.Repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EquilibrageServiceImpl implements IEquilibrageService {

    private final GroupeRepository groupeRepository;
    private final GroupeEtudiantRepository groupeEtudiantRepository;
    private final HistoriqueGroupeRepository historiqueRepository;
        private final GroupeHealthScoreService groupeHealthScoreService;

    private static final List<StatutGroupe> STATUTS_EXCLUS =
            Arrays.asList(StatutGroupe.ANNULE, StatutGroupe.TERMINE, StatutGroupe.ARCHIVE);


    @Override
    public List<Long> getNiveauxEligibles() {
        return groupeRepository.findNiveauxAvecPlusieursGroupes(STATUTS_EXCLUS);
    }



    @Override
    public PlanEquilibrageDTO genererPlanEquilibrage(Long niveauId, int seuilTolerance) {

        // 1. Récupérer les groupes du niveau
        List<Object[]> rawData = groupeRepository.getGroupesParNiveauPourEquilibrage(
                niveauId, StatutObjectif.EN_COURS, STATUTS_EXCLUS);

        if (rawData.size() < 2) {
            return PlanEquilibrageDTO.builder()
                    .niveauId(niveauId)
                    .nombreGroupes(rawData.size())
                    .totalEtudiants(0)
                    .cibleIdeale(0)
                    .seuilTolerance(seuilTolerance)
                    .mouvements(Collections.emptyList())
                    .totalTransferts(0)
                    .ecartTypeAvant(0)
                    .ecartTypeApres(0)
                    .ameliorationPourcent(0)
                    .statut("SIMULE")
                    .message("Moins de 2 groupes actifs pour ce niveau. Équilibrage impossible.")
                    .dateGeneration(LocalDateTime.now())
                    .build();
        }

        // 2. Extraire les groupes avec leurs capacités
        List<GroupeInfo> groupes = rawData.stream()
                .map(row -> {
                    Groupe g = (Groupe) row[0];
                    return new GroupeInfo(
                            g.getIdGroupe(),
                            g.getCodeGroupe(),
                            g.getCapaciteActuelle() != null ? g.getCapaciteActuelle() : 0,
                            g.getCapaciteMin() != null ? g.getCapaciteMin() : 0,
                            g.getCapaciteMax() != null ? g.getCapaciteMax() : Integer.MAX_VALUE
                    );
                })
                .collect(Collectors.toList());

        int totalEtudiants = groupes.stream().mapToInt(g -> g.capaciteActuelle).sum();
        int nombreGroupes = groupes.size();
        int cibleIdeale = totalEtudiants / nombreGroupes;

        // 3. Calculer l'écart-type AVANT
        double ecartTypeAvant = calculerEcartType(groupes.stream()
                .mapToInt(g -> g.capaciteActuelle).toArray());


        List<MouvementEquilibrageDTO> mouvements = new ArrayList<>();

        // Copie pour simulation
        Map<Long, Integer> capacitesSimulees = new HashMap<>();
        Map<Long, String> codesGroupes = new HashMap<>();
        Map<Long, Integer> capacitesAvant = new HashMap<>();
        Map<Long, Integer> capacitesMax = new HashMap<>();

        for (GroupeInfo gi : groupes) {
            capacitesSimulees.put(gi.id, gi.capaciteActuelle);
            codesGroupes.put(gi.id, gi.code);
            capacitesAvant.put(gi.id, gi.capaciteActuelle);
            capacitesMax.put(gi.id, gi.capaciteMax);
        }

        // Boucle d'équilibrage
        boolean mouvementEffectue = true;
        int maxIterations = 100; // sécurité
        int iteration = 0;

        while (mouvementEffectue && iteration < maxIterations) {
            mouvementEffectue = false;
            iteration++;

            // Trouver le groupe le plus rempli (donneur) et le moins rempli (receveur)
            Long donneurId = null;
            Long receveurId = null;
            int maxCapacite = Integer.MIN_VALUE;
            int minCapacite = Integer.MAX_VALUE;

            for (GroupeInfo gi : groupes) {
                int cap = capacitesSimulees.get(gi.id);
                if (cap > cibleIdeale + seuilTolerance && cap > maxCapacite) {
                    maxCapacite = cap;
                    donneurId = gi.id;
                }
                if (cap < cibleIdeale - seuilTolerance && cap < minCapacite) {
                    minCapacite = cap;
                    receveurId = gi.id;
                }
            }

            if (donneurId != null && receveurId != null) {
                // Calculer combien transférer
                int excedent = capacitesSimulees.get(donneurId) - cibleIdeale;
                int deficit = cibleIdeale - capacitesSimulees.get(receveurId);
                int maxAcceptable = capacitesMax.get(receveurId) - capacitesSimulees.get(receveurId);

                int aTransferer = Math.min(excedent, Math.min(deficit, maxAcceptable));

                if (aTransferer > 0) {
                    mouvements.add(MouvementEquilibrageDTO.builder()
                            .groupeSourceId(donneurId)
                            .groupeSourceCode(codesGroupes.get(donneurId))
                            .capaciteAvantSource(capacitesAvant.get(donneurId))
                            .groupeCibleId(receveurId)
                            .groupeCibleCode(codesGroupes.get(receveurId))
                            .capaciteAvantCible(capacitesAvant.get(receveurId))
                            .nombreEtudiantsATransferer(aTransferer)
                            .statut("SIMULE")
                            .message(String.format("%d étudiant(s) : %s → %s",
                                    aTransferer,
                                    codesGroupes.get(donneurId),
                                    codesGroupes.get(receveurId)))
                            .build());

                    // Mettre à jour les capacités simulées
                    capacitesSimulees.put(donneurId,
                            capacitesSimulees.get(donneurId) - aTransferer);
                    capacitesSimulees.put(receveurId,
                            capacitesSimulees.get(receveurId) + aTransferer);

                    mouvementEffectue = true;
                }
            }
        }

        // 5. Calculer l'écart-type APRÈS
        double ecartTypeApres = calculerEcartType(capacitesSimulees.values().stream()
                .mapToInt(Integer::intValue).toArray());

        double amelioration = ecartTypeAvant > 0
                ? Math.round(((ecartTypeAvant - ecartTypeApres) / ecartTypeAvant) * 1000.0) / 10.0
                : 0.0;

        int totalTransferts = mouvements.stream()
                .mapToInt(MouvementEquilibrageDTO::getNombreEtudiantsATransferer).sum();

        String message = mouvements.isEmpty()
                ? "Les groupes sont déjà équilibrés (écart-type: " + String.format("%.1f", ecartTypeAvant) + ")."
                : String.format("Plan généré : %d mouvement(s), %d transfert(s) total. " +
                                "Écart-type avant: %.1f → après: %.1f (amélioration: %.1f%%).",
                        mouvements.size(), totalTransferts,
                        ecartTypeAvant, ecartTypeApres, amelioration);

        return PlanEquilibrageDTO.builder()
                .niveauId(niveauId)
                .nombreGroupes(nombreGroupes)
                .totalEtudiants(totalEtudiants)
                .cibleIdeale(cibleIdeale)
                .seuilTolerance(seuilTolerance)
                .mouvements(mouvements)
                .totalTransferts(totalTransferts)
                .ecartTypeAvant(Math.round(ecartTypeAvant * 100.0) / 100.0)
                .ecartTypeApres(Math.round(ecartTypeApres * 100.0) / 100.0)
                .ameliorationPourcent(amelioration)
                .statut("SIMULE")
                .message(message)
                .dateGeneration(LocalDateTime.now())
                .build();
    }

    // ================================================================
    // EXÉCUTION DU PLAN (IRRÉVERSIBLE)
    // ================================================================

    @Override
    @Transactional
    public PlanEquilibrageDTO executerPlanEquilibrage(Long niveauId, int seuilTolerance) {

        // 1. Générer le plan
        PlanEquilibrageDTO plan = genererPlanEquilibrage(niveauId, seuilTolerance);

        if (plan.getMouvements().isEmpty()) {
            plan.setStatut("EXECUTE");
            plan.setMessage("Aucun mouvement nécessaire. Groupes déjà équilibrés.");
            return plan;
        }

        // 2. Exécuter chaque mouvement
        for (MouvementEquilibrageDTO mouvement : plan.getMouvements()) {
            executerMouvement(mouvement);
        }

        plan.setStatut("EXECUTE");
        plan.setMessage(plan.getMessage().replace("Plan généré", "Plan exécuté"));
        return plan;
    }

    /**
     * Exécute un mouvement individuel :
     *  1. Récupère N étudiants ACTIF du groupe source
     *  2. Clôture leur affectation (SORTI)
     *  3. Crée une nouvelle affectation dans le groupe cible
     *  4. Crée un HistoriqueGroupe pour la traçabilité
     *  5. Met à jour les capacités
     */
    private void executerMouvement(MouvementEquilibrageDTO mouvement) {

        Groupe source = groupeRepository.findById(mouvement.getGroupeSourceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Groupe source introuvable : " + mouvement.getGroupeSourceId()));

        Groupe cible = groupeRepository.findById(mouvement.getGroupeCibleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Groupe cible introuvable : " + mouvement.getGroupeCibleId()));

        List<GroupeEtudiant> etudiantsSource =
                groupeEtudiantRepository.findEtudiantsActifsByGroupe(source.getIdGroupe());

        Integer scoreAvantSource = groupeHealthScoreService.calculerScoreTotal(source);

        int transferes = 0;
        for (GroupeEtudiant affectation : etudiantsSource) {
            if (transferes >= mouvement.getNombreEtudiantsATransferer()) break;

            // Vérifier que le groupe cible accepte encore
            if (cible.getCapaciteMax() != null
                    && cible.getCapaciteActuelle() >= cible.getCapaciteMax()) {
                break;
            }

            // a) Clôturer l'ancienne affectation
            affectation.setStatut(StatutAffectation.SORTI);
            affectation.setDateSortie(LocalDate.now());
            affectation.setCommentaire("Équilibrage automatique : "
                    + source.getCodeGroupe() + " → " + cible.getCodeGroupe());
            groupeEtudiantRepository.save(affectation);

            // b) Créer nouvelle affectation
            GroupeEtudiant nouvelle = new GroupeEtudiant();
            nouvelle.setEtudiantId(affectation.getEtudiantId());
            nouvelle.setGroupe(cible);
            nouvelle.setStatut(StatutAffectation.ACTIF);
            nouvelle.setDateAffectation(LocalDate.now());
            nouvelle.setScoreEntree(affectation.getScoreEntree());
            nouvelle.setCommentaire("Transféré par équilibrage automatique");
            groupeEtudiantRepository.save(nouvelle);

            source.setCapaciteActuelle((source.getCapaciteActuelle() != null ? source.getCapaciteActuelle() : 1) - 1);
            cible.setCapaciteActuelle((cible.getCapaciteActuelle() != null ? cible.getCapaciteActuelle() : 0) + 1);

            groupeRepository.save(source);
            groupeRepository.save(cible);

            Integer scoreApresCible = groupeHealthScoreService.calculerScoreTotal(cible);

            // c) Historique
            HistoriqueGroupe historique = new HistoriqueGroupe();
            historique.setEtudiantId(affectation.getEtudiantId());
            historique.setGroupePrecedentId(source.getIdGroupe());
            historique.setGroupe(cible);
            historique.setDateChangement(LocalDateTime.now());
            historique.setRaisonChangement(RaisonChangement.REEQUILIBRAGE);
            historique.setInitiePar(InitiateurChangement.SYSTEME);
            historique.setScoreAvant(scoreAvantSource);
            historique.setScoreApres(scoreApresCible);
            historique.setCommentaire("Équilibrage automatique niveau " +
                    source.getNiveauId() + " : " +
                    source.getCodeGroupe() + " → " + cible.getCodeGroupe());
            historiqueRepository.save(historique);

            transferes++;
        }

        groupeRepository.save(source);
        groupeRepository.save(cible);

        mouvement.setStatut("EXECUTE");
        mouvement.setMessage(String.format("%d étudiant(s) transféré(s) : %s → %s",
                transferes, source.getCodeGroupe(), cible.getCodeGroupe()));
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private double calculerEcartType(int[] valeurs) {
        if (valeurs.length == 0) return 0;
        double moyenne = Arrays.stream(valeurs).average().orElse(0);
        double sommeCarres = 0;
        for (int v : valeurs) {
            sommeCarres += (v - moyenne) * (v - moyenne);
        }
        return Math.sqrt(sommeCarres / valeurs.length);
    }

    /** Structure interne pour manipuler les infos de groupe */
    private static class GroupeInfo {
        final Long id;
        final String code;
        final int capaciteActuelle;
        final int capaciteMin;
        final int capaciteMax;

        GroupeInfo(Long id, String code, int capaciteActuelle, int capaciteMin, int capaciteMax) {
            this.id = id;
            this.code = code;
            this.capaciteActuelle = capaciteActuelle;
            this.capaciteMin = capaciteMin;
            this.capaciteMax = capaciteMax;
        }
    }
}
