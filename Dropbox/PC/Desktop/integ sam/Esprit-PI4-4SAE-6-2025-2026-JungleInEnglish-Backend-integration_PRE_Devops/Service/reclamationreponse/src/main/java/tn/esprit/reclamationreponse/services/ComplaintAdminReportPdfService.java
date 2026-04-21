package tn.esprit.reclamationreponse.services;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import tn.esprit.reclamationreponse.dto.ComplaintAdminReportStatsDto;
import tn.esprit.reclamationreponse.dto.ComplaintPriorityLevel;
import tn.esprit.reclamationreponse.entities.ComplaintStatus;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ComplaintAdminReportPdfService {

    private static final Color BRAND = new Color(14, 163, 122);
    private static final Color TITLE_DARK = new Color(31, 41, 55);
    private static final Color LIGHT_BG = new Color(247, 250, 252);

    public byte[] generatePdf(ComplaintAdminReportStatsDto stats) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addHeader(document);
            addSummarySection(document, stats);
            addStatusSection(document, stats);
            addPrioritySection(document, stats);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate admin report PDF", ex);
        }
    }

    private void addHeader(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, TITLE_DARK);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY);

        Paragraph appName = new Paragraph("JungleInEnglish", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND));
        appName.setSpacingAfter(4f);
        document.add(appName);

        Paragraph title = new Paragraph("Admin Complaints Performance Report", titleFont);
        title.setSpacingAfter(6f);
        document.add(title);

        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Paragraph subtitle = new Paragraph("Generated on " + generatedAt + " | Back-office analytics export", subtitleFont);
        subtitle.setSpacingAfter(20f);
        document.add(subtitle);
    }

    private void addSummarySection(Document document, ComplaintAdminReportStatsDto stats) throws DocumentException {
        addSectionTitle(document, "Executive Summary");

        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        long unresolvedCount = Math.round((stats.unresolvedRate() / 100.0) * stats.totalComplaints());

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(14f);
        table.setWidths(new float[]{2.2f, 1f});

        addMetricRow(table, "Total complaints", String.valueOf(stats.totalComplaints()));
        addMetricRow(table, "Average processing time", decimalFormat.format(stats.averageProcessingHours()) + " h");
        addMetricRow(table, "Unresolved rate", decimalFormat.format(stats.unresolvedRate()) + " %");
        addMetricRow(table, "Unresolved complaints", String.valueOf(unresolvedCount));

        document.add(table);
    }

    private void addStatusSection(Document document, ComplaintAdminReportStatsDto stats) throws DocumentException {
        addSectionTitle(document, "Distribution by Status");

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(14f);
        table.setWidths(new float[]{2f, 1f});

        addHeaderCell(table, "Status");
        addHeaderCell(table, "Count");

        addBodyCell(table, "Pending");
        addBodyCell(table, String.valueOf(stats.complaintsByStatus().getOrDefault(ComplaintStatus.EN_ATTENTE, 0L)));

        addBodyCell(table, "Resolved");
        addBodyCell(table, String.valueOf(stats.complaintsByStatus().getOrDefault(ComplaintStatus.TRAITEE, 0L)));

        addBodyCell(table, "Rejected");
        addBodyCell(table, String.valueOf(stats.complaintsByStatus().getOrDefault(ComplaintStatus.REJETEE, 0L)));

        document.add(table);
    }

    private void addPrioritySection(Document document, ComplaintAdminReportStatsDto stats) throws DocumentException {
        addSectionTitle(document, "Distribution by Priority");

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(14f);
        table.setWidths(new float[]{2f, 1f});

        addHeaderCell(table, "Priority");
        addHeaderCell(table, "Count");

        addBodyCell(table, "High");
        addBodyCell(table, String.valueOf(stats.complaintsByPriority().getOrDefault(ComplaintPriorityLevel.HIGH, 0L)));

        addBodyCell(table, "Medium");
        addBodyCell(table, String.valueOf(stats.complaintsByPriority().getOrDefault(ComplaintPriorityLevel.MEDIUM, 0L)));

        addBodyCell(table, "Low");
        addBodyCell(table, String.valueOf(stats.complaintsByPriority().getOrDefault(ComplaintPriorityLevel.LOW, 0L)));

        document.add(table);
    }

    private void addSectionTitle(Document document, String title) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, TITLE_DARK);
        Paragraph section = new Paragraph(title, sectionFont);
        section.setSpacingBefore(4f);
        section.setSpacingAfter(8f);
        document.add(section);
    }

    private void addMetricRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA, 11, TITLE_DARK)));
        styleMetricCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND)));
        styleMetricCell(valueCell);
        valueCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void styleMetricCell(PdfPCell cell) {
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(LIGHT_BG);
        cell.setPadding(8f);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE)));
        cell.setBackgroundColor(BRAND);
        cell.setPadding(8f);
        cell.setBorderColor(new Color(200, 210, 220));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 11, TITLE_DARK)));
        cell.setPadding(8f);
        cell.setBorderColor(new Color(220, 227, 234));
        table.addCell(cell);
    }
}
