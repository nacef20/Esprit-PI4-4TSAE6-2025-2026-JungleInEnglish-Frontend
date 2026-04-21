package tn.esprit.gestion_groups.Services;

import tn.esprit.gestion_groups.dto.GroupeDestinationDTO;
import tn.esprit.gestion_groups.dto.ChargePlanningGroupeDTO;
import tn.esprit.gestion_groups.dto.ImpactTransfertDTO;
import tn.esprit.gestion_groups.dto.RecommandationGroupeDTO;
import tn.esprit.gestion_groups.dto.ScoreSanteGroupeDTO;
import tn.esprit.gestion_groups.Entites.InitiateurChangement;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

public interface IAnalyseGroupeService {

    // ── Idée 2 : Impact des transferts ──────────────────────────

    /**
     * Analyse les groupes sources qui perdent des étudiants.
     * Calcule l'impact sur les scores par raison de changement.
     *
     * @param initiePar  filtre optionnel (SYSTEME, ADMIN, ETUDIANT, PROFESSEUR)
     * @param dateDebut  début de la période d'analyse
     * @param dateFin    fin de la période d'analyse
     */
    List<ImpactTransfertDTO> analyserImpactTransferts(
            InitiateurChangement initiePar,
            LocalDateTime dateDebut,
            LocalDateTime dateFin);

    /**
     * Classe les groupes destination par leur capacité réelle
     * à améliorer les scores des étudiants transférés.
     */
    List<GroupeDestinationDTO> getGroupesDestinationMeilleureAmelioration(
            LocalDateTime dateDebut,
            LocalDateTime dateFin);

    // ── Idée 3 : Score de santé composite ───────────────────────

    /**
     * Calcule un score de santé sur 100 pts pour chaque groupe actif.
     * Composantes : taux remplissage + objectifs + règlements + stabilité.
     *
     * @param joursReference fenêtre de temps pour mesurer la stabilité (ex: 30 jours)
     */
    List<ScoreSanteGroupeDTO> calculerScoresSante(int joursReference);

    /**
     * Filtre les résultats par niveau : CRITIQUE, FAIBLE, MOYEN, BON
     */
    List<ScoreSanteGroupeDTO> getGroupesParNiveauSante(String niveauSante, int joursReference);

    // ── Idée 5 : Recommandations fusion / scission ───────────────

    /** Retourne tous les candidats (fusion + scission confondus) */
    List<RecommandationGroupeDTO> getToutesRecommandations();

    /** Retourne uniquement les groupes sous-remplis à fusionner */
    List<RecommandationGroupeDTO> getRecommandationsFusion();

    /** Retourne uniquement les groupes saturés à scinder */
    List<RecommandationGroupeDTO> getRecommandationsScission();

        /**
         * Analyse la charge de planning d'un groupe précis pour une date donnée.
         */
        ChargePlanningGroupeDTO getChargePlanningGroupe(Long groupeId, LocalDate dateReference);

    // ── Idée 2 : Statistiques Globales ──────────────────────────
    /**
     * Retourne les statistiques globales agrégées pour le dashboard admin.
     */
    tn.esprit.gestion_groups.dto.DashboardStatsDTO getGlobalStats();
}
