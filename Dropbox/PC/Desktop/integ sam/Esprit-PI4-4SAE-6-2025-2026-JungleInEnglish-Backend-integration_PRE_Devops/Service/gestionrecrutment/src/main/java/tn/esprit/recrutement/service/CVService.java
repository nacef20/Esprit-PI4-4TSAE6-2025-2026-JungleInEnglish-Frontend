package tn.esprit.recrutement.service;

import tn.esprit.recrutement.model.CV;
import tn.esprit.recrutement.model.JobOffer;
import tn.esprit.recrutement.model.CVMatch;
import tn.esprit.recrutement.model.User;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import tn.esprit.recrutement.repository.CVRepository;
import tn.esprit.recrutement.repository.JobOfferRepository;
import tn.esprit.recrutement.repository.CVMatchRepository;
import tn.esprit.recrutement.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;

@Service
public class CVService {
    private static final Logger logger = LoggerFactory.getLogger(CVService.class);
    @Autowired
    private CVRepository cvRepository;
    @Autowired
    private JobOfferRepository jobOfferRepository;
    @Autowired
    private CVMatchRepository cvMatchRepository;
    @Autowired
    private NotificationService notificationService;

    // CV Processing
    public Map<String, Object> processCVWithAttributes(byte[] pdfContent, User candidate) {
        String extractedText = extractTextFromPDF(pdfContent);
        CV cv = new CV();
        cv.setUser(candidate);
        cv.setContent(extractedText);
        cv.setUploadDate(new Date());

        // Extract sections with more flexible headers
        Set<String> skills = extractSectionItems(extractedText,
                "Skills|Compétences|Technical Skills|Key Skills|Core Skills|Skill Set");
        Set<String> experiences = extractSectionItems(extractedText,
                "Experience|Expérience professionnelle|Professional Experience|Work Experience|Employment History|Career History");
        Set<String> education = extractSectionItems(extractedText,
                "Education|Diplômes|Academic Background|Academic Qualifications|Educational Background|Degrees|Qualifications");
        Set<String> languages = extractSectionItems(extractedText,
                "Languages|Langues|Language Proficiency|Spoken Languages|Language Skills");
        Set<String> certifications = extractSectionItems(extractedText,
                "Certifications?|Certificats|Professional Certifications|Licenses|Licences|Certificates");

        cv.setSkills(skills);
        cv.setExperiences(experiences);
        cv.setEducation(education);
        cv.setLanguages(languages);
        cv.setCertifications(certifications);

        logger.info("Extracted CV Skills: {}", skills);
        logger.info("Extracted CV Experiences: {}", experiences);
        logger.info("Extracted CV Education: {}", education);
        logger.info("Extracted CV Languages: {}", languages);
        logger.info("Extracted CV Certifications: {}", certifications);

        // Save to database
        cvRepository.save(cv);

        // Return both the CV and the extracted attributes for comparison/debugging
        Map<String, Object> result = new HashMap<>();
        result.put("cv", cv);
        result.put("skills", skills);
        result.put("experiences", experiences);
        result.put("education", education);
        result.put("languages", languages);
        result.put("certifications", certifications);
        return result;
    }

