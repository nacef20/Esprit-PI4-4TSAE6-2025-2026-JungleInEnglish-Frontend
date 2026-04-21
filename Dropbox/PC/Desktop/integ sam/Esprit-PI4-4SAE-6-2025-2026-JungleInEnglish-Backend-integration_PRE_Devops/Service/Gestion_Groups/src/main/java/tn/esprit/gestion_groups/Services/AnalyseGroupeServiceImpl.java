package tn.esprit.gestion_groups.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_groups.clients.PlanningFeignClient;
import tn.esprit.gestion_groups.dto.ChargePlanningGroupeDTO;
import tn.esprit.gestion_groups.dto.GroupeDestinationDTO;
import tn.esprit.gestion_groups.dto.ImpactTransfertDTO;
import tn.esprit.gestion_groups.dto.RecommandationGroupeDTO;
import tn.esprit.gestion_groups.dto.ScoreSanteGroupeDTO;
import tn.esprit.gestion_groups.Entites.*;
import tn.esprit.gestion_groups.Repositories.GroupeRepository;
import tn.esprit.gestion_groups.Repositories.HistoriqueGroupeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AnalyseGroupeServiceImpl implements IAnalyseGroupeService {

    private final HistoriqueGroupeRepository historiqueRepository;
    private final GroupeRepository groupeRepository;
        private final GroupeHealthScoreService groupeHealthScoreService;
        private final PlanningFeignClient planningFeignClient;

    // Groupes non pertinents pour l'analyse
    private static final List<StatutGroupe> STATUTS_EXCLUS =
            Arrays.asList(StatutGroupe.ANNULE, StatutGroupe.TERMINE);
        private static final double SEUIL_HEURES_GROUPE_PAR_SEMAINE = 35.0;


    @Override
    public List<ImpactTransfertDTO> analyserImpactTransferts(
            InitiateurChangement initiePar,
            LocalDateTime dateDebut,
            LocalDateTime dateFin) {

        normaliserScoresHistoriqueManquants();

        List<Object[]> rawData = historiqueRepository
                .analyserImpactTransfertsParRaison(initiePar, dateDebut, dateFin);

        return rawData.stream()
                .map(this::mapToImpactTransfertDTO)
                .collect(Collectors.toList());
    }

    private ImpactTransfertDTO mapToImpactTransfertDTO(Object[] row) {
        return new ImpactTransfertDTO(
                (Long)                  row[0],   // groupePrecedentId
                (RaisonChangement)      row[1],   // raisonChangement
                (InitiateurChangement)  row[2],   // initiePar
                ((Number)               row[3]).longValue(),    // nombreTransferts
                row[4] != null ? ((Number) row[4]).doubleValue() : 0.0, // impactMoyen
                ((Number)               row[5]).longValue(),    // ameliorations
                ((Number)               row[6]).longValue(),    // degradations
                ((Number)               row[7]).longValue()     // neutres
        );
    }

    @Override
    public List<GroupeDestinationDTO> getGroupesDestinationMeilleureAmelioration(
            LocalDateTime dateDebut,
            LocalDateTime dateFin) {

        normaliserScoresHistoriqueManquants();

        List<Object[]> rawData = historiqueRepository
                .getGroupesDestinationMeilleureAmelioration(dateDebut, dateFin);

        return rawData.stream()
                .map(this::mapToGroupeDestinationDTO)
                .collect(Collectors.toList());
    }

    private GroupeDestinationDTO mapToGroupeDestinationDTO(Object[] row) {
        return new GroupeDestinationDTO(
                (Long)   row[0],   // idGroupe
                (String) row[1],   // nomGroupe
                ((Number) row[2]).longValue(),                              // nombreEtudiantsRecus
                row[3] != null ? ((Number) row[3]).doubleValue() : 0.0,    // ameliorationMoyenne
                row[4] != null ? ((Number) row[4]).intValue()   : 0,       // ameliorationMax
                row[5] != null ? ((Number) row[5]).intValue()   : 0        // ameliorationMin
        );
    }


    @Override
    public List<ScoreSanteGroupeDTO> calculerScoresSante(int joursReference) {

        LocalDateTime dateReference = LocalDateTime.now().minusDays(joursReference);

        List<Object[]> rawData = groupeRepository.getRawDataPourScoreSante(
                StatutObjectif.ATTEINT,
                dateReference,
                STATUTS_EXCLUS
        );

        return rawData.stream()
                .map(this::buildScoreSante)
                .sorted(Comparator.comparingDouble(ScoreSanteGroupeDTO::getScoreTotal).reversed())
                .collect(Collectors.toList());
    }

    private ScoreSanteGroupeDTO buildScoreSante(Object[] row) {

        Groupe g                           = (Groupe) row[0];
        long   objectifsAtteints           = ((Number) row[1]).longValue();
        long   totalObjectifs              = ((Number) row[2]).longValue();
        long   reglementsObligatoiresActifs= ((Number) row[3]).longValue();
        long   totalReglementsObligatoires = ((Number) row[4]).longValue();
        long   changementsRecents          = ((Number) row[5]).longValue();

        // ── Composante 1 : Taux de remplissage (0-25 pts) ─────────────
        // Un groupe plein ou bien rempli est signe de bonne gestion
        double scoreTaux = (g.getCapaciteMax() != null && g.getCapaciteMax() > 0)
                ? Math.min(25.0, ((double) g.getCapaciteActuelle() / g.getCapaciteMax()) * 25)
                : 0.0;

        // ── Composante 2 : Objectifs pédagogiques atteints (0-25 pts) ──
        // Neutre à 12.5 si aucun objectif défini (pas pénalisé)
        double scoreObj = totalObjectifs > 0
                ? ((double) objectifsAtteints / totalObjectifs) * 25
                : 12.5;

        // ── Composante 3 : Conformité règlements obligatoires (0-25 pts)
        // Neutre à 12.5 si aucun règlement défini
        double scoreReg = totalReglementsObligatoires > 0
                ? ((double) reglementsObligatoiresActifs / totalReglementsObligatoires) * 25
                : 12.5;


        double scoreStab = Math.max(0.0, 25.0 - (changementsRecents * 5.0));

        double scoreTotal = scoreTaux + scoreObj + scoreReg + scoreStab;

        return new ScoreSanteGroupeDTO(
                g.getIdGroupe(), g.getNom(), g.getCodeGroupe(),
                scoreTaux, scoreObj, scoreReg, scoreStab, scoreTotal
        );
    }

    @Override
    public List<ScoreSanteGroupeDTO> getGroupesParNiveauSante(String niveauSante, int joursReference) {
        return calculerScoresSante(joursReference).stream()
                .filter(dto -> dto.getNiveauSante().equalsIgnoreCase(niveauSante))
                .collect(Collectors.toList());
    }



    @Override
    public List<RecommandationGroupeDTO> getToutesRecommandations() {

        List<Object[]> rawData = groupeRepository.getGroupesCandidatsFusionScission(
                StatutObjectif.EN_COURS,
                STATUTS_EXCLUS
        );

        return rawData.stream()
                .map(this::buildRecommandation)
                .collect(Collectors.toList());
    }

    private RecommandationGroupeDTO buildRecommandation(Object[] row) {

        Groupe g              = (Groupe) row[0];
        long   objectifsEnCours = ((Number) row[1]).longValue();
        long   chargeHoraire  = row[2] != null ? ((Number) row[2]).longValue() : 0L;

        double tauxRemplissage = (g.getCapaciteMax() != null && g.getCapaciteMax() > 0)
                ? ((double) g.getCapaciteActuelle() / g.getCapaciteMax()) * 100
                : 0.0;

        boolean candidatFusion = g.getCapaciteActuelle() < g.getCapaciteMin();

        String typeRec;
        String raisonRec;

        if (candidatFusion) {
            typeRec = "FUSION";
            raisonRec = String.format(
                    "Effectif actuel (%d) inférieur au minimum requis (%d). " +
                    "Taux de remplissage : %.1f%%. " +
                    "Charge pédagogique active : %d objectif(s) en cours — %d h estimées. " +
                    "Recommandation : fusionner avec un groupe de même niveau.",
                    g.getCapaciteActuelle(), g.getCapaciteMin(),
                    tauxRemplissage, objectifsEnCours, chargeHoraire
            );
        } else {
            typeRec = "SCISSION";
            raisonRec = String.format(
                    "Effectif actuel (%d) ≥ 90%% de la capacité maximum (%d). " +
                    "Taux de remplissage : %.1f%%. " +
                    "Charge pédagogique active : %d objectif(s) en cours — %d h estimées. " +
                    "Recommandation : scinder en deux groupes pour réduire la charge.",
                    g.getCapaciteActuelle(), g.getCapaciteMax(),
                    tauxRemplissage, objectifsEnCours, chargeHoraire
            );
        }

        return new RecommandationGroupeDTO(
                g.getIdGroupe(), g.getNom(), g.getCodeGroupe(),
                g.getCapaciteActuelle(), g.getCapaciteMin(), g.getCapaciteMax(),
                Math.round(tauxRemplissage * 10.0) / 10.0,
                objectifsEnCours, chargeHoraire,
                typeRec, raisonRec
        );
    }

    @Override
    public List<RecommandationGroupeDTO> getRecommandationsFusion() {
        return getToutesRecommandations().stream()
                .filter(dto -> "FUSION".equals(dto.getTypeRecommandation()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommandationGroupeDTO> getRecommandationsScission() {
        return getToutesRecommandations().stream()
                .filter(dto -> "SCISSION".equals(dto.getTypeRecommandation()))
                .collect(Collectors.toList());
    }

        @Override
        public ChargePlanningGroupeDTO getChargePlanningGroupe(Long groupeId, LocalDate dateReference) {
                Groupe groupe = groupeRepository.findById(groupeId)
                                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable : " + groupeId));

                PlanningFeignClient.WeeklyLoadReportDTO weeklyLoad = planningFeignClient.getWeeklyLoad(dateReference.toString());
                Map<Long, Double> heuresParGroupe = weeklyLoad.getHeuresParGroupe() != null
                                ? weeklyLoad.getHeuresParGroupe()
                                : new LinkedHashMap<>();

                double heuresPlanifiees = heuresParGroupe.getOrDefault(groupeId, 0.0);
                double tauxCharge = SEUIL_HEURES_GROUPE_PAR_SEMAINE > 0
                                ? (heuresPlanifiees / SEUIL_HEURES_GROUPE_PAR_SEMAINE) * 100.0
                                : 0.0;
                boolean surcharge = heuresPlanifiees > SEUIL_HEURES_GROUPE_PAR_SEMAINE;

                String niveauCharge;
                if (heuresPlanifiees == 0.0) {
                        niveauCharge = "AUCUNE";
                } else if (tauxCharge < 60.0) {
                        niveauCharge = "MODEREE";
                } else if (tauxCharge < 100.0) {
                        niveauCharge = "ELEVEE";
                } else {
                        niveauCharge = "CRITIQUE";
                }

                String message = surcharge
                                ? String.format("Le groupe %s dépasse le seuil de planning hebdomadaire avec %.2f h planifiées.", groupe.getCodeGroupe(), heuresPlanifiees)
                                : String.format("Le groupe %s reste sous le seuil hebdomadaire avec %.2f h planifiées.", groupe.getCodeGroupe(), heuresPlanifiees);

                return ChargePlanningGroupeDTO.builder()
                                .idGroupe(groupe.getIdGroupe())
                                .codeGroupe(groupe.getCodeGroupe())
                                .nomGroupe(groupe.getNom())
                                .weekNumber(weeklyLoad.getWeekNumber())
                                .year(weeklyLoad.getYear())
                                .heuresPlanifiees(round(heuresPlanifiees))
                                .seuilAlerteHeures(SEUIL_HEURES_GROUPE_PAR_SEMAINE)
                                .tauxCharge(round(tauxCharge))
                                .surcharge(surcharge)
                                .niveauCharge(niveauCharge)
                                .message(message)
                                .build();
        }



    @Override
    public tn.esprit.gestion_groups.dto.DashboardStatsDTO getGlobalStats() {
                List<Object[]> rows = groupeRepository.getStatistiquesGlobales(
                STATUTS_EXCLUS,
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0)
        );

                if (rows == null || rows.isEmpty()) {
                        return tn.esprit.gestion_groups.dto.DashboardStatsDTO.builder()
                                        .totalGroupesActifs(0)
                                        .totalEtudiantsAffectes(0)
                                        .totalFormateursActifs(0)
                                        .tauxRemplissageMoyen(0)
                                        .groupesSousRemplis(0)
                                        .groupesQuasiPleins(0)
                                        .transfertsCeMois(0)
                                        .objectifsEnCours(0)
                                        .ratioEtudiantsParFormateur(0)
                                        .tendance("STABLE")
                                        .build();
                }

                Object[] raw = rows.get(0);
                if (raw.length == 1 && raw[0] instanceof Object[] nested) {
                        raw = nested;
                }

        long totalGroupesActifs = ((Number) raw[0]).longValue();
        long totalEtudiants = ((Number) raw[1]).longValue();
        long totalFormateurs = ((Number) raw[2]).longValue();
        double tauxMoyen = raw[3] != null ? ((Number) raw[3]).doubleValue() : 0.0;
        long groupesSousRemplis = ((Number) raw[4]).longValue();
        long groupesQuasiPleins = ((Number) raw[5]).longValue();
        long transfertsMois = ((Number) raw[6]).longValue();
        long objectifsEnCours = ((Number) raw[7]).longValue();

        double ratio = totalFormateurs > 0 ? (double) totalEtudiants / totalFormateurs : 0.0;
        
        // Tendance arbitraire pour l'exemple (on pourrait comparer avec le mois dernier si on avait la donnée)
        String tendance = totalEtudiants > 50 ? "CROISSANCE" : "STABLE";

        return tn.esprit.gestion_groups.dto.DashboardStatsDTO.builder()
                .totalGroupesActifs(totalGroupesActifs)
                .totalEtudiantsAffectes(totalEtudiants)
                .totalFormateursActifs(totalFormateurs)
                .tauxRemplissageMoyen(Math.round(tauxMoyen * 10.0) / 10.0)
                .groupesSousRemplis(groupesSousRemplis)
                .groupesQuasiPleins(groupesQuasiPleins)
                .transfertsCeMois(transfertsMois)
                .objectifsEnCours(objectifsEnCours)
                .ratioEtudiantsParFormateur(Math.round(ratio * 10.0) / 10.0)
                .tendance(tendance)
                .build();
    }

        private double round(double value) {
                return Math.round(value * 100.0) / 100.0;
        }

        private void normaliserScoresHistoriqueManquants() {
                historiqueRepository.findAll().forEach(historique -> {
                        boolean modifie = false;

                        if (historique.getScoreAvant() == null && historique.getGroupePrecedentId() != null) {
                                groupeRepository.findById(historique.getGroupePrecedentId())
                                                .ifPresent(groupe -> {
                                                        Integer score = groupeHealthScoreService.calculerScoreTotal(groupe);
                                                        historique.setScoreAvant(score);
                                                        historiqueRepository.save(historique);
                                                });
                                modifie = true;
                        }

                        if (historique.getScoreApres() == null && historique.getGroupe() != null) {
                                Integer score = groupeHealthScoreService.calculerScoreTotal(historique.getGroupe());
                                historique.setScoreApres(score);
                                historiqueRepository.save(historique);
                        }
                });
        }
}
