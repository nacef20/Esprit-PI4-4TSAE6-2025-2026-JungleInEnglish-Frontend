package tn.esprit.gestion_groups.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_groups.dto.AlerteGroupeDTO;
import tn.esprit.gestion_groups.Services.IAnomalieService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/alertes")
public class AnomalieController {

    private final IAnomalieService anomalieService;

    /**
     * GET /alertes/scan
     * Scan complet — retourne toutes les anomalies triées par sévérité.
     */
    @GetMapping("/scan")
    public List<AlerteGroupeDTO> scanComplet() {
        return anomalieService.detecterToutesAnomalies();
    }

    /**
     * GET /alertes/scan/resume
     * Résumé : nombre d'anomalies par sévérité.
     */
    @GetMapping("/scan/resume")
    public Map<String, Long> getResume() {
        List<AlerteGroupeDTO> alertes = anomalieService.detecterToutesAnomalies();
        return alertes.stream()
                .collect(Collectors.groupingBy(AlerteGroupeDTO::getSeverite, Collectors.counting()));
    }

    /**
     * GET /alertes/type/{typeAnomalie}
     * Filtre par type : SANS_FORMATEUR, CAPACITE_INCOHERENTE, OBJECTIF_EN_RETARD, SANS_OBJECTIF
     */
    @GetMapping("/type/{typeAnomalie}")
    public List<AlerteGroupeDTO> getParType(@PathVariable String typeAnomalie) {
        return anomalieService.detecterAnomaliesParType(typeAnomalie);
    }

    /**
     * GET /alertes/severite/{severite}
     * Filtre par sévérité : CRITIQUE, WARNING, INFO
     */
    @GetMapping("/severite/{severite}")
    public List<AlerteGroupeDTO> getParSeverite(@PathVariable String severite) {
        return anomalieService.detecterAnomaliesParSeverite(severite);
    }
}
