package com.fluently.feedbackservice.services;

import com.fluently.feedbackservice.dto.ModerationResult;
import com.fluently.feedbackservice.entities.*;
import com.fluently.feedbackservice.repositories.FeedbackRepository;
import com.fluently.feedbackservice.repositories.FlaggedFeedbackRepository;
import com.fluently.feedbackservice.repositories.ProfessorBadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeaturesFeedbackTest {

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. BAD-WORD FILTERING — ContentModerationService#moderate()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("1. Bad-word filtering")
    class BadWordFilteringTest {

        @Mock
        private FlaggedFeedbackRepository flaggedFeedbackRepository;
        @Mock
        private RestTemplate restTemplate;

        @InjectMocks
        private ContentModerationService moderationService;

        @Test
        @DisplayName("Clean comment → not flagged, content unchanged")
        void cleanComment_NotFlagged() {
            // Simulate PurgoMalum returning the exact same text (no bad words)
            ContentModerationService.PurgoMalumResponse purgoResponse = new ContentModerationService.PurgoMalumResponse();
            purgoResponse.setResult("Great lesson!");
            when(restTemplate.getForObject(any(String.class), eq(ContentModerationService.PurgoMalumResponse.class)))
                    .thenReturn(purgoResponse);

            ModerationResult result = moderationService.moderate("Great lesson!");

            assertFalse(result.isFlagged(), "Clean comment should not be flagged");
            assertEquals("Great lesson!", result.getCleanedContent());
        }

        @Test
        @DisplayName("Comment with bad word → flagged, word replaced by * in cleaned")
        void badWordComment_Flagged() {
            // PurgoMalum replaces the bad word with stars
            ContentModerationService.PurgoMalumResponse purgoResponse = new ContentModerationService.PurgoMalumResponse();
            purgoResponse.setResult("This is ****");
            when(restTemplate.getForObject(any(String.class), eq(ContentModerationService.PurgoMalumResponse.class)))
                    .thenReturn(purgoResponse);

            ModerationResult result = moderationService.moderate("This is shit");

            assertTrue(result.isFlagged(), "Comment with bad word should be flagged");
            assertEquals("This is ****", result.getCleanedContent(), "Bad word must be replaced with *");
        }

        @Test
        @DisplayName("Local bad word (e.g. 'idiot') → flagged even if API returns null")
        void localBadWord_FlaggedWithoutApi() {
            when(restTemplate.getForObject(any(String.class), eq(ContentModerationService.PurgoMalumResponse.class)))
                    .thenReturn(null); // API unreachable

            ModerationResult result = moderationService.moderate("You are an idiot");

            assertTrue(result.isFlagged(), "Local blacklist must catch 'idiot'");
        }

        @Test
        @DisplayName("Gibberish word throws exception during English validation")
        void validateEnglish_ThrowsOnGibberish() {
            // Mock API saying the word "shita" doesn't exist (NotFound)
            when(restTemplate.getForObject("https://api.dictionaryapi.dev/api/v2/entries/en/shita", Object.class))
                    .thenThrow(new org.springframework.web.client.HttpClientErrorException(
                            org.springframework.http.HttpStatus.NOT_FOUND));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> moderationService.validateEnglish("This is shita"));

            assertTrue(ex.getMessage().contains("We know slang is part of English"),
                    "Should throw the english practice message");
        }

        @Test
        @DisplayName("Valid English word passes validation gracefully")
        void validateEnglish_PassesValidWord() {
            // Mock API responding with proper 200 OK for word "good"
            when(restTemplate.getForObject("https://api.dictionaryapi.dev/api/v2/entries/en/good", Object.class))
                    .thenReturn(new Object());

            assertDoesNotThrow(() -> moderationService.validateEnglish("This is good"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. COOLDOWN BAN — ContentModerationService#checkCooldown()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("2. Cooldown ban (5 min)")
    class CooldownTest {

        @Mock
        private FlaggedFeedbackRepository flaggedFeedbackRepository;
        @Mock
        private RestTemplate restTemplate;

        @InjectMocks
        private ContentModerationService moderationService;

        @BeforeEach
        void setUp() {
            // @Value fields are not processed by Mockito — inject manually
            ReflectionTestUtils.setField(moderationService, "cooldownMinutes", 5);
        }

        @Test
        @DisplayName("Student with < 3 confirmed flags → no exception")
        void belowThreshold_NoBan() {
            when(flaggedFeedbackRepository.countByStudentIdAndStatus("stud1", FlagStatus.CONFIRMED))
                    .thenReturn(2L);

            assertDoesNotThrow(() -> moderationService.checkCooldown("stud1"));
        }

        @Test
        @DisplayName("Student in active cooldown → throws with remaining seconds")
        void activeCooldown_Throws() {
            when(flaggedFeedbackRepository.countByStudentIdAndStatus("stud1", FlagStatus.CONFIRMED))
                    .thenReturn(3L);

            // Last flag was 2 minutes ago → still in 5-min cooldown
            FlaggedFeedback recentFlag = new FlaggedFeedback();
            recentFlag.setFlaggedAt(LocalDateTime.now().minusMinutes(2));
            when(flaggedFeedbackRepository.findLatestConfirmedFlag("stud1"))
                    .thenReturn(Optional.of(recentFlag));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> moderationService.checkCooldown("stud1"));

            assertTrue(ex.getMessage().contains("temporarily restricted"),
                    "Exception should mention the restriction");
        }

        @Test
        @DisplayName("Student whose cooldown has expired → no exception")
        void expiredCooldown_NoBan() {
            when(flaggedFeedbackRepository.countByStudentIdAndStatus("stud1", FlagStatus.CONFIRMED))
                    .thenReturn(3L);

            // Last flag was 10 minutes ago → cooldown (5 min) has already passed
            FlaggedFeedback oldFlag = new FlaggedFeedback();
            oldFlag.setFlaggedAt(LocalDateTime.now().minusMinutes(10));
            when(flaggedFeedbackRepository.findLatestConfirmedFlag("stud1"))
                    .thenReturn(Optional.of(oldFlag));

            assertDoesNotThrow(() -> moderationService.checkCooldown("stud1"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. BADGE SYSTEM — ProfessorBadgeService#recalculateBadge()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("3. Professor badge system")
    class BadgeSystemTest {

        @Mock
        private ProfessorBadgeRepository badgeRepository;
        @Mock
        private FeedbackRepository feedbackRepository;

        @InjectMocks
        private ProfessorBadgeService badgeService;

        @Test
        @DisplayName("Score ≥ 300 → GOLD badge")
        void highScore_GoldBadge() {
            when(feedbackRepository.calculateTotalScoreForProfessor("prof1")).thenReturn(350);
            when(feedbackRepository.calculateAverageStarRatingForProfessor("prof1")).thenReturn(4.2);
            when(badgeRepository.findByProfessorId("prof1")).thenReturn(Optional.empty());
            when(badgeRepository.save(any(ProfessorBadge.class))).thenAnswer(i -> i.getArgument(0));

            ProfessorBadge badge = badgeService.recalculateBadge("prof1");

            assertEquals(BadgeType.GOLD, badge.getBadgeType());
            assertEquals("GOLD", badge.getStarBadge()); // avg 4.2 → GOLD star
        }

        @Test
        @DisplayName("Score = 0 → NONE badge")
        void zeroScore_NoBadge() {
            when(feedbackRepository.calculateTotalScoreForProfessor("prof2")).thenReturn(0);
            when(feedbackRepository.calculateAverageStarRatingForProfessor("prof2")).thenReturn(0.0);
            when(badgeRepository.findByProfessorId("prof2")).thenReturn(Optional.empty());
            when(badgeRepository.save(any(ProfessorBadge.class))).thenAnswer(i -> i.getArgument(0));

            ProfessorBadge badge = badgeService.recalculateBadge("prof2");

            assertEquals(BadgeType.NONE, badge.getBadgeType());
            assertEquals("NONE", badge.getStarBadge());
        }

        @Test
        @DisplayName("Existing badge → updated in place, not a new record")
        void existingBadge_UpdatedInPlace() {
            ProfessorBadge existing = new ProfessorBadge();
            existing.setProfessorId("prof3");
            existing.setBadgeType(BadgeType.ROOKIE);

            when(feedbackRepository.calculateTotalScoreForProfessor("prof3")).thenReturn(600);
            when(feedbackRepository.calculateAverageStarRatingForProfessor("prof3")).thenReturn(4.9);
            when(badgeRepository.findByProfessorId("prof3")).thenReturn(Optional.of(existing));
            when(badgeRepository.save(any(ProfessorBadge.class))).thenAnswer(i -> i.getArgument(0));

            ProfessorBadge badge = badgeService.recalculateBadge("prof3");

            // Same object updated, not a new one
            assertSame(existing, badge);
            assertEquals(BadgeType.PLATINUM, badge.getBadgeType());
            assertEquals("LEGEND", badge.getStarBadge()); // avg 4.9 ≥ 4.8
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 4. EMAIL ALERT — EmailService#sendRepetitivePatternAlert()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("4. Email alert (professor notification)")
    class EmailAlertTest {

        @Mock
        private JavaMailSender mailSender;

        @InjectMocks
        private EmailService emailService;

        @BeforeEach
        void setUp() {
            // @Value("${spring.mail.username}") not resolved by Mockito — inject manually
            ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@jungle.com");
        }

        @Test
        @DisplayName("Valid data → mailSender.send() is called once")
        void sendsEmail_WhenValidData() {
            jakarta.mail.internet.MimeMessage mimeMsg = mock(jakarta.mail.internet.MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMsg);

            Map<String, List<String>> patterns = Map.of(
                    "audio", List.of("audio issue", "audio problem"));

            assertDoesNotThrow(
                    () -> emailService.sendRepetitivePatternAlert("chirine7dardouri@gmail.com", 1L, patterns));

            verify(mailSender, times(1)).send(any(jakarta.mail.internet.MimeMessage.class));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 5. STATISTICS / ANALYTICS — PatternDetectionService
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("5. Statistics & analytics")
    class StatisticsTest {

        @Mock
        private FeedbackRepository feedbackRepository;
        @Mock
        private KeycloakService keycloakService;
        @Mock
        private EmailService emailService;

        @InjectMocks
        private PatternDetectionService patternDetectionService;

        @Test
        @DisplayName("extractMeaningfulWords - strips stopwords and punctuation")
        void extractMeaningfulWords_FiltersCorrectly() {
            // "this" and "great" are stopwords/short; "audio" and "problem" should survive
            var words = patternDetectionService.extractMeaningfulWords("This audio has a great problem!");

            assertTrue(words.contains("audio"));
            assertTrue(words.contains("problem"));
            assertFalse(words.contains("this"));
            assertFalse(words.contains("great")); // stopword
        }

        @Test
        @DisplayName("getGlobalIssues - counts keyword frequency across all visible feedbacks")
        void getGlobalIssues_CountsKeywords() {
            Feedback f1 = makeFeedback("The audio keeps cutting out");
            Feedback f2 = makeFeedback("Audio quality is terrible");
            Feedback f3 = makeFeedback("Cannot hear audio properly");

            when(feedbackRepository.findAll()).thenReturn(List.of(f1, f2, f3));

            List<Map<String, Object>> issues = patternDetectionService.getGlobalIssues(5);

            assertFalse(issues.isEmpty());
            // "audio" appears in all 3 feedbacks → should be top keyword
            Map<String, Object> top = issues.get(0);
            assertEquals("audio", top.get("keyword"));
            assertEquals(3, top.get("count"));
        }

        @Test
        @DisplayName("analyzeAndNotify - does NOT send email when < 3 visible feedbacks")
        void analyzeAndNotify_SkipsIfTooFewFeedbacks() {
            Feedback f1 = makeFeedback("Audio issue");
            when(feedbackRepository.findByLessonId(1L)).thenReturn(List.of(f1));

            patternDetectionService.analyzeAndNotify(1L, "prof1");

            verifyNoInteractions(emailService); // no email sent
        }

        @Test
        @DisplayName("analyzeAndNotify - sends email when repeated keyword threshold is met")
        void analyzeAndNotify_SendsEmailOnPattern() {
            // 3 visible feedbacks all mentioning "audio"
            List<Feedback> feedbacks = List.of(
                    makeFeedback("The audio keeps cutting out"),
                    makeFeedback("Audio quality is terrible"),
                    makeFeedback("Cannot hear audio properly"));
            when(feedbackRepository.findByLessonId(1L)).thenReturn(feedbacks);
            when(keycloakService.getProfessorEmail("prof1")).thenReturn("chirine7dardouri@gmail.com");

            patternDetectionService.analyzeAndNotify(1L, "prof1");

            verify(emailService, times(1))
                    .sendRepetitivePatternAlert(eq("chirine7dardouri@gmail.com"), eq(1L), any());
        }

        @Test
        @DisplayName("analyzeAndNotify - skips email when professor email is blank")
        void analyzeAndNotify_SkipsEmailIfNoProfessorEmail() {
            List<Feedback> feedbacks = List.of(
                    makeFeedback("The audio keeps cutting out"),
                    makeFeedback("Audio quality is terrible"),
                    makeFeedback("Cannot hear audio properly"));
            when(feedbackRepository.findByLessonId(1L)).thenReturn(feedbacks);
            when(keycloakService.getProfessorEmail("prof1")).thenReturn(""); // no email

            patternDetectionService.analyzeAndNotify(1L, "prof1");

            verifyNoInteractions(emailService);
        }

        private Feedback makeFeedback(String content) {
            Feedback f = new Feedback();
            f.setContent(content);
            f.setVisible(true);
            return f;
        }
    }
}