    public Map<String, Object> processCVWithAttributesForJob(byte[] pdfContent, User candidate, Long jobOfferId) {
        String extractedText = extractTextFromPDF(pdfContent);
        CV cv = new CV();
        cv.setUser(candidate);
        cv.setContent(extractedText);
        cv.setUploadDate(new Date());

        // Extract sections with more flexible headers
        Set<String> skills = extractSectionItems(extractedText,
                "Skills|Compétences|Technical Skills|Key Skills|Core Skills|Skill Set|Technical Expertise|Hard Skills|Technical Qualifications|Competencies|Compétence");
        Set<String> experiences = extractSectionItems(extractedText,
                "Experience|Expérience professionnelle|Professional Experience|Work Experience|Employment History|Career History|Work History|Professional Background|Career Summary");
        Set<String> education = extractSectionItems(extractedText,
                "Education|Diplômes|Academic Background|Academic Qualifications|Educational Background|Degrees|Qualifications|Academic History|Formation|Études");
        Set<String> languages = extractSectionItems(extractedText,
                "Languages|Langues|Language Proficiency|Spoken Languages|Language Skills|Linguistic Skills|Langue");
        Set<String> certifications = extractSectionItems(extractedText,
                "Certifications?|Certificats|Professional Certifications|Licenses|Licences|Certificates|Accreditations|Professional Development|Certifié");

        cv.setSkills(skills);
        cv.setExperiences(experiences);
        cv.setEducation(education);
        cv.setLanguages(languages);
        cv.setCertifications(certifications);

        logger.info("Extracted CV Skills: {}", skills);
        logger.info("Extracted CV Experiences: {}", experiences);
        logger.info("Extracted CV Education: {}", education);
        logger.info("Extracted CV Languages: {}", languages);
        logger.info("Extracted CV Certifications: {}", certifications);

        // Save to database
        cvRepository.save(cv);

        // Create a match for this specific job offer
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId).orElse(null);
        CVMatch match = null;
        if (jobOffer != null) {
            match = createCVMatch(cv, jobOffer);
            cvMatchRepository.save(match);
            notificationService.createNotificationsForApplication(candidate, jobOffer, match);
        }

