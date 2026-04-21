package tn.esprit.reclamationreponse.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BadWordsFilterService {

    private static final int EMBEDDED_MATCH_MIN_LENGTH = 5;

    private final boolean enabled;
    private final String replacement;
    private final List<Pattern> badWordPatterns;

    public BadWordsFilterService(
            @Value("${complaint.bad-words-enabled:true}") boolean enabled,
            @Value("${complaint.bad-words-replacement:***}") String replacement,
            @Value("${complaint.bad-words:idiot,stupid,dumb,imbecile}") String badWordsCsv
    ) {
        this.enabled = enabled;
        this.replacement = (replacement == null || replacement.isBlank()) ? "***" : replacement;
        this.badWordPatterns = Arrays.stream(badWordsCsv.split(","))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .map(this::buildWordPattern)
                .collect(Collectors.toList());
    }

    public String sanitize(String text) {
        if (!enabled || text == null || text.isBlank() || badWordPatterns.isEmpty()) {
            return text;
        }

        String sanitized = text;
        for (Pattern pattern : badWordPatterns) {
            sanitized = pattern.matcher(sanitized).replaceAll(replacement);
        }
        return sanitized;
    }

    private Pattern buildWordPattern(String word) {
        String escapedWord = Pattern.quote(word);

        if (word.length() >= EMBEDDED_MATCH_MIN_LENGTH) {
            return Pattern.compile("(?iu)" + escapedWord);
        }

        return Pattern.compile("(?iu)(?<!\\p{L})" + escapedWord + "(?!\\p{L})");
    }
}
