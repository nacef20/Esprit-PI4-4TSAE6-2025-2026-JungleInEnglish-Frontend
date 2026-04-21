package tn.esprit.quiz_microservice.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DashedBorder;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tn.esprit.quiz_microservice.entities.Attempt;
import tn.esprit.quiz_microservice.entities.Certificate;
import tn.esprit.quiz_microservice.repositories.AttemptRepository;
import tn.esprit.quiz_microservice.repositories.CertificateRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final AttemptRepository attemptRepository;
    private final CertificateRepository certificateRepository;

    // Brand colors
    private static final DeviceRgb PRIMARY_GREEN = new DeviceRgb(14, 163, 122);      // #0ea37a
    private static final DeviceRgb DARK_GREEN = new DeviceRgb(6, 95, 70);            // #065f46
    private static final DeviceRgb LIGHT_GREEN_BG = new DeviceRgb(236, 253, 245);    // #ecfdf5
    private static final DeviceRgb GREEN_BORDER = new DeviceRgb(134, 239, 172);      // #86efac
    private static final DeviceRgb GOLD = new DeviceRgb(202, 138, 4);                // #ca8a04
    private static final DeviceRgb DARK_TEXT = new DeviceRgb(31, 41, 55);             // #1f2937
    private static final DeviceRgb GRAY_TEXT = new DeviceRgb(107, 114, 128);          // #6b7280

    private static final int MINIMUM_PERCENTAGE = 70;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' HH:mm");

    // ═══════════════════════════════════════════════════
    //  PUBLIC API
    // ═══════════════════════════════════════════════════

    /**
     * Get or create a certificate for the given attempt.
     * If a certificate already exists in DB, return it directly.
     * If not, generate the PDF, persist it, and return it.
     */
    public Certificate getOrCreateCertificate(Long attemptId) throws IOException {
        // 1. Check if certificate already exists
        Optional<Certificate> existing = certificateRepository.findByAttemptId(attemptId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 2. Load the attempt
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

        float score = attempt.getScore() != null ? attempt.getScore() : 0;
        int totalPoints = attempt.getTotalPoints() != null ? attempt.getTotalPoints() : 0;
        int percentage = totalPoints > 0 ? Math.round(score * 100f / totalPoints) : 0;

        if (percentage < MINIMUM_PERCENTAGE) {
            throw new RuntimeException("Score percentage (" + percentage + "%) is below the required " + MINIMUM_PERCENTAGE + "% for certification.");
        }

        String studentName = attempt.getStudentName() != null ? attempt.getStudentName() : "Student #" + attempt.getStudentId();
        String quizTitle = attempt.getQuiz() != null ? attempt.getQuiz().getTitle() : "Unknown Quiz";
        String grade = computeGrade(percentage);

        // 3. Generate the PDF
        byte[] pdfData = buildCertificatePdf(attempt, studentName, quizTitle, score, totalPoints, percentage);

        // 4. Save to DB
        Certificate cert = new Certificate();
        cert.setCertificateNumber("JIE-CERT-" + String.format("%06d", attemptId));
        cert.setAttempt(attempt);
        cert.setStudentName(studentName);
        cert.setQuizTitle(quizTitle);
        cert.setScore(score);
        cert.setTotalPoints(totalPoints);
        cert.setPercentage(percentage);
        cert.setGrade(grade);
        cert.setIssuedAt(LocalDateTime.now());
        cert.setPdfData(pdfData);

        return certificateRepository.save(cert);
    }

    /**
     * Get the PDF bytes for an existing certificate.
     */
    public byte[] getCertificatePdf(Long certificateId) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found: " + certificateId));
        return cert.getPdfData();
    }

    /**
     * Get certificate metadata by attempt ID (without PDF data).
     */
    public Optional<Certificate> findByAttemptId(Long attemptId) {
        return certificateRepository.findByAttemptId(attemptId);
    }

    /**
     * Check if an attempt qualifies for certification (>= 70%).
     */
    public boolean isEligible(Long attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));
        float score = attempt.getScore() != null ? attempt.getScore() : 0;
        int totalPoints = attempt.getTotalPoints() != null ? attempt.getTotalPoints() : 0;
        int percentage = totalPoints > 0 ? Math.round(score * 100f / totalPoints) : 0;
        return percentage >= MINIMUM_PERCENTAGE;
    }

    /**
     * Check if a certificate already exists for this attempt.
     */
    public boolean existsForAttempt(Long attemptId) {
        return certificateRepository.existsByAttemptId(attemptId);
    }

    private String computeGrade(int percentage) {
        if (percentage >= 90) return "Excellent";
        if (percentage >= 80) return "Very Good";
        if (percentage >= 70) return "Good";
        return "Pass";
    }

    // ═══════════════════════════════════════════════════
    //  PDF GENERATION
    // ═══════════════════════════════════════════════════

    private byte[] buildCertificatePdf(Attempt attempt, String studentName, String quizTitle,
                                        float score, int totalPoints, int percentage) throws IOException {
        String dateStr = attempt.getSubmittedAt() != null ? attempt.getSubmittedAt().format(DATE_FMT) : LocalDateTime.now().format(DATE_FMT);
        String dateTimeStr = attempt.getSubmittedAt() != null ? attempt.getSubmittedAt().format(DATETIME_FMT) : "—";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        PageSize pageSize = PageSize.A4.rotate();
        pdfDoc.setDefaultPageSize(pageSize);
        Document doc = new Document(pdfDoc);
        doc.setMargins(25, 35, 25, 35);

        // Ensure first page exists before drawing on it
        pdfDoc.addNewPage(pageSize);

        // ─── DECORATIVE BORDER FRAME ───
        drawDecorativeBorder(pdfDoc, pageSize);

        // ─── INNER CONTENT ───

        // Spacer
        doc.add(new Paragraph("\n").setFontSize(4));

        // ─── LOGO + BRAND ───
        addLogoBrand(doc);

        // ─── DECORATIVE LINE ───
        Table divider = new Table(1).useAllAvailableWidth()
                .setMarginTop(8).setMarginBottom(5);
        divider.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(PRIMARY_GREEN, 2)).setHeight(1));
        doc.add(divider);

        // ─── CERTIFICATE TITLE ───
        doc.add(new Paragraph("CERTIFICATE OF ACHIEVEMENT")
                .setFontSize(28)
                .setBold()
                .setFontColor(DARK_GREEN)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(8)
                .setMarginBottom(2));

        doc.add(new Paragraph("This certificate is proudly presented to")
                .setFontSize(12)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8));

        // ─── STUDENT NAME ───
        doc.add(new Paragraph(studentName)
                .setFontSize(36)
                .setBold()
                .setFontColor(PRIMARY_GREEN)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        // Decorative underline under the name
        Table nameLine = new Table(1).setWidth(UnitValue.createPointValue(300))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setMarginBottom(12);
        nameLine.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new DashedBorder(GREEN_BORDER, 1.5f)).setHeight(1));
        doc.add(nameLine);

        // ─── ACHIEVEMENT TEXT ───
        doc.add(new Paragraph("For successfully completing the quiz")
                .setFontSize(12)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        doc.add(new Paragraph("\"" + quizTitle + "\"")
                .setFontSize(20)
                .setBold()
                .setFontColor(DARK_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        // ─── SCORE BADGE ───
        addScoreBadge(doc, score, totalPoints, percentage);

        // ─── DATE AND DETAILS ───
        doc.add(new Paragraph("Awarded on " + dateStr)
                .setFontSize(11)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(12)
                .setMarginBottom(8));

        // ─── FOOTER: SIGNATURE AREA ───
        addFooterSection(doc, dateTimeStr, attempt.getId());

        // ─── BOTTOM BRAND LINE ───
        Table bottomDivider = new Table(1).useAllAvailableWidth()
                .setMarginTop(8);
        bottomDivider.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(PRIMARY_GREEN, 2)).setHeight(1));
        doc.add(bottomDivider);

        doc.add(new Paragraph("Jungle In English — Learn English Through Nature")
                .setFontSize(8)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(4));

        doc.close();
        return baos.toByteArray();
    }

    // ═══════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════

    private void drawDecorativeBorder(PdfDocument pdfDoc, PageSize pageSize) {
        PdfCanvas canvas = new PdfCanvas(pdfDoc.getFirstPage());
        float w = pageSize.getWidth();
        float h = pageSize.getHeight();
        float margin = 15;

        // Outer border — thick green
        canvas.setStrokeColor(PRIMARY_GREEN);
        canvas.setLineWidth(3);
        canvas.rectangle(margin, margin, w - 2 * margin, h - 2 * margin);
        canvas.stroke();

        // Inner border — thin green
        float inner = 22;
        canvas.setStrokeColor(GREEN_BORDER);
        canvas.setLineWidth(1);
        canvas.rectangle(inner, inner, w - 2 * inner, h - 2 * inner);
        canvas.stroke();

        // Corner decorations (small squares)
        float cornerSize = 8;
        canvas.setFillColor(PRIMARY_GREEN);
        // Top-left
        canvas.rectangle(margin - 1, h - margin - cornerSize + 1, cornerSize, cornerSize).fill();
        // Top-right
        canvas.rectangle(w - margin - cornerSize + 1, h - margin - cornerSize + 1, cornerSize, cornerSize).fill();
        // Bottom-left
        canvas.rectangle(margin - 1, margin - 1, cornerSize, cornerSize).fill();
        // Bottom-right
        canvas.rectangle(w - margin - cornerSize + 1, margin - 1, cornerSize, cornerSize).fill();
    }

    private void addLogoBrand(Document doc) throws IOException {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 5, 1}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);

        // Empty cell for centering
        headerTable.addCell(new Cell().setBorder(Border.NO_BORDER));

        // Center: Logo + Brand
        Cell centerCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);

        try {
            ClassPathResource logoResource = new ClassPathResource("static/logo.jpg");
            byte[] logoBytes = logoResource.getInputStream().readAllBytes();
            Image logo = new Image(ImageDataFactory.create(logoBytes));
            logo.setWidth(55).setHeight(55)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
            centerCell.add(logo);
        } catch (Exception e) {
            // If logo not found, skip
        }

        Paragraph brandName = new Paragraph("Jungle In English")
                .setFontSize(14)
                .setBold()
                .setFontColor(PRIMARY_GREEN)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(3);
        centerCell.add(brandName);

        headerTable.addCell(centerCell);

        // Empty cell for centering
        headerTable.addCell(new Cell().setBorder(Border.NO_BORDER));

        doc.add(headerTable);
    }

    private void addScoreBadge(Document doc, float score, int totalPoints, int percentage) {
        // Outer badge table
        Table badgeTable = new Table(1).setWidth(UnitValue.createPointValue(340))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setBorder(new SolidBorder(GREEN_BORDER, 1.5f))
                .setBackgroundColor(LIGHT_GREEN_BG);

        // Inner content
        Table inner = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);

        // Score
        Cell scoreCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER).setPadding(10);
        scoreCell.add(new Paragraph("Score").setFontSize(9).setFontColor(GRAY_TEXT));
        scoreCell.add(new Paragraph(String.format("%.0f/%d", score, totalPoints))
                .setFontSize(18).setBold().setFontColor(DARK_GREEN));
        inner.addCell(scoreCell);

        // Percentage
        Cell pctCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER).setPadding(10);
        pctCell.add(new Paragraph("Percentage").setFontSize(9).setFontColor(GRAY_TEXT));
        pctCell.add(new Paragraph(percentage + "%")
                .setFontSize(18).setBold().setFontColor(PRIMARY_GREEN));
        inner.addCell(pctCell);

        // Grade
        Cell gradeCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER).setPadding(10);
        gradeCell.add(new Paragraph("Grade").setFontSize(9).setFontColor(GRAY_TEXT));
        String grade;
        DeviceRgb gradeColor;
        if (percentage >= 90) { grade = "Excellent"; gradeColor = DARK_GREEN; }
        else if (percentage >= 80) { grade = "Very Good"; gradeColor = PRIMARY_GREEN; }
        else if (percentage >= 70) { grade = "Good"; gradeColor = GOLD; }
        else { grade = "Pass"; gradeColor = GRAY_TEXT; }
        gradeCell.add(new Paragraph(grade)
                .setFontSize(16).setBold().setFontColor(gradeColor));
        inner.addCell(gradeCell);

        badgeTable.addCell(new Cell().add(inner).setBorder(Border.NO_BORDER).setPadding(5));
        doc.add(badgeTable);
    }

    private void addFooterSection(Document doc, String dateTimeStr, Long attemptId) {
        Table footerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER)
                .setMarginTop(5);

        // Left: Certificate ID
        Cell leftCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT).setPadding(5);
        leftCell.add(new Paragraph("Certificate ID").setFontSize(8).setFontColor(GRAY_TEXT));
        leftCell.add(new Paragraph("JIE-CERT-" + String.format("%06d", attemptId))
                .setFontSize(10).setBold().setFontColor(DARK_TEXT));
        footerTable.addCell(leftCell);

        // Center: Seal / Award icon text
        Cell centerCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(5);
        centerCell.add(new Paragraph("★")
                .setFontSize(28).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER));
        centerCell.add(new Paragraph("Verified Achievement")
                .setFontSize(8).setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER));
        footerTable.addCell(centerCell);

        // Right: Date & Time
        Cell rightCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT).setPadding(5);
        rightCell.add(new Paragraph("Completed on").setFontSize(8).setFontColor(GRAY_TEXT));
        rightCell.add(new Paragraph(dateTimeStr)
                .setFontSize(10).setBold().setFontColor(DARK_TEXT));
        footerTable.addCell(rightCell);

        doc.add(footerTable);
    }
}
