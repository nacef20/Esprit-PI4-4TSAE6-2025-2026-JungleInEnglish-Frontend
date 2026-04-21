package tn.esprit.quiz_microservice.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tn.esprit.quiz_microservice.entities.*;
import tn.esprit.quiz_microservice.repositories.AttemptAnswerRepository;
import tn.esprit.quiz_microservice.repositories.AttemptRepository;
import tn.esprit.quiz_microservice.repositories.CertificateRepository;
import tn.esprit.quiz_microservice.repositories.ChoiceRepository;
import tn.esprit.quiz_microservice.repositories.QuestionRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttemptPdfService {

    private final AttemptRepository attemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final CertificateRepository certificateRepository;

    // Brand colors
    private static final DeviceRgb PRIMARY_GREEN = new DeviceRgb(14, 163, 122);    // #0ea37a
    private static final DeviceRgb DARK_GREEN = new DeviceRgb(6, 95, 70);          // #065f46
    private static final DeviceRgb LIGHT_GREEN_BG = new DeviceRgb(220, 252, 231);  // #dcfce7
    private static final DeviceRgb GREEN_BORDER = new DeviceRgb(134, 239, 172);    // #86efac
    private static final DeviceRgb GREEN_TEXT = new DeviceRgb(21, 128, 61);         // #15803d
    private static final DeviceRgb LIGHT_RED_BG = new DeviceRgb(254, 226, 226);    // #fee2e2
    private static final DeviceRgb RED_BORDER = new DeviceRgb(252, 165, 165);      // #fca5a5
    private static final DeviceRgb RED_TEXT = new DeviceRgb(185, 28, 28);           // #b91c1c
    private static final DeviceRgb DARK_TEXT = new DeviceRgb(31, 41, 55);           // #1f2937
    private static final DeviceRgb GRAY_TEXT = new DeviceRgb(107, 114, 128);        // #6b7280
    private static final DeviceRgb LIGHT_GRAY_BG = new DeviceRgb(249, 250, 251);   // #f9fafb
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb CARD_BORDER = new DeviceRgb(229, 231, 235);     // #e5e7eb

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    public byte[] generateAttemptPdf(Long attemptId) throws IOException {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

        List<AttemptAnswer> answers = attemptAnswerRepository.findByAttemptId(attemptId);
        List<Question> questions = attempt.getQuiz() != null
                ? questionRepository.findByQuizId(attempt.getQuiz().getId())
                : List.of();

        // Map answers by question id
        Map<Long, AttemptAnswer> answerByQuestion = answers.stream()
                .filter(a -> a.getQuestion() != null && a.getQuestion().getId() != null)
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (a, b) -> a));

        // Map choices by question id
        Map<Long, List<Choice>> choicesByQuestion = questions.stream()
                .collect(Collectors.toMap(
                        Question::getId,
                        q -> choiceRepository.findByQuestionId(q.getId())
                ));

        // Calculate score
        float score = attempt.getScore() != null ? attempt.getScore() : 0;
        int totalPoints = attempt.getTotalPoints() != null ? attempt.getTotalPoints() : 0;
        int percentage = totalPoints > 0 ? Math.round(score * 100f / totalPoints) : 0;
        boolean passed = percentage >= 50;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc, PageSize.A4);
        doc.setMargins(30, 40, 40, 40);

        // ─── HEADER WITH LOGO ───
        addHeader(doc);

        // ─── DOCUMENT TITLE ───
        doc.add(new Paragraph("Quiz Report")
                .setFontSize(22)
                .setBold()
                .setFontColor(DARK_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setMarginBottom(20));

        // ─── SUMMARY CARD ───
        addSummaryCard(doc, attempt, score, totalPoints, percentage, passed);

        // ─── QUESTIONS & ANSWERS ───
        doc.add(new Paragraph("Questions & Answers")
                .setFontSize(16)
                .setBold()
                .setFontColor(DARK_TEXT)
                .setMarginTop(25)
                .setMarginBottom(15));

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            AttemptAnswer answer = answerByQuestion.get(question.getId());
            List<Choice> choices = choicesByQuestion.getOrDefault(question.getId(), List.of());
            boolean questionCorrect = isQuestionCorrect(answer, choices);

            addQuestionBlock(doc, question, choices, answer, i + 1, questionCorrect);
        }

        // ─── FOOTER ───
        doc.add(new Paragraph("\n"));
        addFooter(doc);

        doc.close();
        return baos.toByteArray();
    }

    private void addHeader(Document doc) throws IOException {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 4}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);

        // Logo
        try {
            ClassPathResource logoResource = new ClassPathResource("static/logo.jpg");
            byte[] logoBytes = logoResource.getInputStream().readAllBytes();
            Image logo = new Image(ImageDataFactory.create(logoBytes));
            logo.setWidth(60).setHeight(60);
            Cell logoCell = new Cell().add(logo)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            headerTable.addCell(logoCell);
        } catch (Exception e) {
            // If logo not found, add empty cell
            headerTable.addCell(new Cell().add(new Paragraph(""))
                    .setBorder(Border.NO_BORDER));
        }

        // Brand name
        Paragraph brandName = new Paragraph("Jungle In English")
                .setFontSize(24)
                .setBold()
                .setFontColor(PRIMARY_GREEN);
        Paragraph tagline = new Paragraph("Learn English Through Nature")
                .setFontSize(10)
                .setFontColor(GRAY_TEXT);

        Cell textCell = new Cell()
                .add(brandName)
                .add(tagline)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        headerTable.addCell(textCell);

        doc.add(headerTable);

        // Divider line
        Table divider = new Table(1).useAllAvailableWidth()
                .setBorderBottom(new SolidBorder(PRIMARY_GREEN, 2))
                .setMarginBottom(5);
        divider.addCell(new Cell().setBorder(Border.NO_BORDER).setHeight(1));
        doc.add(divider);
    }

    private void addSummaryCard(Document doc, Attempt attempt, float score, int totalPoints,
                                 int percentage, boolean passed) {
        // Outer card
        Table card = new Table(1).useAllAvailableWidth()
                .setBorder(new SolidBorder(CARD_BORDER, 1))
                .setBackgroundColor(WHITE)
                .setMarginBottom(5);

        // Quiz title row
        String quizTitle = attempt.getQuiz() != null ? attempt.getQuiz().getTitle() : "Unknown Quiz";
        String studentName = attempt.getStudentName() != null ? attempt.getStudentName() : "Student #" + attempt.getStudentId();
        String dateStr = attempt.getSubmittedAt() != null ? attempt.getSubmittedAt().format(DATE_FMT) : "—";

        // Info section
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);

        // Left side - Quiz info
        Cell infoLeft = new Cell().setBorder(Border.NO_BORDER).setPadding(15);
        infoLeft.add(new Paragraph("Quiz").setFontSize(9).setFontColor(GRAY_TEXT));
        infoLeft.add(new Paragraph(quizTitle).setFontSize(14).setBold().setFontColor(DARK_TEXT).setMarginBottom(8));
        infoLeft.add(new Paragraph("Student").setFontSize(9).setFontColor(GRAY_TEXT));
        infoLeft.add(new Paragraph(studentName).setFontSize(12).setFontColor(DARK_TEXT).setMarginBottom(8));
        infoLeft.add(new Paragraph("Submitted").setFontSize(9).setFontColor(GRAY_TEXT));
        infoLeft.add(new Paragraph(dateStr).setFontSize(11).setFontColor(DARK_TEXT));
        infoTable.addCell(infoLeft);

        // Right side - Score
        DeviceRgb scoreBg = passed ? LIGHT_GREEN_BG : LIGHT_RED_BG;
        DeviceRgb scoreBorder = passed ? GREEN_BORDER : RED_BORDER;
        DeviceRgb scoreColor = passed ? GREEN_TEXT : RED_TEXT;

        Cell infoRight = new Cell().setBorder(Border.NO_BORDER).setPadding(15)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        // Score box
        Table scoreBox = new Table(1)
                .setWidth(UnitValue.createPercentValue(70))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setBackgroundColor(scoreBg)
                .setBorder(new SolidBorder(scoreBorder, 1.5f));

        Cell scoreContent = new Cell().setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);
        scoreContent.add(new Paragraph(String.format("%.0f / %d", score, totalPoints))
                .setFontSize(22).setBold().setFontColor(scoreColor));
        scoreContent.add(new Paragraph(percentage + "%")
                .setFontSize(14).setFontColor(scoreColor).setMarginTop(2));
        scoreContent.add(new Paragraph(passed ? "PASSED" : "FAILED")
                .setFontSize(12).setBold().setFontColor(scoreColor).setMarginTop(5));
        scoreBox.addCell(scoreContent);

        infoRight.add(scoreBox);
        infoTable.addCell(infoRight);

        card.addCell(new Cell().add(infoTable).setBorder(Border.NO_BORDER));
        doc.add(card);
    }

    private void addQuestionBlock(Document doc, Question question, List<Choice> choices,
                                   AttemptAnswer answer, int questionNumber, boolean correct) {
        DeviceRgb headerBg = correct ? LIGHT_GREEN_BG : LIGHT_RED_BG;
        DeviceRgb borderColor = correct ? GREEN_BORDER : RED_BORDER;
        DeviceRgb statusColor = correct ? GREEN_TEXT : RED_TEXT;

        Table questionCard = new Table(1).useAllAvailableWidth()
                .setBorder(new SolidBorder(borderColor, 1))
                .setMarginBottom(12);

        // Question header
        Table headerRow = new Table(UnitValue.createPercentArray(new float[]{5, 1}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(headerBg);

        // Question text
        Cell qTextCell = new Cell().setBorder(Border.NO_BORDER).setPadding(10);
        Paragraph qText = new Paragraph()
                .add(new Text("Q" + questionNumber + "  ").setBold().setFontSize(11).setFontColor(statusColor))
                .add(new Text(question.getContent()).setFontSize(11).setFontColor(DARK_TEXT));
        qTextCell.add(qText);
        headerRow.addCell(qTextCell);

        // Points + status
        Cell statusCell = new Cell().setBorder(Border.NO_BORDER).setPadding(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        statusCell.add(new Paragraph(question.getPoints() + " pts")
                .setFontSize(9).setFontColor(GRAY_TEXT));
        statusCell.add(new Paragraph(correct ? "✓ Correct" : "✗ Wrong")
                .setFontSize(10).setBold().setFontColor(statusColor));
        headerRow.addCell(statusCell);

        questionCard.addCell(new Cell().add(headerRow).setBorder(Border.NO_BORDER));

        // Choices
        Cell choicesCell = new Cell().setBorder(Border.NO_BORDER).setPadding(10);

        for (Choice choice : choices) {
            boolean isSelected = answer != null
                    && answer.getSelectedChoice() != null
                    && answer.getSelectedChoice().getId() != null
                    && answer.getSelectedChoice().getId().equals(choice.getId());
            boolean isCorrectChoice = choice.getIsCorrect() != null && choice.getIsCorrect();

            Table choiceRow = new Table(UnitValue.createPercentArray(new float[]{0.5f, 8, 2}))
                    .useAllAvailableWidth()
                    .setMarginBottom(4);

            // Determine choice styling
            DeviceRgb choiceBg;
            DeviceRgb choiceBorderColor;
            if (isCorrectChoice) {
                choiceBg = LIGHT_GREEN_BG;
                choiceBorderColor = GREEN_BORDER;
            } else if (isSelected) {
                choiceBg = LIGHT_RED_BG;
                choiceBorderColor = RED_BORDER;
            } else {
                choiceBg = LIGHT_GRAY_BG;
                choiceBorderColor = CARD_BORDER;
            }

            // Bullet
            String bullet = isSelected ? "●" : "○";
            DeviceRgb bulletColor = isCorrectChoice ? GREEN_TEXT : (isSelected ? RED_TEXT : GRAY_TEXT);
            Cell bulletCell = new Cell().setBorder(new SolidBorder(choiceBorderColor, 0.5f))
                    .setBackgroundColor(choiceBg)
                    .setPadding(6)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            bulletCell.add(new Paragraph(bullet).setFontSize(10).setFontColor(bulletColor));
            choiceRow.addCell(bulletCell);

            // Choice text
            Cell textCell = new Cell().setBorder(new SolidBorder(choiceBorderColor, 0.5f))
                    .setBackgroundColor(choiceBg)
                    .setPadding(6)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            textCell.add(new Paragraph(choice.getContent()).setFontSize(10).setFontColor(DARK_TEXT));
            choiceRow.addCell(textCell);

            // Tag
            Cell tagCell = new Cell().setBorder(new SolidBorder(choiceBorderColor, 0.5f))
                    .setBackgroundColor(choiceBg)
                    .setPadding(6)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);

            if (isSelected && !isCorrectChoice) {
                tagCell.add(new Paragraph("Your answer").setFontSize(8).setFontColor(RED_TEXT));
            } else if (isCorrectChoice) {
                String tag = isSelected ? "Correct ✓" : "Correct answer";
                tagCell.add(new Paragraph(tag).setFontSize(8).setFontColor(GREEN_TEXT));
            } else {
                tagCell.add(new Paragraph("").setFontSize(8));
            }
            choiceRow.addCell(tagCell);

            choicesCell.add(choiceRow);
        }

        // No answer selected
        if (answer == null || answer.getSelectedChoice() == null) {
            choicesCell.add(new Paragraph("No answer selected")
                    .setFontSize(9).setItalic().setFontColor(GRAY_TEXT).setMarginTop(5));
        }

        questionCard.addCell(choicesCell);
        doc.add(questionCard);
    }

    private void addFooter(Document doc) {
        Table divider = new Table(1).useAllAvailableWidth()
                .setBorderTop(new SolidBorder(PRIMARY_GREEN, 1.5f))
                .setMarginBottom(8);
        divider.addCell(new Cell().setBorder(Border.NO_BORDER).setHeight(1));
        doc.add(divider);

        doc.add(new Paragraph("Generated by Jungle In English — Quiz Microservice")
                .setFontSize(9)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("© 2026 Jungle In English. All rights reserved.")
                .setFontSize(8)
                .setFontColor(new DeviceRgb(156, 163, 175))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(2));
    }

    private boolean isQuestionCorrect(AttemptAnswer answer, List<Choice> choices) {
        if (answer == null || answer.getSelectedChoice() == null || answer.getSelectedChoice().getId() == null) {
            return false;
        }
        return choices.stream()
                .filter(c -> c.getId().equals(answer.getSelectedChoice().getId()))
                .findFirst()
                .map(c -> c.getIsCorrect() != null && c.getIsCorrect())
                .orElse(false);
    }

    // ═══════════════════════════════════════════════════════════════
    //  ATTEMPTS LIST PDF EXPORT
    // ═══════════════════════════════════════════════════════════════

    public byte[] generateAttemptsListPdf(Long quizId, String quizTitle, String studentName) throws IOException {
        // Fetch filtered attempts (all, no pagination)
        List<Attempt> attempts;
        boolean hasTitle = quizTitle != null && !quizTitle.isEmpty();
        boolean hasName = studentName != null && !studentName.isEmpty();

        if (hasTitle && hasName) {
            attempts = attemptRepository.findByQuizTitleAndStudentNameList(quizTitle, studentName);
        } else if (hasTitle && quizId != null) {
            attempts = attemptRepository.findByQuizTitleContainingAndQuizIdList(quizTitle, quizId);
        } else if (hasTitle) {
            attempts = attemptRepository.findByQuizTitleContainingList(quizTitle);
        } else if (hasName && quizId != null) {
            attempts = attemptRepository.findByStudentNameContainingAndQuizIdList(studentName, quizId);
        } else if (hasName) {
            attempts = attemptRepository.findByStudentNameContainingList(studentName);
        } else if (quizId != null) {
            attempts = attemptRepository.findByQuizIdOrderBySubmittedAtDesc(quizId);
        } else {
            attempts = attemptRepository.findAllByOrderBySubmittedAtDesc();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc, PageSize.A4.rotate()); // Landscape for table
        doc.setMargins(25, 30, 30, 30);

        // ─── HEADER ───
        addHeader(doc);

        // ─── TITLE ───
        String title = "Attempts Report";
        if (quizId != null || hasTitle || hasName) {
            StringBuilder sb = new StringBuilder("Attempts Report");
            if (hasTitle) sb.append(" — Quiz: \"").append(quizTitle).append("\"");
            if (hasName) sb.append(" — Student: \"").append(studentName).append("\"");
            title = sb.toString();
        }
        doc.add(new Paragraph(title)
                .setFontSize(18)
                .setBold()
                .setFontColor(DARK_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(8)
                .setMarginBottom(5));

        // ─── SUMMARY KPI ROW ───
        addListKpiRow(doc, attempts);

        // ─── GENERATED AT ───
        doc.add(new Paragraph("Generated on " + LocalDateTime.now().format(DATE_FMT))
                .setFontSize(9)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(10));

        // ─── TABLE ───
        addAttemptsTable(doc, attempts);

        // ─── FOOTER ───
        doc.add(new Paragraph("\n"));
        addFooter(doc);

        doc.close();
        return baos.toByteArray();
    }

    private void addListKpiRow(Document doc, List<Attempt> attempts) {
        int total = attempts.size();
        double avgScore = attempts.stream()
                .filter(a -> a.getTotalPoints() != null && a.getTotalPoints() > 0)
                .mapToDouble(a -> a.getScore() * 100.0 / a.getTotalPoints())
                .average().orElse(0);
        long passedCount = attempts.stream()
                .filter(a -> a.getTotalPoints() != null && a.getTotalPoints() > 0)
                .filter(a -> (a.getScore() * 100.0 / a.getTotalPoints()) >= 50)
                .count();
        double passRate = total > 0 ? (passedCount * 100.0 / total) : 0;

        Table kpiTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(10);

        // Total Attempts
        kpiTable.addCell(createKpiCell("Total Attempts", String.valueOf(total), PRIMARY_GREEN));
        // Average Score
        kpiTable.addCell(createKpiCell("Average Score", Math.round(avgScore) + "%", PRIMARY_GREEN));
        // Pass Rate
        DeviceRgb prColor = passRate >= 50 ? GREEN_TEXT : RED_TEXT;
        kpiTable.addCell(createKpiCell("Pass Rate", Math.round(passRate) + "%", prColor));
        // Passed / Failed
        kpiTable.addCell(createKpiCell("Passed / Failed", passedCount + " / " + (total - passedCount), PRIMARY_GREEN));

        doc.add(kpiTable);
    }

    private Cell createKpiCell(String label, String value, DeviceRgb valueColor) {
        Cell cell = new Cell()
                .setBorder(new SolidBorder(CARD_BORDER, 0.5f))
                .setBackgroundColor(LIGHT_GRAY_BG)
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph(label).setFontSize(8).setFontColor(GRAY_TEXT).setMarginBottom(2));
        cell.add(new Paragraph(value).setFontSize(18).setBold().setFontColor(valueColor));
        return cell;
    }

    private static final DeviceRgb GOLD_BG = new DeviceRgb(254, 243, 199);       // #fef3c7
    private static final DeviceRgb GOLD_TEXT = new DeviceRgb(146, 64, 14);        // #92400e
    private static final DeviceRgb GOLD_BORDER = new DeviceRgb(252, 211, 77);     // #fcd34d

    private void addAttemptsTable(Document doc, List<Attempt> attempts) {
        // Column widths: #, Quiz, Student, Score, %, Result, Certificate, Date
        Table table = new Table(UnitValue.createPercentArray(new float[]{0.5f, 2.2f, 1.8f, 1.2f, 0.8f, 1f, 1.5f, 2f}))
                .useAllAvailableWidth()
                .setBorder(new SolidBorder(CARD_BORDER, 0.5f));

        // ─── HEADER ROW ───
        DeviceRgb headerBg = new DeviceRgb(243, 244, 246); // #f3f4f6
        String[] headers = {"#", "QUIZ", "STUDENT", "SCORE", "%", "RESULT", "CERTIFICATE", "DATE"};
        for (String h : headers) {
            Cell headerCell = new Cell()
                    .setBackgroundColor(headerBg)
                    .setBorder(new SolidBorder(CARD_BORDER, 0.5f))
                    .setPadding(8);
            headerCell.add(new Paragraph(h).setFontSize(8).setBold()
                    .setFontColor(GRAY_TEXT).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(headerCell);
        }

        // ─── DATA ROWS ───
        DeviceRgb altRowBg = new DeviceRgb(249, 250, 251); // #f9fafb
        for (int i = 0; i < attempts.size(); i++) {
            Attempt a = attempts.get(i);
            DeviceRgb rowBg = i % 2 == 0 ? WHITE : altRowBg;

            float score = a.getScore() != null ? a.getScore() : 0;
            int totalPts = a.getTotalPoints() != null ? a.getTotalPoints() : 0;
            int pct = totalPts > 0 ? Math.round(score * 100f / totalPts) : 0;
            boolean passed = pct >= 50;
            String quizName = a.getQuiz() != null ? a.getQuiz().getTitle() : "Unknown";
            String student = a.getStudentName() != null ? a.getStudentName() : "Student #" + a.getStudentId();
            String dateStr = a.getSubmittedAt() != null ? a.getSubmittedAt().format(DATE_FMT) : "—";

            // # column
            table.addCell(createDataCell(String.valueOf(i + 1), GRAY_TEXT, rowBg, TextAlignment.CENTER));

            // Quiz
            Cell quizCell = new Cell()
                    .setBorder(new SolidBorder(CARD_BORDER, 0.5f))
                    .setBackgroundColor(rowBg)
                    .setPadding(7);
            quizCell.add(new Paragraph(quizName).setFontSize(9).setBold().setFontColor(DARK_TEXT));
            if (a.getQuiz() != null) {
                quizCell.add(new Paragraph("ID: " + a.getQuiz().getId()).setFontSize(7).setFontColor(GRAY_TEXT));
            }
            table.addCell(quizCell);

            // Student
            table.addCell(createDataCell(student, DARK_TEXT, rowBg, TextAlignment.LEFT));

            // Score
            DeviceRgb scoreColor = pct >= 80 ? GREEN_TEXT : (pct >= 50 ? new DeviceRgb(202, 138, 4) : RED_TEXT);
            Cell scoreCell = new Cell()
                    .setBorder(new SolidBorder(CARD_BORDER, 0.5f))
                    .setBackgroundColor(rowBg)
                    .setPadding(7)
                    .setTextAlignment(TextAlignment.CENTER);
            scoreCell.add(new Paragraph(String.format("%.0f/%d", score, totalPts))
                    .setFontSize(10).setBold().setFontColor(scoreColor));
            table.addCell(scoreCell);

            // %
            DeviceRgb pctBg = pct >= 80 ? LIGHT_GREEN_BG : (pct >= 50 ? new DeviceRgb(254, 249, 195) : LIGHT_RED_BG);
            Cell pctCell = new Cell()
                    .setBorder(new SolidBorder(CARD_BORDER, 0.5f))
                    .setBackgroundColor(pctBg)
                    .setPadding(7)
                    .setTextAlignment(TextAlignment.CENTER);
            pctCell.add(new Paragraph(pct + "%").setFontSize(9).setBold().setFontColor(scoreColor));
            table.addCell(pctCell);

            // Result
            DeviceRgb resultBg = passed ? LIGHT_GREEN_BG : LIGHT_RED_BG;
            DeviceRgb resultColor = passed ? GREEN_TEXT : RED_TEXT;
            Cell resultCell = new Cell()
                    .setBorder(new SolidBorder(CARD_BORDER, 0.5f))
                    .setBackgroundColor(resultBg)
                    .setPadding(7)
                    .setTextAlignment(TextAlignment.CENTER);
            resultCell.add(new Paragraph(passed ? "Passed" : "Failed")
                    .setFontSize(9).setBold().setFontColor(resultColor));
            table.addCell(resultCell);

            // Certificate
            String certGrade = certificateRepository.findByAttemptId(a.getId())
                    .map(Certificate::getGrade)
                    .orElse(null);
            if (certGrade != null) {
                Cell certCell = new Cell()
                        .setBorder(new SolidBorder(GOLD_BORDER, 0.5f))
                        .setBackgroundColor(GOLD_BG)
                        .setPadding(7)
                        .setTextAlignment(TextAlignment.CENTER);
                certCell.add(new Paragraph(certGrade)
                        .setFontSize(9).setBold().setFontColor(GOLD_TEXT));
                table.addCell(certCell);
            } else {
                table.addCell(createDataCell("None", GRAY_TEXT, rowBg, TextAlignment.CENTER));
            }

            // Date
            table.addCell(createDataCell(dateStr, GRAY_TEXT, rowBg, TextAlignment.CENTER));
        }

        doc.add(table);

        // Total row count
        doc.add(new Paragraph(attempts.size() + " attempt(s) exported")
                .setFontSize(8)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(5));
    }

    private Cell createDataCell(String text, DeviceRgb color, DeviceRgb bg, TextAlignment align) {
        Cell cell = new Cell()
                .setBorder(new SolidBorder(CARD_BORDER, 0.5f))
                .setBackgroundColor(bg)
                .setPadding(7)
                .setTextAlignment(align);
        cell.add(new Paragraph(text).setFontSize(9).setFontColor(color));
        return cell;
    }
}
