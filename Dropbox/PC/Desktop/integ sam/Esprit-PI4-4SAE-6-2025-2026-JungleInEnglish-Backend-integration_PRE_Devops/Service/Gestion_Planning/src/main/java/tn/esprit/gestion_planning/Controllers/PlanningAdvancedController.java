// PlanningAdvancedController.java
package tn.esprit.gestion_planning.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestion_planning.DTO.*;
import tn.esprit.gestion_planning.Services.IPlanningAdvancedService;
import tn.esprit.gestion_planning.Services.PlanningPdfService;

import java.time.LocalDate;

@RestController
@AllArgsConstructor
@RequestMapping("/plannings/advanced")
public class PlanningAdvancedController {

    private final IPlanningAdvancedService planningAdvancedService;


    @PostMapping("/generate")
    public GenerationResult generateWeeklyPlanning(
            @RequestBody PlanningGeneratorRequest request) {
        return planningAdvancedService.generateWeeklyPlanning(request);
    }


    @GetMapping("/weekly-load")
    public WeeklyLoadReport analyzeWeeklyLoad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return planningAdvancedService.analyzeWeeklyLoad(date);
    }

    @PostMapping("/replicate-semester")
    public SemesterReplicationResult replicatePlanningSemester(
            @RequestBody SemesterReplicationRequest request) {
        return planningAdvancedService.replicatePlanningSemester(request);
    }

    // Ajouter dans PlanningAdvancedController.java

    @Autowired
    private PlanningPdfService planningPdfService;

    /**
     * GET /plannings/advanced/pdf?date=2025-09-15
     * Retourne le PDF de l'emploi du temps de la semaine
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadEmploiDuTempsPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            byte[] pdf = planningPdfService.generateEmploiDuTempsPdf(date);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "emploi_du_temps_" + date + ".pdf");

            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}