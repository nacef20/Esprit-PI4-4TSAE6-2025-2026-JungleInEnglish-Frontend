package tn.esprit.quiz_microservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.quiz_microservice.entities.Certificate;
import tn.esprit.quiz_microservice.service.CertificateService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/certificates")

@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    /**
     * GET /api/certificates/attempt/{attemptId}/status
     * Returns certificate info: { eligible, exists, certificateId, viewUrl }
     */
    @GetMapping("/attempt/{attemptId}/status")
    public ResponseEntity<Map<String, Object>> getCertificateStatus(@PathVariable Long attemptId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean eligible = certificateService.isEligible(attemptId);
            result.put("eligible", eligible);

            Optional<Certificate> existing = certificateService.findByAttemptId(attemptId);
            if (existing.isPresent()) {
                Certificate cert = existing.get();
                result.put("exists", true);
                result.put("certificateId", cert.getId());
                result.put("certificateNumber", cert.getCertificateNumber());
                result.put("grade", cert.getGrade());
                result.put("issuedAt", cert.getIssuedAt().toString());
                result.put("viewUrl", "/api/certificates/" + cert.getId() + "/view");
            } else {
                result.put("exists", false);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("eligible", false);
            result.put("exists", false);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * POST /api/certificates/attempt/{attemptId}/generate
     * Creates the certificate if it doesn't exist, returns metadata + viewUrl.
     */
    @PostMapping("/attempt/{attemptId}/generate")
    public ResponseEntity<Map<String, Object>> generateCertificate(@PathVariable Long attemptId) {
        try {
            Certificate cert = certificateService.getOrCreateCertificate(attemptId);

            Map<String, Object> result = new HashMap<>();
            result.put("certificateId", cert.getId());
            result.put("certificateNumber", cert.getCertificateNumber());
            result.put("studentName", cert.getStudentName());
            result.put("quizTitle", cert.getQuizTitle());
            result.put("percentage", cert.getPercentage());
            result.put("grade", cert.getGrade());
            result.put("issuedAt", cert.getIssuedAt().toString());
            result.put("viewUrl", "/api/certificates/" + cert.getId() + "/view");
            result.put("downloadUrl", "/api/certificates/" + cert.getId() + "/download");

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/certificates/{id}/view
     * Serves the PDF inline (for browser viewing).
     */
    @GetMapping("/{id}/view")
    public ResponseEntity<byte[]> viewCertificate(@PathVariable Long id) {
        try {
            byte[] pdfBytes = certificateService.getCertificatePdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set("Content-Disposition", "inline; filename=certificate-" + id + ".pdf");
            headers.setCacheControl("public, max-age=86400");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/certificates/{id}/download
     * Serves the PDF as a download attachment.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
        try {
            byte[] pdfBytes = certificateService.getCertificatePdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "certificate-" + id + ".pdf");
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
