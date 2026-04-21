package tn.esprit.gestion_groups.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_groups.dto.ParcoursEtudiantDTO;
import tn.esprit.gestion_groups.dto.RetentionGroupeDTO;
import tn.esprit.gestion_groups.Services.IAuditTrailService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/audit")
public class AuditTrailController {

    private final IAuditTrailService auditTrailService;

    /**
     * GET /audit/parcours/{etudiantId}
     * Timeline complète d'un étudiant à travers les groupes.
     */
    @GetMapping("/parcours/{etudiantId}")
    public List<ParcoursEtudiantDTO> getParcoursEtudiant(@PathVariable Long etudiantId) {
        return auditTrailService.getParcoursEtudiant(etudiantId);
    }

    /**
     * GET /audit/retention?seuil=1
     * Statistiques de rétention par groupe.
     * @param seuil nombre minimum d'affectations pour inclure un groupe (défaut: 1)
     */
    @GetMapping("/retention")
    public List<RetentionGroupeDTO> getStatistiquesRetention(
            @RequestParam(defaultValue = "1") long seuil) {
        return auditTrailService.getStatistiquesRetention(seuil);
    }
}
