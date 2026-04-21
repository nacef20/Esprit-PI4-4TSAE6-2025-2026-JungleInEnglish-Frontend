package tn.esprit.quiz_microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.quiz_microservice.dto.AiQuizRequest;
import tn.esprit.quiz_microservice.entities.Quiz;
import tn.esprit.quiz_microservice.service.AiQuizGeneratorService;

import java.util.Map;

@RestController
@RequestMapping("/api/quizzes/ai")

@RequiredArgsConstructor
public class AiQuizController {

    private final AiQuizGeneratorService aiQuizGeneratorService;
    private final ObjectMapper objectMapper;

    /**
     * Generate a quiz from AI using a file (PDF/image) and/or a text prompt.
     *
     * Accepts multipart/form-data:
     * - file (optional): PDF or image (jpg, png)
     * - request (required): JSON string with prompt, numberOfQuestions, difficulty, teacherId, courseId
     */
    @PostMapping(value = "/generate", consumes = "multipart/form-data")
    public ResponseEntity<?> generateQuiz(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("request") String requestJson) {
        try {
            AiQuizRequest request = objectMapper.readValue(requestJson, AiQuizRequest.class);

            // Validate: at least a file or a prompt must be provided
            boolean hasFile = file != null && !file.isEmpty();
            boolean hasPrompt = request.getPrompt() != null && !request.getPrompt().isBlank();
            if (!hasFile && !hasPrompt) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", 400,
                        "error", "You must provide either a file (PDF/image) or a text prompt."
                ));
            }

            // Validate file type if present
            if (hasFile) {
                String contentType = file.getContentType();
                if (contentType == null ||
                        (!contentType.equals("application/pdf")
                                && !contentType.startsWith("image/"))) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "status", 400,
                            "error", "Only PDF and image files (JPG, PNG) are accepted."
                    ));
                }
                // Limit file size to 10 MB
                if (file.getSize() > 10 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "status", 400,
                            "error", "File size must not exceed 10 MB."
                    ));
                }
            }

            Quiz quiz = aiQuizGeneratorService.generateQuizFromAi(file, request);

            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", 500,
                    "error", "AI quiz generation failed: " + e.getMessage()
            ));
        }
    }
}
