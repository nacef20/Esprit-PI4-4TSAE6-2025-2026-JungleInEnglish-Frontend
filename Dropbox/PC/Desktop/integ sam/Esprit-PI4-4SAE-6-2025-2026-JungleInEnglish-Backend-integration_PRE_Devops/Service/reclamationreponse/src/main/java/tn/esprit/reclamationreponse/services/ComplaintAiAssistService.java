package tn.esprit.reclamationreponse.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.reclamationreponse.dto.ComplaintAiAssistRequest;
import tn.esprit.reclamationreponse.dto.ComplaintAiAssistResponse;
import tn.esprit.reclamationreponse.dto.ComplaintSentimentRequest;
import tn.esprit.reclamationreponse.dto.ComplaintSentimentResponse;
import tn.esprit.reclamationreponse.dto.ResponseAiAssistRequest;
import tn.esprit.reclamationreponse.dto.ResponseAiAssistResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ComplaintAiAssistService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Set<String> URGENT_KEYWORDS = new HashSet<>(Arrays.asList(
        "urgent", "urgence", "asap", "immediately", "immediat", "critical", "critique",
        "bloque", "blocked", "cannot access", "impossible", "payment", "paiement",
        "security", "securite", "fraud", "arnaque", "error 500", "down", "panne"
    ));
    private static final Set<String> NEGATIVE_KEYWORDS = new HashSet<>(Arrays.asList(
        "problem", "probleme", "issue", "failed", "failure", "error", "bug", "slow",
        "blocked", "bloque", "unacceptable", "angry", "frustrated", "frustrating",
        "refund", "cancel", "rejected", "not working", "ne fonctionne", "impossible"
    ));
    private static final Set<String> POSITIVE_KEYWORDS = new HashSet<>(Arrays.asList(
        "thanks", "thank you", "merci", "great", "good", "excellent", "satisfied", "resolved"
    ));

    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final List<String> fallbackModels;
    private final int maxRetries;

    public ComplaintAiAssistService(
            ObjectMapper objectMapper,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model,
            @Value("${gemini.fallback-models:gemini-2.0-flash,gemini-1.5-flash}") String fallbackModelsCsv,
            @Value("${gemini.max-retries:2}") int maxRetries
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.fallbackModels = Arrays.stream(fallbackModelsCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
        this.maxRetries = Math.max(maxRetries, 1);
    }

    public ComplaintAiAssistResponse assist(ComplaintAiAssistRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }

        if (request == null) {
            throw new IllegalArgumentException("assist request is required");
        }

        String mode = normalizeMode(request.mode());
        String title = safe(request.title());
        String description = safe(request.description());
        if ("IMPROVE".equals(mode) && description.isBlank()) {
            mode = "GENERATE";
        }

        String prompt = buildPrompt(mode, title, description);
        String rawText;
        try {
            rawText = callGemini(prompt);
        } catch (IllegalStateException ex) {
            // Keep UX smooth even if Gemini is temporarily unavailable.
            return buildLocalFallback(mode, title, description);
        }

        return parseAssistResponse(rawText, title, description);
    }

    public ResponseAiAssistResponse assistResponse(ResponseAiAssistRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }

        if (request == null) {
            throw new IllegalArgumentException("response assist request is required");
        }

        String mode = normalizeMode(request.mode());
        String complaintTitle = safe(request.complaintTitle());
        String complaintDescription = safe(request.complaintDescription());
        String draftResponse = safe(request.draftResponse());
        if ("IMPROVE".equals(mode) && draftResponse.isBlank()) {
            mode = "GENERATE";
        }

        String prompt = buildResponsePrompt(mode, complaintTitle, complaintDescription, draftResponse);
        String rawText;
        try {
            rawText = callGemini(prompt);
        } catch (IllegalStateException ex) {
            return buildLocalResponseFallback(mode, complaintTitle, complaintDescription, draftResponse);
        }

        return parseResponseAssist(rawText, complaintTitle, complaintDescription, draftResponse);
    }

    public ComplaintSentimentResponse analyzeComplaintSentiment(ComplaintSentimentRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }

        if (request == null) {
            throw new IllegalArgumentException("sentiment request is required");
        }

        Long complaintId = request.complaintId();
        String title = safe(request.title());
        String description = safe(request.description());

        if (title.isBlank() && description.isBlank()) {
            return new ComplaintSentimentResponse(complaintId, "NEUTRAL", false, "Not enough text to analyze.");
        }

        String prompt = buildSentimentPrompt(title, description);
        String rawText;
        try {
            rawText = callGemini(prompt);
        } catch (IllegalStateException ex) {
            return buildLocalSentimentFallback(complaintId, title, description);
        }

        return parseSentimentResponse(rawText, complaintId, title, description);
    }

    private String callGemini(String prompt) {
        List<String> modelsToTry = new ArrayList<>();
        modelsToTry.add(model);
        for (String fallbackModel : fallbackModels) {
            if (!fallbackModel.equals(model)) {
                modelsToTry.add(fallbackModel);
            }
        }

        IllegalStateException lastException = null;
        for (String modelToTry : modelsToTry) {
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    return executeGeminiRequest(modelToTry, prompt);
                } catch (IllegalStateException ex) {
                    lastException = ex;

                    boolean retriable = isRetriableGeminiError(ex.getMessage());
                    boolean hasMoreAttempts = attempt < maxRetries;
                    if (retriable && hasMoreAttempts) {
                        continue;
                    }

                    break;
                }
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new IllegalStateException("Gemini request failed");
    }

    private String executeGeminiRequest(String modelToTry, String prompt) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            ArrayNode contents = payload.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            parts.addObject().put("text", prompt);

            ObjectNode generationConfig = payload.putObject("generationConfig");
            generationConfig.put("temperature", 0.3);
            generationConfig.put("maxOutputTokens", 500);
            generationConfig.put("responseMimeType", "application/json");

            String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
            + modelToTry
                    + ":generateContent?key="
                    + encodedKey;

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                String errorMessage = extractGeminiErrorMessage(response.body());
                throw new IllegalStateException(
                        "Gemini request failed with status "
                                + response.statusCode()
                                + (errorMessage.isBlank() ? "" : " - " + errorMessage)
                );
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new IllegalStateException("Gemini returned no candidate response");
            }

            JsonNode partsNode = candidates.get(0).path("content").path("parts");
            if (!partsNode.isArray() || partsNode.isEmpty()) {
                throw new IllegalStateException("Gemini returned empty content");
            }

            StringBuilder textBuilder = new StringBuilder();
            for (JsonNode part : partsNode) {
                String text = part.path("text").asText("");
                if (!text.isBlank()) {
                    if (textBuilder.length() > 0) {
                        textBuilder.append("\n");
                    }
                    textBuilder.append(text);
                }
            }

            String result = textBuilder.toString().trim();
            if (result.isBlank()) {
                throw new IllegalStateException("Gemini returned empty text");
            }

            return result;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate AI assistance", ex);
        }
    }

    private String extractGeminiErrorMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("error").path("message").asText("");
            return message == null ? "" : message.trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private boolean isRetriableGeminiError(String message) {
        if (message == null) {
            return false;
        }

        return message.contains("status 429")
                || message.contains("status 500")
                || message.contains("status 502")
                || message.contains("status 503")
                || message.contains("status 504");
    }

    private ComplaintAiAssistResponse buildLocalFallback(String mode, String title, String description) {
        if ("GENERATE".equals(mode)) {
            String generatedTitle = title.isBlank()
                    ? "Issue encountered while using the platform"
                    : capitalize(title);

            String generatedDescription;
            if (description.isBlank()) {
                generatedDescription = "I encountered an issue while using the platform. "
                        + "Please review this complaint and guide me through the resolution steps.";
            } else {
                generatedDescription = normalizeSentence(description);
            }

            return new ComplaintAiAssistResponse(generatedTitle, generatedDescription);
        }

        return new ComplaintAiAssistResponse(capitalize(title), normalizeSentence(description));
    }

    private String normalizeSentence(String value) {
        String cleaned = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (cleaned.isBlank()) {
            return "";
        }

        String capitalized = capitalize(cleaned);
        if (!capitalized.endsWith(".") && !capitalized.endsWith("!") && !capitalized.endsWith("?")) {
            return capitalized + ".";
        }

        return capitalized;
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String trimmed = value.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }

    private ComplaintAiAssistResponse parseAssistResponse(String rawText, String fallbackTitle, String fallbackDescription) {
        String safeRawText = safe(rawText);

        try {
            String jsonPayload = extractJsonObject(safeRawText);
            JsonNode json = objectMapper.readTree(jsonPayload);

            String title = safe(json.path("title").asText(fallbackTitle));
            String description = safe(json.path("description").asText(fallbackDescription));

            return new ComplaintAiAssistResponse(
                    title.isBlank() ? fallbackTitle : title,
                    description.isBlank() ? fallbackDescription : description
            );
        } catch (Exception ex) {
            String extractedTitle = extractJsonField(safeRawText, "title");
            String extractedDescription = extractJsonField(safeRawText, "description");
            if (extractedTitle.isBlank()) {
                extractedTitle = extractJsonFieldLenient(safeRawText, "title");
            }
            if (extractedDescription.isBlank()) {
                extractedDescription = extractJsonFieldLenient(safeRawText, "description");
            }
            if (extractedDescription.isBlank()) {
                extractedDescription = extractJsonFieldLenient(safeRawText, "message");
            }

            String finalTitle = extractedTitle.isBlank() ? fallbackTitle : cleanupJsonArtifacts(extractedTitle, "title");
            String finalDescription = extractedDescription.isBlank()
                    ? fallbackDescription
                    : cleanupJsonArtifacts(extractedDescription, "description");

            if (finalTitle.isBlank()) {
                finalTitle = fallbackTitle;
            }

            return new ComplaintAiAssistResponse(
                    finalTitle,
                    finalDescription.isBlank() ? normalizeSentence(fallbackDescription) : finalDescription
            );
        }
    }

    private ResponseAiAssistResponse parseResponseAssist(
            String rawText,
            String complaintTitle,
            String complaintDescription,
            String draftResponse
    ) {
        String safeRawText = safe(rawText);

        try {
            String jsonPayload = extractJsonObject(safeRawText);
            JsonNode json = objectMapper.readTree(jsonPayload);

            String message = safe(json.path("message").asText(""));
            if (message.isBlank()) {
                message = safe(json.path("description").asText(""));
            }
            if (message.isBlank()) {
                message = safe(json.path("title").asText(""));
            }
            message = cleanupJsonArtifacts(message, "message");

            if (!message.isBlank()) {
                return new ResponseAiAssistResponse(message);
            }
        } catch (Exception ex) {
            // Try a regex extraction fallback below.
        }

        String extractedMessage = extractJsonField(safeRawText, "message");
        if (extractedMessage.isBlank()) {
            extractedMessage = extractJsonField(safeRawText, "description");
        }
        if (extractedMessage.isBlank()) {
            extractedMessage = extractJsonFieldLenient(safeRawText, "message");
        }
        if (extractedMessage.isBlank()) {
            extractedMessage = extractJsonFieldLenient(safeRawText, "description");
        }
        if (extractedMessage.isBlank()) {
            extractedMessage = extractJsonFieldLenient(safeRawText, "title");
        }
        if (extractedMessage.isBlank()) {
            extractedMessage = safeRawText;
        }

        String cleanedMessage = sanitizeEchoedContext(cleanupJsonArtifacts(extractedMessage, "message"));
        if (cleanedMessage.isBlank()) {
            return buildLocalResponseFallback("IMPROVE", complaintTitle, complaintDescription, draftResponse);
        }

        return new ResponseAiAssistResponse(cleanedMessage);
    }

    private ResponseAiAssistResponse buildLocalResponseFallback(
            String mode,
            String complaintTitle,
            String complaintDescription,
            String draftResponse
    ) {
        if ("GENERATE".equals(mode)) {
            if (!draftResponse.isBlank()) {
                return new ResponseAiAssistResponse(normalizeSentence(draftResponse));
            }

            String topic = complaintTitle.isBlank() ? "your complaint" : complaintTitle;
            String generated = "Hello, we have received your complaint about " + topic
                    + ". We are currently reviewing your request and will get back to you as soon as possible."
                    + " Thank you for your patience and understanding.";
            return new ResponseAiAssistResponse(generated);
        }

        if (!draftResponse.isBlank()) {
            return new ResponseAiAssistResponse(normalizeSentence(draftResponse));
        }

        if (!complaintDescription.isBlank()) {
            String generated = "Hello, thank you for your feedback. We have taken note of your message and"
                    + " our team will review this issue carefully before providing a final answer.";
            return new ResponseAiAssistResponse(generated);
        }

        return new ResponseAiAssistResponse(
                "Hello, we have received your request and will provide you with an update as soon as possible."
        );
    }

    private String sanitizeEchoedContext(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }

        String cleaned = message
                .replace("Complaint details:", "")
                .replace("Draft response:", "")
                .trim();

        cleaned = cleaned
                .replace("complaintDetails:", "")
                .replace("draftResponse:", "")
                .trim();

        if (cleaned.equalsIgnoreCase("Complaint details:") || cleaned.equalsIgnoreCase("Draft response:")) {
            return "";
        }

        return cleaned;
    }

    private String extractJsonField(String text, String fieldName) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String pattern = "[\\\"']" + Pattern.quote(fieldName) + "[\\\"']\\s*:\\s*[\\\"']((?:\\\\.|[^\\\"'])*)[\\\"']";
        Pattern fieldPattern = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher matcher = fieldPattern.matcher(text);
        if (!matcher.find()) {
            return "";
        }

        String value = matcher.group(1);
        return unescapeJsonLikeString(value).trim();
    }

    private String extractJsonFieldLenient(String text, String fieldName) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalized = text
                .replace("```json", "")
                .replace("```", "")
                .trim();

        String pattern = "[\\\"']?" + Pattern.quote(fieldName) + "[\\\"']?\\s*:\\s*";
        Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(normalized);
        if (!matcher.find()) {
            return "";
        }

        int index = matcher.end();
        while (index < normalized.length() && Character.isWhitespace(normalized.charAt(index))) {
            index++;
        }

        if (index >= normalized.length()) {
            return "";
        }

        char quote = 0;
        char first = normalized.charAt(index);
        if (first == '"' || first == '\'') {
            quote = first;
            index++;
        }

        StringBuilder builder = new StringBuilder();
        boolean escaped = false;
        for (int i = index; i < normalized.length(); i++) {
            char current = normalized.charAt(i);

            if (quote != 0) {
                if (!escaped && current == '\\') {
                    escaped = true;
                    builder.append(current);
                    continue;
                }

                if (!escaped && current == quote) {
                    break;
                }

                escaped = false;
                builder.append(current);
                continue;
            }

            if (current == ',' || current == '}' || current == '\n' || current == '\r') {
                break;
            }

            builder.append(current);
        }

        return unescapeJsonLikeString(builder.toString()).trim();
    }

    private String cleanupJsonArtifacts(String value, String preferredFieldName) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String cleaned = value
                .replace("```json", "")
                .replace("```", "")
                .trim();

        String preferredPrefix = "^[\\{\\s]*[\\\"']?" + Pattern.quote(preferredFieldName) + "[\\\"']?\\s*:\\s*[\\\"']?";
        cleaned = cleaned.replaceFirst(preferredPrefix, "");
        cleaned = cleaned.replaceFirst("^[\\{\\s]*[\\\"']?(message|description|title)[\\\"']?\\s*:\\s*[\\\"']?", "");
        cleaned = cleaned.replaceFirst("[\\\"'\\s}]+$", "").trim();

        return cleaned;
    }

    private String unescapeJsonLikeString(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\'", "'")
                .replace("\\\\", "\\");
    }

    private String extractJsonObject(String text) {
        String normalized = text
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int start = normalized.indexOf('{');
        int end = normalized.lastIndexOf('}');

        if (start >= 0 && end > start) {
            return normalized.substring(start, end + 1);
        }

        return normalized;
    }

    private String buildPrompt(String mode, String title, String description) {
        StringBuilder builder = new StringBuilder();
        builder.append("Tu es un assistant de support client.")
                .append(" Retourne uniquement un JSON valide avec les clés title et description. ")
                .append("Aucune explication, aucun markdown.\n");

        if ("GENERATE".equals(mode)) {
            builder.append("Objectif: proposer un titre et une description clairs et professionnels pour une réclamation client.")
                    .append(" Si des éléments sont déjà fournis, améliore-les. S'ils sont vides, génère un contenu pertinent.\n");
        } else {
            builder.append("Objectif: corriger grammaticalement et reformuler la réclamation en conservant exactement l'intention utilisateur.")
                    .append(" Retourne une version réellement améliorée (pas la copie brute du texte d'entrée).\n");
        }

        builder.append("Contraintes: français, ton poli, précis, concis, sans contenu offensant.\n")
                .append("Entrée:\n")
                .append("title=").append(title).append("\n")
                .append("description=").append(description).append("\n")
                .append("Format attendu:\n")
                .append("{\"title\":\"...\",\"description\":\"...\"}");

        return builder.toString();
    }

    private String buildResponsePrompt(String mode, String complaintTitle, String complaintDescription, String draftResponse) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are a customer support assistant for administrators.")
                .append(" Return only valid JSON with one key: message.")
                .append(" No markdown, no explanations.\n");

        if ("GENERATE".equals(mode)) {
            builder.append("Goal: write a professional support response to the customer's complaint.\n");
        } else {
            builder.append("Goal: improve and rewrite the draft response while preserving its original intent.")
                    .append(" Do not return the exact same input text.\n");
        }

        builder.append("Constraints: concise, polite, clear, neutral tone.\n")
                .append("Input:\n")
                .append("complaintTitle=").append(complaintTitle).append("\n")
                .append("complaintDescription=").append(complaintDescription).append("\n")
                .append("draftResponse=").append(draftResponse).append("\n")
                .append("Expected format:\n")
                .append("{\"message\":\"...\"}");

        return builder.toString();
    }

    private String buildSentimentPrompt(String title, String description) {
        return new StringBuilder()
                .append("You are an AI triage assistant for customer complaints. ")
                .append("Return ONLY valid JSON with keys tone, urgent, reason. ")
                .append("tone must be one of POSITIVE, NEUTRAL, NEGATIVE. ")
                .append("urgent must be true or false. reason must be short (max 20 words).\n")
                .append("Mark urgent=true only if there is immediate impact (payment blocked, account access blocked,")
                .append(" security/fraud risk, or severe outage).\n")
                .append("Input:\n")
                .append("title=").append(title).append("\n")
                .append("description=").append(description).append("\n")
                .append("Expected format:\n")
                .append("{\"tone\":\"NEGATIVE\",\"urgent\":true,\"reason\":\"...\"}")
                .toString();
    }

    private ComplaintSentimentResponse parseSentimentResponse(
            String rawText,
            Long complaintId,
            String title,
            String description
    ) {
        String safeRawText = safe(rawText);
        String fallbackTone = detectToneHeuristically(title, description);
        boolean fallbackUrgent = detectUrgencyHeuristically(title, description, fallbackTone);

        try {
            JsonNode json = objectMapper.readTree(extractJsonObject(safeRawText));

            String tone = normalizeTone(json.path("tone").asText(""));
            if (tone.isBlank()) {
                tone = fallbackTone;
            }

            boolean urgent = parseUrgentNode(json.path("urgent"), fallbackUrgent);
            String reason = safe(json.path("reason").asText(""));
            if (reason.isBlank()) {
                reason = safe(json.path("summary").asText(""));
            }
            reason = cleanupJsonArtifacts(reason, "reason");
            if (reason.isBlank()) {
                reason = buildDefaultSentimentReason(tone, urgent);
            }

            return new ComplaintSentimentResponse(complaintId, tone, urgent, reason);
        } catch (Exception ex) {
            String tone = normalizeTone(extractJsonField(safeRawText, "tone"));
            if (tone.isBlank()) {
                tone = normalizeTone(extractJsonFieldLenient(safeRawText, "tone"));
            }
            if (tone.isBlank()) {
                tone = fallbackTone;
            }

            String urgentText = extractJsonField(safeRawText, "urgent");
            if (urgentText.isBlank()) {
                urgentText = extractJsonFieldLenient(safeRawText, "urgent");
            }
            boolean urgent = parseUrgentValue(urgentText, fallbackUrgent);

            String reason = extractJsonField(safeRawText, "reason");
            if (reason.isBlank()) {
                reason = extractJsonField(safeRawText, "summary");
            }
            if (reason.isBlank()) {
                reason = extractJsonFieldLenient(safeRawText, "reason");
            }
            if (reason.isBlank()) {
                reason = buildDefaultSentimentReason(tone, urgent);
            }

            return new ComplaintSentimentResponse(
                    complaintId,
                    tone,
                    urgent,
                    cleanupJsonArtifacts(reason, "reason")
            );
        }
    }

    private ComplaintSentimentResponse buildLocalSentimentFallback(Long complaintId, String title, String description) {
        String tone = detectToneHeuristically(title, description);
        boolean urgent = detectUrgencyHeuristically(title, description, tone);
        String reason = buildDefaultSentimentReason(tone, urgent);

        return new ComplaintSentimentResponse(complaintId, tone, urgent, reason);
    }

    private String detectToneHeuristically(String title, String description) {
        String text = (safe(title) + " " + safe(description)).toLowerCase(Locale.ROOT);

        int positiveScore = scoreKeywordMatches(text, POSITIVE_KEYWORDS);
        int negativeScore = scoreKeywordMatches(text, NEGATIVE_KEYWORDS);

        if (negativeScore > positiveScore) {
            return "NEGATIVE";
        }

        if (positiveScore > negativeScore) {
            return "POSITIVE";
        }

        return "NEUTRAL";
    }

    private boolean detectUrgencyHeuristically(String title, String description, String tone) {
        String text = (safe(title) + " " + safe(description)).toLowerCase(Locale.ROOT);
        int urgentScore = scoreKeywordMatches(text, URGENT_KEYWORDS);

        if (urgentScore > 0) {
            return true;
        }

        return "NEGATIVE".equals(tone) && (text.contains("payment") || text.contains("paiement") || text.contains("blocked") || text.contains("bloque"));
    }

    private int scoreKeywordMatches(String text, Set<String> keywords) {
        int score = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                score++;
            }
        }
        return score;
    }

    private String normalizeTone(String tone) {
        String normalized = safe(tone).toUpperCase(Locale.ROOT);
        if ("POSITIVE".equals(normalized) || "NEUTRAL".equals(normalized) || "NEGATIVE".equals(normalized)) {
            return normalized;
        }
        return "";
    }

    private boolean parseUrgentNode(JsonNode urgentNode, boolean fallback) {
        if (urgentNode == null || urgentNode.isMissingNode() || urgentNode.isNull()) {
            return fallback;
        }

        if (urgentNode.isBoolean()) {
            return urgentNode.asBoolean();
        }

        return parseUrgentValue(urgentNode.asText(""), fallback);
    }

    private boolean parseUrgentValue(String value, boolean fallback) {
        String normalized = safe(value).toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return fallback;
        }

        if (normalized.equals("true") || normalized.equals("yes") || normalized.equals("oui") || normalized.equals("urgent") || normalized.equals("high")) {
            return true;
        }

        if (normalized.equals("false") || normalized.equals("no") || normalized.equals("non") || normalized.equals("low")) {
            return false;
        }

        return fallback;
    }

    private String buildDefaultSentimentReason(String tone, boolean urgent) {
        if (urgent) {
            return "Potential urgent impact detected; prioritize admin review.";
        }

        if ("NEGATIVE".equals(tone)) {
            return "Negative sentiment detected; follow-up is recommended.";
        }

        if ("POSITIVE".equals(tone)) {
            return "Positive tone detected with no urgent signal.";
        }

        return "Neutral tone detected with no urgent signal.";
    }

    private String normalizeMode(String mode) {
        String safeMode = safe(mode).toUpperCase();
        if ("GENERATE".equals(safeMode) || "IMPROVE".equals(safeMode)) {
            return safeMode;
        }
        return "IMPROVE";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
