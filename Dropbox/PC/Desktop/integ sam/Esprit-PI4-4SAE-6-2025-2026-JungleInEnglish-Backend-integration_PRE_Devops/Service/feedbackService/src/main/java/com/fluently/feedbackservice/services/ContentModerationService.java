package com.fluently.feedbackservice.services;

import com.fluently.feedbackservice.dto.ModerationResult;
import com.fluently.feedbackservice.entities.FlagStatus;
import com.fluently.feedbackservice.repositories.FlaggedFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class ContentModerationService {

    private static final String PURGOMALUM_URL = "https://www.purgomalum.com/service/json";
    private static final String DICTIONARY_API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";
    private static final int FLAGS_BEFORE_COOLDOWN = 3;

    @Value("${moderation.cooldown-minutes:5}")
    private int cooldownMinutes;

    // local backup filter: full words only
    private static final Pattern LOCAL_BAD_WORDS = Pattern.compile(
            "\\b(damn|idiot|stupid|dumb|trash|garbage|loser|moron|hate|ugly|pathetic|nonsense|shutup|fool|useless)\\b",
            Pattern.CASE_INSENSITIVE);

    @Autowired
    private FlaggedFeedbackRepository flaggedFeedbackRepository;

    @Autowired
    private RestTemplate restTemplate;

    // scheduler: auto-lift cooldown, check every 1 min
    @Scheduled(fixedRateString = "${moderation.cooldown-check-ms:60000}")
    public void expireStaleFlags() {
        flaggedFeedbackRepository.expireOldFlags(LocalDateTime.now().minusMinutes(cooldownMinutes));
    }

    // if student in cooldown : msg with remaining time
    public void checkCooldown(String studentId) {
        long confirmedFlags = flaggedFeedbackRepository
                .countByStudentIdAndStatus(studentId, FlagStatus.CONFIRMED);

        if (confirmedFlags < FLAGS_BEFORE_COOLDOWN)
            return;

        flaggedFeedbackRepository.findLatestConfirmedFlag(studentId).ifPresent(latest -> {
            LocalDateTime cooldownEnds = latest.getFlaggedAt().plusMinutes(cooldownMinutes);
            if (LocalDateTime.now().isBefore(cooldownEnds)) {
                long secondsLeft = java.time.Duration.between(LocalDateTime.now(), cooldownEnds).getSeconds();
                throw new RuntimeException(
                        "You are temporarily restricted from submitting feedback. " +
                                "Please wait " + secondsLeft + " seconds.");
            }
        });
    }

    // local regex check with word boundaries so substrings (word inside word) pass
    private boolean containsBlockedWordLocally(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }
        return LOCAL_BAD_WORDS.matcher(content).find();
    }

    public void validateEnglish(String content) {
        if (content == null || content.isBlank())
            return;

        String[] words = content.toLowerCase().replaceAll("[^a-z\\s]", "").split("\\s+");
        int checks = 0;

        for (String word : words) {
            if (word.length() >= 4) {
                try {
                    restTemplate.getForObject(DICTIONARY_API_URL + word, Object.class);
                } catch (org.springframework.web.client.HttpClientErrorException e) {
                    if (e.getStatusCode() == org.springframework.http.HttpStatus.NOT_FOUND) {
                        throw new RuntimeException(
                                "We know slang is part of English but we want you to practice writing :) Let's try again !");
                    }
                } catch (Exception e) {
                    // Ignore other API errors so we don't break the flow
                }
                checks++;
                if (checks >= 5)
                    break; // Check maximum 5 words to keep the API fast
            }
        }
    }

    // Sends content to PurgoMalum. If the returned text differs from the original,
    // bad words were detected
    public ModerationResult moderate(String content) {
        try {
            boolean locallyFlagged = containsBlockedWordLocally(content);

            String encoded = URLEncoder.encode(content, StandardCharsets.UTF_8);
            String url = PURGOMALUM_URL + "?text=" + encoded;

            PurgoMalumResponse response = restTemplate.getForObject(url, PurgoMalumResponse.class);

            if (response == null || response.getResult() == null) {
                // If API is unreachable, use local check only
                return new ModerationResult(locallyFlagged, content);
            }

            String cleaned = response.getResult();
            boolean apiFlagged = !cleaned.equals(content);

            return new ModerationResult(locallyFlagged || apiFlagged, cleaned);

        } catch (Exception e) {
            // don't block students if the API is down
            return new ModerationResult(containsBlockedWordLocally(content), content);
        }
    }

    // Inner class to deserialize PurgoMalum JSON response { "result": "..." }
    public static class PurgoMalumResponse {
        private String result;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}