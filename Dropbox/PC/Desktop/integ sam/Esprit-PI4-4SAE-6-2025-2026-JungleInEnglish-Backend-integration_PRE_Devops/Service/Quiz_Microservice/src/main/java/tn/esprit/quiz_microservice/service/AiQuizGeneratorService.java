package tn.esprit.quiz_microservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.quiz_microservice.dto.AiQuizRequest;
import tn.esprit.quiz_microservice.dto.AiQuizResponse;
import tn.esprit.quiz_microservice.entities.*;
import tn.esprit.quiz_microservice.repositories.ChoiceRepository;
import tn.esprit.quiz_microservice.repositories.QuestionRepository;
import tn.esprit.quiz_microservice.repositories.QuizRepository;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiQuizGeneratorService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.model:gemini-2.0-flash}")
    private String geminiModel;

    /**
     * Generate a complete quiz from a file (PDF or image) and/or a text prompt.
     */
    public Quiz generateQuizFromAi(MultipartFile file, AiQuizRequest request) throws IOException {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new RuntimeException("Gemini API key is not configured. Set 'gemini.api.key' in application.yml. "
                    + "Get a free key at https://aistudio.google.com/app/apikey");
        }

        // 1. Build the request body for Gemini
        Map<String, Object> geminiRequestBody = buildGeminiRequest(file, request);

        // 2. Call Gemini API
        String aiJsonResponse = callGemini(geminiRequestBody);

        // 3. Parse AI response into DTO
        AiQuizResponse quizData = parseAiResponse(aiJsonResponse);

        // 4. Save Quiz + Questions + Choices to database
        return saveQuizFromAiResponse(quizData, request);
    }

    // ═══════════════════════════════════════════════════════════════
    //  BUILD REQUEST FOR GEMINI
    // ═══════════════════════════════════════════════════════════════

    private Map<String, Object> buildGeminiRequest(MultipartFile file, AiQuizRequest request) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();

        // System instruction (replaces OpenAI "system" role)
        String systemPrompt = buildSystemPrompt(request);
        requestBody.put("systemInstruction", Map.of(
                "parts", List.of(Map.of("text", systemPrompt))
        ));

        // User content parts
        List<Map<String, Object>> parts = new ArrayList<>();

        // Add text prompt
        String userText = buildUserPrompt(file, request);
        parts.add(Map.of("text", userText));

        // Add image inline if file is an image
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
                String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                parts.add(Map.of(
                        "inlineData", Map.of(
                                "mimeType", contentType,
                                "data", base64
                        )
                ));
            }
        }

        requestBody.put("contents", List.of(Map.of("parts", parts)));

        // Generation config
        requestBody.put("generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 8192
        ));

        return requestBody;
    }

    private String buildSystemPrompt(AiQuizRequest request) {
        return """
                You are an expert English language teacher and quiz creator for the "Jungle In English" platform.
                Your job is to create high-quality multiple-choice quizzes to help students learn English.
                
                RULES:
                - Generate exactly %d questions
                - Difficulty level: %s
                - Each question MUST have exactly 4 choices
                - Exactly ONE choice per question must be correct (isCorrect: true)
                - The other 3 choices must be plausible but incorrect (isCorrect: false)
                - Points per question: easy=5, medium=10, hard=20
                - Quiz duration: calculate a reasonable time in minutes based on difficulty and number of questions
                - All content must be in English
                - Questions should be clear, educational, and well-formulated
                - Title should be concise and descriptive (3-100 chars)
                - Description should summarize the quiz topic (max 500 chars)
                
                You MUST respond with ONLY valid JSON in this exact format (no markdown, no extra text):
                {
                  "title": "Quiz Title Here",
                  "description": "Brief description of the quiz topic",
                  "duration": 10,
                  "questions": [
                    {
                      "content": "Question text here?",
                      "points": 10,
                      "choices": [
                        {"content": "Choice A", "isCorrect": false},
                        {"content": "Choice B", "isCorrect": true},
                        {"content": "Choice C", "isCorrect": false},
                        {"content": "Choice D", "isCorrect": false}
                      ]
                    }
                  ]
                }
                """.formatted(request.getNumberOfQuestions(), request.getDifficulty());
    }

    private String buildUserPrompt(MultipartFile file, AiQuizRequest request) throws IOException {
        StringBuilder prompt = new StringBuilder();

        // Extract text from PDF if applicable
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType != null && contentType.equals("application/pdf")) {
                String pdfText = extractTextFromPdf(file);
                prompt.append("Here is the content from a PDF document. Create a quiz based on this content:\n\n");
                prompt.append("--- PDF CONTENT START ---\n");
                // Limit to ~4000 chars to stay within token limits
                if (pdfText.length() > 4000) {
                    prompt.append(pdfText, 0, 4000);
                    prompt.append("\n... (content truncated)");
                } else {
                    prompt.append(pdfText);
                }
                prompt.append("\n--- PDF CONTENT END ---\n\n");
            } else if (contentType != null && contentType.startsWith("image/")) {
                prompt.append("I'm uploading an image. Create a quiz based on the content visible in this image.\n\n");
            }
        }

        // Add user's custom prompt
        if (request.getPrompt() != null && !request.getPrompt().isBlank()) {
            prompt.append("Additional instructions: ").append(request.getPrompt());
        } else if (prompt.isEmpty()) {
            prompt.append("Create a general English language quiz covering vocabulary, grammar, and reading comprehension.");
        }

        return prompt.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    //  PDF TEXT EXTRACTION
    // ═══════════════════════════════════════════════════════════════

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  GEMINI API CALL
    // ═══════════════════════════════════════════════════════════════

    private String callGemini(Map<String, Object> geminiRequestBody) {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel + ":generateContent?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(geminiRequestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Extract text from Gemini response: candidates[0].content.parts
                // Gemini 2.5 may include "thought" parts — skip them
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    String content = null;
                    if (parts.isArray()) {
                        for (JsonNode part : parts) {
                            // Skip thinking parts (thought: true)
                            if (part.has("thought") && part.path("thought").asBoolean()) {
                                continue;
                            }
                            if (part.has("text")) {
                                content = part.path("text").asText();
                            }
                        }
                    }
                    if (content != null && !content.isBlank()) {
                        log.info("Gemini response received, length: {}", content.length());
                        return content;
                    }
                }
                throw new RuntimeException("Gemini returned an empty response");
            }
            throw new RuntimeException("Gemini API returned status: " + response.getStatusCode());
        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            log.error("Gemini API client error {}: {}", e.getStatusCode(), body);
            if (e.getStatusCode().value() == 429) {
                throw new RuntimeException("Gemini API rate limit exceeded. Free tier allows 15 requests/minute. Please wait and try again.");
            } else if (e.getStatusCode().value() == 400) {
                throw new RuntimeException("Gemini API bad request. Check your API key and request format. Details: " + body);
            } else if (e.getStatusCode().value() == 403) {
                throw new RuntimeException("Gemini API key is invalid or the API is not enabled. "
                        + "Get a free key at https://aistudio.google.com/app/apikey");
            }
            throw new RuntimeException("Gemini API error (" + e.getStatusCode().value() + "): " + body, e);
        } catch (HttpServerErrorException e) {
            log.error("Gemini API server error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini server error. Please try again later.", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  PARSE AI RESPONSE
    // ═══════════════════════════════════════════════════════════════

    private AiQuizResponse parseAiResponse(String aiResponse) {
        try {
            // Clean the response — remove markdown code fences if present
            String cleaned = aiResponse.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            AiQuizResponse response = objectMapper.readValue(cleaned, AiQuizResponse.class);

            // Validate response
            if (response.getTitle() == null || response.getTitle().isBlank()) {
                throw new RuntimeException("AI generated quiz has no title");
            }
            if (response.getQuestions() == null || response.getQuestions().isEmpty()) {
                throw new RuntimeException("AI generated quiz has no questions");
            }
            for (AiQuizResponse.AiQuestion q : response.getQuestions()) {
                if (q.getChoices() == null || q.getChoices().size() < 2) {
                    throw new RuntimeException("Question '" + q.getContent() + "' has fewer than 2 choices");
                }
                long correctCount = q.getChoices().stream().filter(AiQuizResponse.AiChoice::isCorrect).count();
                if (correctCount != 1) {
                    throw new RuntimeException("Question '" + q.getContent() + "' must have exactly 1 correct choice, found " + correctCount);
                }
            }

            return response;
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            log.debug("Raw AI response: {}", aiResponse);
            throw new RuntimeException("Failed to parse AI-generated quiz: " + e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SAVE TO DATABASE
    // ═══════════════════════════════════════════════════════════════

    private Quiz saveQuizFromAiResponse(AiQuizResponse aiResponse, AiQuizRequest request) {
        // Create Quiz entity
        Quiz quiz = new Quiz();
        quiz.setTitle(aiResponse.getTitle());
        quiz.setDescription(aiResponse.getDescription());
        quiz.setDuration(aiResponse.getDuration());
        quiz.setStatus(QuizStatus.DRAFT);
        quiz.setTeacherId(request.getTeacherId());
        quiz.setCourseId(request.getCourseId());

        quiz = quizRepository.save(quiz);

        // Create Questions and Choices
        for (AiQuizResponse.AiQuestion aiQuestion : aiResponse.getQuestions()) {
            Question question = new Question();
            question.setContent(aiQuestion.getContent());
            question.setPoints(aiQuestion.getPoints());
            question.setQuiz(quiz);
            question = questionRepository.save(question);

            for (AiQuizResponse.AiChoice aiChoice : aiQuestion.getChoices()) {
                Choice choice = new Choice();
                choice.setContent(aiChoice.getContent());
                choice.setIsCorrect(aiChoice.isCorrect());
                choice.setQuestion(question);
                choiceRepository.save(choice);
            }
        }

        log.info("AI quiz created — ID: {}, Title: '{}', Questions: {}",
                quiz.getId(), quiz.getTitle(), aiResponse.getQuestions().size());

        return quiz;
    }
}
