package tn.esprit.quiz_microservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.quiz_microservice.dto.StudentStatsDTO;
import tn.esprit.quiz_microservice.dto.SubmitQuizRequest;
import tn.esprit.quiz_microservice.entities.Attempt;
import tn.esprit.quiz_microservice.service.AttemptExcelService;
import tn.esprit.quiz_microservice.service.AttemptPdfService;
import tn.esprit.quiz_microservice.service.IAttemptService;
import tn.esprit.quiz_microservice.service.QuizAttemptService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attempts")

@RequiredArgsConstructor
public class AttemptController {

    private final IAttemptService attemptService;
    private final AttemptPdfService attemptPdfService;
    private final AttemptExcelService attemptExcelService;
    private final QuizAttemptService quizAttemptService;

    @PostMapping
    public Attempt save(@RequestBody Attempt attempt) {
        return attemptService.save(attempt);
    }

    // ─── QUIZ ATTEMPT FLOW (server-side timer + scoring) ───

    @PostMapping("/start")
    public ResponseEntity<?> startAttempt(
            @RequestParam Long quizId,
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "Student") String studentName) {
        try {
            Attempt attempt = quizAttemptService.startAttempt(quizId, studentId, studentName);
            Map<String, Object> result = new HashMap<>();
            result.put("attemptId", attempt.getId());
            result.put("startedAt", attempt.getStartedAt().toString());
            result.put("remainingSeconds", quizAttemptService.getRemainingTime(attempt.getId()));
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitAttempt(
            @PathVariable Long id,
            @RequestBody SubmitQuizRequest request) {
        try {
            Map<String, Object> result = quizAttemptService.submitAttempt(id, request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{id}/remaining-time")
    public ResponseEntity<Map<String, Object>> getRemainingTime(@PathVariable Long id) {
        try {
            long remaining = quizAttemptService.getRemainingTime(id);
            Map<String, Object> result = new HashMap<>();
            result.put("remainingSeconds", remaining);
            result.put("expired", remaining <= 0);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("remainingSeconds", 0);
            error.put("expired", true);
            return ResponseEntity.ok(error);
        }
    }

    @GetMapping
    public List<Attempt> findAll() {
        return attemptService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attempt> findById(@PathVariable Long id) {
        return attemptService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public Attempt update(@RequestBody Attempt attempt) {
        return attemptService.update(attempt);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        attemptService.deleteById(id);
    }

    @GetMapping("/count")
    public long countByQuizAndStudent(@RequestParam Long quizId, @RequestParam Long studentId) {
        return attemptService.countByQuizIdAndStudentId(quizId, studentId);
    }

    @GetMapping("/page")
    public Page<Attempt> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) Long quizId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String quizTitle,
            @RequestParam(required = false) String studentName) {
        Sort sort = Sort.by("submittedAt").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        boolean hasTitle = quizTitle != null && !quizTitle.isEmpty();
        boolean hasName = studentName != null && !studentName.isEmpty();

        if (hasTitle && hasName) {
            return attemptService.findByQuizTitleAndStudentNamePaginated(quizTitle, studentName, pageRequest);
        } else if (hasTitle && studentId != null) {
            return attemptService.findByQuizTitleAndStudentIdPaginated(quizTitle, studentId, pageRequest);
        } else if (hasTitle) {
            return attemptService.findByQuizTitlePaginated(quizTitle, pageRequest);
        } else if (hasName && quizId != null) {
            return attemptService.findByStudentNameAndQuizIdPaginated(studentName, quizId, pageRequest);
        } else if (hasName) {
            return attemptService.findByStudentNamePaginated(studentName, pageRequest);
        } else if (quizId != null && studentId != null) {
            return attemptService.findByQuizIdAndStudentIdPaginated(quizId, studentId, pageRequest);
        } else if (quizId != null) {
            return attemptService.findByQuizIdPaginated(quizId, pageRequest);
        } else if (studentId != null) {
            return attemptService.findByStudentIdPaginated(studentId, pageRequest);
        }
        return attemptService.findAllPaginated(pageRequest);
    }

    @GetMapping("/page/student/{studentId}")
    public Page<Attempt> findByStudentPaginated(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return attemptService.findByStudentIdPaginated(
                studentId, PageRequest.of(page, size, Sort.by("submittedAt").descending()));
    }

    @GetMapping("/stats/student/{studentId}")
    public StudentStatsDTO getStudentStats(@PathVariable Long studentId) {
        Object[] raw = attemptService.getStudentStats(studentId);
        long total = raw[0] != null ? ((Number) raw[0]).longValue() : 0;
        double avg = raw[1] != null ? Math.round(((Number) raw[1]).doubleValue()) : 0;
        double best = raw[2] != null ? Math.round(((Number) raw[2]).doubleValue()) : 0;
        double passRate = 0;
        if (total > 0 && raw[3] != null) {
            passRate = Math.round(((Number) raw[3]).doubleValue() * 100.0 / total);
        }
        return new StudentStatsDTO(total, avg, best, passRate);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadAttemptPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = attemptPdfService.generateAttemptPdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "attempt-" + id + "-report.pdf");
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportAttemptsListPdf(
            @RequestParam(required = false) Long quizId,
            @RequestParam(required = false) String quizTitle,
            @RequestParam(required = false) String studentName) {
        try {
            byte[] pdfBytes = attemptPdfService.generateAttemptsListPdf(quizId, quizTitle, studentName);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "attempts-report.pdf");
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportAttemptsListExcel(
            @RequestParam(required = false) Long quizId,
            @RequestParam(required = false) String quizTitle,
            @RequestParam(required = false) String studentName) {
        try {
            byte[] excelBytes = attemptExcelService.generateAttemptsExcel(quizId, quizTitle, studentName);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "attempts-report.xlsx");
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            return ResponseEntity.ok().headers(headers).body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