        // Return both the CV and the extracted attributes for comparison/debugging
        Map<String, Object> result = new HashMap<>();
        result.put("cv", cv);
        result.put("skills", skills);
        result.put("experiences", experiences);
        result.put("education", education);
        result.put("languages", languages);
        result.put("certifications", certifications);
        if (match != null) {
            result.put("matchScore", match.getMatchScore());
            result.put("match", match);
        }
        return result;
    }

    private String extractTextFromPDF(byte[] pdfContent) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfContent))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }

    // Helper to extract items from a section (very basic, can be improved)
    private Set<String> extractSectionItems(String text, String sectionHeaderRegex) {
        Set<String> items = new HashSet<>();
        String[] lines = text.split("\r?\n");
        boolean inSection = false;

        // Comprehensive list of headers to stop extraction
        String allHeadersRegex = "(?i)^(Skills|Compétences|Technical Skills|Key Skills|Core Skills|Skill Set|Technical Expertise|Hard Skills|Technical Qualifications|Competencies|Compétence|"
                + "Experience|Expérience professionnelle|Professional Experience|Work Experience|Employment History|Career History|Work History|Professional Background|Career Summary|"
                + "Education|Diplômes|Academic Background|Academic Qualifications|Educational Background|Degrees|Qualifications|Academic History|Formation|Études|"
                + "Languages|Langues|Language Proficiency|Spoken Languages|Language Skills|Linguistic Skills|Langue|"
                + "Certifications?|Certificats|Professional Certifications|Licenses|Licences|Certificates|Accreditations|Professional Development|Certifié)[:：]?(\\s+.*)?$";

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty())
                continue;

            // Check if this line is a header for the section we want
            if (trimmed.matches("(?i).*(" + sectionHeaderRegex + ")[:：]?(\\s+.*)?$")) {
                inSection = true;
                // If there's content on the same line after the header/colon, grab it
                String afterHeader = trimmed.replaceAll("(?i).*(" + sectionHeaderRegex + ")[:：]?", "").trim();
                if (!afterHeader.isEmpty()) {
                    addItemsFromText(items, afterHeader);
                }
                continue;
            }

            // Check if we hit another header - stop if we do
            if (inSection && trimmed.matches(allHeadersRegex)) {
                // Double check it's not the same header repeated or a sub-header we should keep
                if (!trimmed.matches("(?i).*(" + sectionHeaderRegex + ")[:：]?(\\s+.*)?$")) {
                    break;
                }
            }

            if (inSection) {
                addItemsFromText(items, trimmed);
            }
        }
        return items;
    }

    private void addItemsFromText(Set<String> items, String text) {
        // Split by common separators: comma, pipe, slash, semicolon, tab
        String[] parts = text.split("[,|/;\\t]");
        for (String part : parts) {
            // Remove bullet points like -, •, *, numbers like 1., and whitespace
            String item = part.replaceAll("^[-•*\\d.\\s]+", "").trim();
            if (!item.isEmpty() && item.length() < 100) { // Avoid adding entire sentences as one "item"
                items.add(item);
            }
        }
    }

    // Helper to normalize strings for matching
    private String normalize(String s) {
        if (s == null)
            return "";
        // Replace curly quotes/apostrophes with straight ones, em/en dashes with dash
        s = s.replaceAll("[‘’´`ʻʼʽʾʿˈˊˋ˴˵˶˷˸˹˺˻˼˽˾˿’]", "'")
                .replaceAll("[“”«»„‟❝❞❮❯❛❜]", "\"")
                .replaceAll("[–—−]", "-");

        // Handle common variations/aliases
        String result = s.toLowerCase()
                .replaceAll("[-_]+", " ")
                // Preserve + and # for C++, C#, etc.
                .replaceAll("[.,:;()\\[\\]{}|/\\'\"!?@$%^&~`]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // Specific normalizations
        if (result.equals("javascript"))
            return "js";
        if (result.contains("spring boot"))
            result = result.replace("spring boot", "springboot");

        return result;
    }

    // CV Matching
    public List<CVMatch> matchCVWithAllJobs(CV cv) {
        List<JobOffer> jobOffers = jobOfferRepository.findAll();
        List<CVMatch> matches = jobOffers.stream()
                .map(job -> createCVMatch(cv, job))
                .sorted((m1, m2) -> Double.compare(m2.getMatchScore(), m1.getMatchScore()))
                .toList();
        cvMatchRepository.saveAll(matches);
        return matches;
    }

    public void updateMatchesForJob(JobOffer job) {
        List<CVMatch> matches = cvMatchRepository.findByJobOfferId(job.getId());

        for (CVMatch match : matches) {
            CV cv = match.getCv();
            if (cv != null) {
                match.setSkillsMatch(calculateSkillsMatch(cv.getSkills(), job.getRequiredSkills(), cv.getContent()));
                // If logical experience match fails (returns 0), we'll try the raw content
                // fallback in calculateExperienceMatch
                match.setExperienceMatch(
                        calculateExperienceMatch(cv.getExperiences(), job.getExperienceLevel(), cv.getContent()));
                match.setEducationMatch(calculateEducationMatch(cv.getEducation(), job.getRequiredQualifications()));
                match.setLanguageMatch(calculateLanguageMatch(cv.getLanguages(), job.getRequiredLanguages()));
                match.setCertificationMatch(
                        calculateCertificationMatch(cv.getCertifications(), job.getRequiredCertifications()));
                match.setMatchScore(calculateOverallScore(match));
            }
        }
        cvMatchRepository.saveAll(matches);
    }

    private CVMatch createCVMatch(CV cv, JobOffer job) {
        CVMatch match = new CVMatch();
        match.setCv(cv);
        match.setJobOffer(job);

        // Calculate match scores
        match.setSkillsMatch(calculateSkillsMatch(cv.getSkills(), job.getRequiredSkills(), cv.getContent()));
        match.setExperienceMatch(
                calculateExperienceMatch(cv.getExperiences(), job.getExperienceLevel(), cv.getContent()));
        match.setEducationMatch(calculateEducationMatch(cv.getEducation(), job.getRequiredQualifications()));
        match.setLanguageMatch(calculateLanguageMatch(cv.getLanguages(), job.getRequiredLanguages()));
        match.setCertificationMatch(
                calculateCertificationMatch(cv.getCertifications(), job.getRequiredCertifications()));

        // Calculate overall match score
        match.setMatchScore(calculateOverallScore(match));
        match.setMatchDate(new Date());

        return match;
    }

    public double recalculateScore(CVMatch match) {
        // Targeted Weights: Technical Skills (40%), Professional Experience (30%),
        // Education/Qualifications (20%), and Certifications/Languages (10%).
        double skillScore = match.getSkillsMatch() * 0.40;
        double expScore = match.getExperienceMatch() * 0.30;
        double eduScore = match.getEducationMatch() * 0.20;
        double langCertScore = ((match.getLanguageMatch() + match.getCertificationMatch()) / 2.0) * 0.10;

        return skillScore + expScore + eduScore + langCertScore;
    }

    private double calculateOverallScore(CVMatch match) {
        return recalculateScore(match);
    }

    // Helper methods for scoring
    private int calculateSkillsMatch(Set<String> cvSkills, Set<String> jobSkills, String rawContent) {
        logger.info("Comparing CV Skills with Job Skills: {}", jobSkills);
        if (jobSkills == null || jobSkills.isEmpty())
            return 0;

        int matched = 0;
        String normRawContent = normalize(rawContent);

        for (String skill : jobSkills) {
            String normSkill = normalize(skill);
            boolean found = false;

            // 1. Try matching in extracted skills set
            if (cvSkills != null && !cvSkills.isEmpty()) {
                if (cvSkills.stream()
                        .anyMatch(s -> normalize(s).contains(normSkill) || normSkill.contains(normalize(s)))) {
                    found = true;
                }
            }

            // 2. Fallback: Search in raw content
            if (!found && !normRawContent.isEmpty()) {
                if (normRawContent.contains(normSkill)) {
                    found = true;
                    logger.info("Skill '{}' found in raw content fallback", skill);
                }
            }

            if (found)
                matched++;
        }
        logger.info("Matched Skills: {} out of {}", matched, jobSkills.size());
        return (int) ((matched * 100.0) / jobSkills.size());
    }

    private int calculateExperienceMatch(Set<String> cvExperiences, String jobExperienceLevel, String rawContent) {
        logger.info("Comparing CV Experience with Job Level: {}", jobExperienceLevel);
        if (jobExperienceLevel == null || jobExperienceLevel.isEmpty())
            return 0;

        int finalScore = 0;

        // Level 1: Logical matching if we have experience items
        if (cvExperiences != null && !cvExperiences.isEmpty()) {
            finalScore = performLogicalExperienceMatch(cvExperiences, jobExperienceLevel);
            if (finalScore > 0) {
                logger.info("Experience logically matched with score: {}", finalScore);
                return finalScore;
            }
        }

        // Level 2: Fallback - Keyword density in Experience section
        if (cvExperiences != null && !cvExperiences.isEmpty()) {
            String normLevel = normalize(jobExperienceLevel);
            String combinedExp = cvExperiences.stream()
                    .map(this::normalize)
                    .collect(java.util.stream.Collectors.joining(" "));

            finalScore = calculateKeywordScore(normLevel, combinedExp);
            if (finalScore >= 40) {
                logger.info("Experience matched via section keyword density: {}%", finalScore);
                return finalScore;
            }
        }

        // Level 3: Final Fallback - Search raw document content
        String normLevel = normalize(jobExperienceLevel);
        String normRaw = normalize(rawContent);
        finalScore = calculateKeywordScore(normLevel, normRaw);

        if (finalScore > 0) {
            logger.info("Experience matched via raw content fallback: {}%", finalScore);
            return finalScore;
        }

        return 0;
    }

    private int calculateKeywordScore(String target, String source) {
        if (target == null || source == null || target.isEmpty())
            return 0;
        String[] words = target.split("\\s+");
        int wordMatches = 0;
        int meaningfulWords = 0;

        for (String w : words) {
            // Include numbers (like '5') for experience matching as they are critical
            if (w.length() >= 1 && (Character.isDigit(w.charAt(0)) || w.length() > 2)) {
                meaningfulWords++;
                if (source.contains(w)) {
                    wordMatches++;
                }
            }
        }

        if (meaningfulWords > 0) {
            return Math.min(100, (int) ((wordMatches * 100.0) / meaningfulWords));
        }
        return 0;
    }

    private int performLogicalExperienceMatch(Set<String> cvExperiences, String jobExperienceLevel) {
        String normLevel = normalize(jobExperienceLevel);

        // 1. Try direct keyword matching first (Senior, Junior, Mid, etc.)
        String[] levelKeywords = { "junior", "senior", "mid", "lead", "principal", "intern", "entry",
                "intermediate", "staff", "associate", "manager", "director" };
        for (String keyword : levelKeywords) {
            if (normLevel.contains(keyword)) {
                for (String exp : cvExperiences) {
                    if (normalize(exp).contains(keyword)) {
                        logger.info("Experience keyword matched '{}' in: {}", keyword, exp);
                        return 100;
                    }
                }
            }
        }

        // 2. Extract required years from job level
        int requiredYears = extractYears(jobExperienceLevel);
        if (requiredYears == 0)
            return 0; // If no years required, we can't do numeric matching

        // 3. Estimate candidate's total years from CV
        int totalCvYears = 0;

        // A. Look for explicit durations like "(4 years)", "Experience: 5 ans"
        java.util.regex.Pattern durationPattern = java.util.regex.Pattern
                .compile("(\\d+)\\s*(?:years?|ans?|yrs?|mois|months?)", java.util.regex.Pattern.CASE_INSENSITIVE);

        // B. Look for date ranges (more flexible: handle dots, slashes, months)
        java.util.regex.Pattern dateRange = java.util.regex.Pattern
                .compile(
                        "(\\d{4}|\\d{1,2}[./]\\d{4})\\s*[-–—to]+\\s*(\\d{4}|\\d{1,2}[./]\\d{4}|[Pp]resent|[Aa]ctuel|[Nn]ow|[Cc]urrent)",
                        java.util.regex.Pattern.CASE_INSENSITIVE);

        for (String exp : cvExperiences) {
            // Try duration extraction first
            java.util.regex.Matcher dm = durationPattern.matcher(exp);
            while (dm.find()) {
                int val = Integer.parseInt(dm.group(1));
                String unit = dm.group(0).toLowerCase();
                if (unit.contains("mois") || unit.contains("month")) {
                    totalCvYears += Math.max(0, val / 12);
                } else {
                    totalCvYears += val;
                }
            }

            // Try date range extraction
            java.util.regex.Matcher m = dateRange.matcher(exp);
            while (m.find()) {
                String startGroup = m.group(1);
                String endGroup = m.group(2);

                int start = extractYearFromGroup(startGroup);
                int end = extractYearFromGroup(endGroup);

                if (start > 0 && end > 0) {
                    totalCvYears += Math.max(0, end - start);
                }
            }
        }

        logger.info("Extracted CV total years: {}, Required years: {}", totalCvYears, requiredYears);

        if (totalCvYears > 0) {
            int score = Math.min(100, (int) ((totalCvYears * 100.0) / requiredYears));
            logger.info("Experience score by years: {}", score);
            return score;
        }

        return 0;
    }

    private int extractYearFromGroup(String group) {
        if (group.matches("(?i)present|actuel|now|current")) {
            return java.time.Year.now().getValue();
        }
        // Handle YYYY
        if (group.matches("\\d{4}")) {
            return Integer.parseInt(group);
        }
        // Handle MM.YYYY or MM/YYYY
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2})[./](\\d{4})").matcher(group);
        if (m.find()) {
            return Integer.parseInt(m.group(2));
        }
        return 0;
    }

    /**
     * Extract a number of years from a string like "5+ years", "3-5 years", "5 ans"
     */
    private int extractYears(String text) {
        // Match patterns like "5+", "5 years", "3-5 years", "5 ans"
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d+)\\s*[+]?\\s*(?:years?|ans?|yrs?)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        // Also match just a bare number followed by + (e.g. "5+")
        m = java.util.regex.Pattern.compile("(\\d+)\\s*\\+").matcher(text);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    private int calculateEducationMatch(Set<String> cvEducation, Set<String> jobQualifications) {
        logger.info("Comparing CV Education: {} with Job Qualifications: {}", cvEducation, jobQualifications);
        if (cvEducation == null || jobQualifications == null || jobQualifications.isEmpty())
            return 0;
        int matched = 0;

        // Extract degree keywords from CV education lines using multiple patterns
        Set<String> degreeNames = new HashSet<>();
        // Common degree patterns to recognize
        String degreeRegex = "(?i)("
                + "(?:bachelor|master|doctor|phd|ph\\.d|bsc|msc|mba|b\\.?s\\.?|m\\.?s\\.?|b\\.?a\\.?|m\\.?a\\.?|"
                + "licence|licencié|ingénieur|ingenieur|diplôme|diplome|baccalaur[ée]at|bac|"
                + "associate|postgraduate|undergraduate|dut|bts|deug|magist[eè]re|doctorat)"
                + "[a-zA-ZÀ-ÿ''\\s.]*(?:of|in|en|de|d')?[a-zA-ZÀ-ÿ''\\s.]*)";
        java.util.regex.Pattern degreePattern = java.util.regex.Pattern.compile(degreeRegex);

        for (String edu : cvEducation) {
            // Try to extract degree with the flexible pattern
            java.util.regex.Matcher m = degreePattern.matcher(edu);
            if (m.find()) {
                String extracted = m.group(1).trim();
                logger.info("Extracted degree: '{}' from '{}'", extracted, edu);
                degreeNames.add(extracted);
            }
            // Also add the raw line (cleaned) for fallback matching
            degreeNames.add(edu.split("[,\\-\\(]")[0].trim());
        }

        logger.info("All extracted degree names: {}", degreeNames);

        for (String qual : jobQualifications) {
            String normQual = normalize(qual);
            logger.info("Comparing job qualification: '{}' (normalized: '{}')", qual, normQual);

            // Check if any degree name matches the qualification
            boolean found = false;
            for (String degree : degreeNames) {
                String normDegree = normalize(degree);
                // Full or partial containment
                if (normDegree.contains(normQual) || normQual.contains(normDegree)) {
                    found = true;
                    break;
                }
                // Word-level overlap: check if key words from the qualification appear in the
                // degree
                String[] qualWords = normQual.split("\\s+");
                int wordHits = 0;
                for (String w : qualWords) {
                    if (w.length() > 2 && normDegree.contains(w))
                        wordHits++;
                }
                if (qualWords.length > 0 && wordHits >= Math.max(1, qualWords.length / 2)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                matched++;
                logger.info("Qualification matched: {}", qual);
            }
        }
        logger.info("Matched Qualifications: {} out of {}", matched, jobQualifications.size());
        return (int) ((matched * 100.0) / jobQualifications.size());
    }

    private int calculateLanguageMatch(Set<String> cvLanguages, Set<String> jobLanguages) {
        logger.info("Comparing CV Languages: {} with Job Languages: {}", cvLanguages, jobLanguages);
        if (cvLanguages == null || jobLanguages == null || jobLanguages.isEmpty())
            return 0;
        int matched = 0;
        for (String lang : jobLanguages) {
            String normLang = normalize(lang);
            if (cvLanguages.stream()
                    .anyMatch(l -> normalize(l).contains(normLang) || normLang.contains(normalize(l)))) {
                matched++;
            }
        }
        logger.info("Matched Languages: {} out of {}", matched, jobLanguages.size());
        return (int) ((matched * 100.0) / jobLanguages.size());
    }

    private int calculateCertificationMatch(Set<String> cvCertifications, Set<String> jobCertifications) {
        logger.info("Comparing CV Certifications: {} with Job Certifications: {}", cvCertifications, jobCertifications);
        if (cvCertifications == null || jobCertifications == null || jobCertifications.isEmpty())
            return 0;
        int matched = 0;
        for (String cert : jobCertifications) {
            String normCert = normalize(cert);
            if (cvCertifications.stream()
                    .anyMatch(c -> normalize(c).contains(normCert) || normCert.contains(normalize(c)))) {
                matched++;
            }
        }
        logger.info("Matched Certifications: {} out of {}", matched, jobCertifications.size());
        return (int) ((matched * 100.0) / jobCertifications.size());
    }
}
