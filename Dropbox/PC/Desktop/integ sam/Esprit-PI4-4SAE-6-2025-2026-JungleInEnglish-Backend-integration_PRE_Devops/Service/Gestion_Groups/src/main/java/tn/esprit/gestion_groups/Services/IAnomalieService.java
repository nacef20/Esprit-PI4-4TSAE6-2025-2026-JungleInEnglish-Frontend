package tn.esprit.gestion_groups.Services;

import tn.esprit.gestion_groups.dto.AlerteGroupeDTO;

import java.util.List;

public interface IAnomalieService {

    /** Scan complet — retourne toutes les anomalies triées par sévérité */
    List<AlerteGroupeDTO> detecterToutesAnomalies();

    /** Anomalies d'un type spécifique */
    List<AlerteGroupeDTO> detecterAnomaliesParType(String typeAnomalie);

    /** Anomalies d'une sévérité spécifique */
    List<AlerteGroupeDTO> detecterAnomaliesParSeverite(String severite);
}
