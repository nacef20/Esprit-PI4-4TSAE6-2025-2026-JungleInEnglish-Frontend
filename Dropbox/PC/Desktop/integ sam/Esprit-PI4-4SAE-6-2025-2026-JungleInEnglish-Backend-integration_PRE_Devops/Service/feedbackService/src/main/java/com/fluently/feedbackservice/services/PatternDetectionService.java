package com.fluently.feedbackservice.services;


import com.fluently.feedbackservice.entities.Feedback;
import com.fluently.feedbackservice.repositories.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatternDetectionService {

    private static final int REPEAT_THRESHOLD = 3;
    private static final int MIN_WORD_LENGTH = 4;

    // Words to ignore — too common to be meaningful
    private static final Set<String> STOPWORDS = Set.of(
            "this", "that", "with", "have", "from", "they", "will", "been",
            "were", "your", "when", "what", "which", "there", "their", "about",
            "would", "could", "should", "just", "also", "even", "some", "more",
            "very", "really", "please", "thank", "good", "great", "nice", "love",
            "like", "course", "lesson", "class", "teacher", "professor", "student",
            "actually", "isnt", "doesnt", "wasnt", "dont", "cant", "couldnt", 
            "shouldnt", "arent", "basically", "literally", "totally", "every",
            "thing", "think", "thought", "maybe", "perhaps", "anyway", "already"
    );

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private EmailService emailService;

    public void analyzeAndNotify(Long lessonId, String professorId) {
    //debug
        System.out.println(">>> analyzeAndNotify called for lessonId=" + lessonId + " professorId=" + professorId);

        List<Feedback> feedbacks = feedbackRepository.findByLessonId(lessonId)
                .stream()
                .filter(Feedback::isVisible)
                .collect(Collectors.toList());

        System.out.println(">>> visible feedbacks count: " + feedbacks.size());
        if (feedbacks.size() < REPEAT_THRESHOLD) return;

        // Map: keyword → list of full comments that contain it
        Map<String, List<String>> keywordToComments = new HashMap<>();

        for (Feedback feedback : feedbacks) {
            Set<String> wordsInThisFeedback = extractMeaningfulWords(feedback.getContent());

            for (String word : wordsInThisFeedback) {
                keywordToComments
                        .computeIfAbsent(word, k -> new ArrayList<>())
                        .add(feedback.getContent());
            }
        }

        // Keep only keywords that appear in more than REPEAT_THRESHOLD feedbacks
        Map<String, List<String>> patterns = keywordToComments.entrySet().stream()
                .filter(e -> e.getValue().size() >= REPEAT_THRESHOLD)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        System.out.println(">>> patterns found: " + patterns);

        if (patterns.isEmpty()) {
            System.out.println(">>> no patterns, returning");
            return;
        }

        System.out.println(">>> fetching email for keycloakId=" + professorId);
        String professorEmail = keycloakService.getProfessorEmail(professorId);
        System.out.println(">>> professor email resolved to: " + professorEmail);

        if (professorEmail == null || professorEmail.isBlank()) {
            System.out.println(">>> email is null/blank, returning");
            return;
        }

        emailService.sendRepetitivePatternAlert(professorEmail, lessonId, patterns);
        System.out.println(">>> email sent!");
    }

    // Package-visible so AnalyticsService can reuse it without copying the logic
    Set<String> extractMeaningfulWords(String content) {
        if (content == null || content.isBlank()) return Collections.emptySet();

        return Arrays.stream(content.toLowerCase()
                        .replaceAll("[^a-z\\s]", "") // strip punctuation
                        .split("\\s+"))
                .filter(w -> w.length() >= MIN_WORD_LENGTH)
                .filter(w -> !STOPWORDS.contains(w))
                .collect(Collectors.toSet()); // Set = count word once per feedback
    }

    public List<Map<String, Object>> getGlobalIssues(int topN) {
        List<Feedback> allVisible = feedbackRepository.findAll()
                .stream()
                .filter(Feedback::isVisible)
                .collect(Collectors.toList());

        Map<String, Integer> globalCount = new HashMap<>();

        for (Feedback feedback : allVisible) {
            Set<String> words = extractMeaningfulWords(feedback.getContent());
            for (String word : words) {
                globalCount.merge(word, 1, Integer::sum);
            }
        }

        return globalCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .map(e -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("keyword", e.getKey());
                    entry.put("count", e.getValue());
                    return entry;
                })
                .collect(Collectors.toList());
    }
}