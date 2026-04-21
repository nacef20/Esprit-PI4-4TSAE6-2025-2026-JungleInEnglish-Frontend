package tn.esprit.reclamationreponse.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BadWordsFilterServiceTest {

    @Test
    void sanitizeShouldReplaceLongBadWordAsStandaloneWord() {
        BadWordsFilterService service = new BadWordsFilterService(true, "***", "stupid");

        assertEquals("this is ***", service.sanitize("this is stupid"));
    }

    @Test
    void sanitizeShouldReplaceLongBadWordInsideConcatenatedToken() {
        BadWordsFilterService service = new BadWordsFilterService(true, "***", "stupid");

        assertEquals("***institute", service.sanitize("stupidinstitute"));
    }

    @Test
    void sanitizeShouldKeepShortWordBoundariesToAvoidFalsePositives() {
        BadWordsFilterService service = new BadWordsFilterService(true, "***", "con");

        assertEquals("contact support", service.sanitize("contact support"));
    }
}
