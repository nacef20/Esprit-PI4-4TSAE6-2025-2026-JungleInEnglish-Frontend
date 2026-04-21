package tn.esprit.gestion_groups.Services;

import tn.esprit.gestion_groups.dto.ParcoursEtudiantDTO;
import tn.esprit.gestion_groups.dto.RetentionGroupeDTO;

import java.util.List;

public interface IAuditTrailService {

    /** Reconstruit la timeline complète d'un étudiant */
    List<ParcoursEtudiantDTO> getParcoursEtudiant(Long etudiantId);

    /** Statistiques de rétention par groupe */
    List<RetentionGroupeDTO> getStatistiquesRetention(long seuilMinimum);
}
