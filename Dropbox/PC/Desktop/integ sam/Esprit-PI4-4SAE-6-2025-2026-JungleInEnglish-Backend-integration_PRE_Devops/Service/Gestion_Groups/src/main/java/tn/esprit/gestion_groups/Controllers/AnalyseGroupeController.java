package tn.esprit.gestion_groups.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_groups.dto.ChargePlanningGroupeDTO;
import tn.esprit.gestion_groups.dto.GroupeDestinationDTO;
import tn.esprit.gestion_groups.dto.ImpactTransfertDTO;
import tn.esprit.gestion_groups.dto.RecommandationGroupeDTO;
import tn.esprit.gestion_groups.dto.ScoreSanteGroupeDTO;
import tn.esprit.gestion_groups.Entites.InitiateurChangement;
import tn.esprit.gestion_groups.Services.IAnalyseGroupeService;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/analyse")
public class AnalyseGroupeController {

    private final IAnalyseGroupeService analyseService;

    // ================================================================
    // IDÉE 2 — Impact des transferts d'étudiants
    // ================================================================

    /**
     * GET /analyse/transferts/impact?dateDebut=...&dateFin=...&initiePar=ADMIN
     *
     * Retourne pour chaque groupe source + raison :
     *   - nombre de transferts
     *   - impact moyen sur le score
     *   - répartition améliorations / dégradations / neutres
     *
     * Le paramètre initiePar est optionnel.
     *
     * Exemple : /analyse/transferts/impact
     *           ?dateDebut=2024-09-01T00:00:00
     *           &dateFin=2025-06-30T23:59:59
     *           &initiePar=ADMIN
     */
    @GetMapping("/transferts/impact")
    public List<ImpactTransfertDTO> analyserImpactTransferts(
            @RequestParam(required = false) InitiateurChangement initiePar,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {

        return analyseService.analyserImpactTransferts(initiePar, dateDebut, dateFin);
    }

    /**
     * GET /analyse/transferts/meilleurs-groupes-destination
     *
     * Retourne les groupes destination classés par amélioration moyenne
     * des scores des étudiants transférés (uniquement ceux avec impact positif).
     */
    @GetMapping("/transferts/meilleurs-groupes-destination")
    public List<GroupeDestinationDTO> getMeilleursGroupesDestination(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {

        return analyseService.getGroupesDestinationMeilleureAmelioration(dateDebut, dateFin);
    }

    // ================================================================
    // IDÉE 3 — Score de santé composite des groupes
    // ================================================================

    /**
     * GET /analyse/sante/scores?joursReference=30
     *
     * Retourne tous les groupes actifs avec leur score de santé (0-100)
     * décomposé en 4 composantes, classés du plus sain au moins sain.
     *
     * joursReference : fenêtre pour compter les changements récents (défaut 30 jours)
     */
    @GetMapping("/sante/scores")
    public List<ScoreSanteGroupeDTO> getScoresSante(
            @RequestParam(defaultValue = "30") int joursReference) {

        return analyseService.calculerScoresSante(joursReference);
    }

    /**
     * GET /analyse/sante/niveau/CRITIQUE?joursReference=30
     *
     * Filtre les groupes par niveau de santé : CRITIQUE | FAIBLE | MOYEN | BON
     */
    @GetMapping("/sante/niveau/{niveauSante}")
    public List<ScoreSanteGroupeDTO> getGroupesParNiveauSante(
            @PathVariable String niveauSante,
            @RequestParam(defaultValue = "30") int joursReference) {

        return analyseService.getGroupesParNiveauSante(niveauSante, joursReference);
    }

    // ================================================================
    // IDÉE 5 — Recommandations fusion / scission
    // ================================================================

    /**
     * GET /analyse/recommandations
     *
     * Tous les groupes candidats à une restructuration
     * (fusion si sous-remplis, scission si ≥ 90% saturés)
     * avec leur charge pédagogique active.
     */
    @GetMapping("/recommandations")
    public List<RecommandationGroupeDTO> getToutesRecommandations() {
        return analyseService.getToutesRecommandations();
    }

    /**
     * GET /analyse/recommandations/fusion
     *
     * Uniquement les groupes sous-remplis (capaciteActuelle < capaciteMin)
     */
    @GetMapping("/recommandations/fusion")
    public List<RecommandationGroupeDTO> getRecommandationsFusion() {
        return analyseService.getRecommandationsFusion();
    }

    /**
     * GET /analyse/recommandations/scission
     *
     * Uniquement les groupes quasi-saturés (≥ 90% de capaciteMax)
     */
    @GetMapping("/recommandations/scission")
    public List<RecommandationGroupeDTO> getRecommandationsScission() {
        return analyseService.getRecommandationsScission();
    }

    /**
     * GET /analyse/groupes/{groupeId}/planning-charge?date=2026-04-14
     *
     * Vue groupe-centrique: charge hebdomadaire issue du microservice Planning
     * et enrichie avec les informations du groupe local.
     */
    @GetMapping("/groupes/{groupeId}/planning-charge")
    public ChargePlanningGroupeDTO getChargePlanningGroupe(
            @PathVariable Long groupeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return analyseService.getChargePlanningGroupe(groupeId, date);
    }

    /**
     * GET /stats/global
     * Statistiques globales pour le dashboard admin.
     */
    @GetMapping("/stats/global")
    public tn.esprit.gestion_groups.dto.DashboardStatsDTO getGlobalStats() {
        return analyseService.getGlobalStats();
    }
}
