package tn.esprit.quiz_microservice.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import tn.esprit.quiz_microservice.entities.Attempt;
import tn.esprit.quiz_microservice.entities.Certificate;
import tn.esprit.quiz_microservice.repositories.AttemptRepository;
import tn.esprit.quiz_microservice.repositories.CertificateRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttemptExcelService {

    private final AttemptRepository attemptRepository;
    private final CertificateRepository certificateRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    public byte[] generateAttemptsExcel(Long quizId, String quizTitle, String studentName) throws IOException {
        List<Attempt> attempts = fetchAttempts(quizId, quizTitle, studentName);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Attempts Report");

            // ─── STYLES ───
            XSSFCellStyle titleStyle = createTitleStyle(workbook);
            XSSFCellStyle subtitleStyle = createSubtitleStyle(workbook);
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle dataStyle = createDataStyle(workbook);
            XSSFCellStyle altDataStyle = createAltDataStyle(workbook);
            XSSFCellStyle scoreGreenStyle = createScoreStyle(workbook, new XSSFColor(new byte[]{21, (byte) 128, 61}, null));
            XSSFCellStyle scoreYellowStyle = createScoreStyle(workbook, new XSSFColor(new byte[]{(byte) 202, (byte) 138, 4}, null));
            XSSFCellStyle scoreRedStyle = createScoreStyle(workbook, new XSSFColor(new byte[]{(byte) 185, 28, 28}, null));
            XSSFCellStyle passedStyle = createResultStyle(workbook, true);
            XSSFCellStyle failedStyle = createResultStyle(workbook, false);
            XSSFCellStyle kpiLabelStyle = createKpiLabelStyle(workbook);
            XSSFCellStyle kpiValueStyle = createKpiValueStyle(workbook);
            XSSFCellStyle certStyle = createCertificateStyle(workbook);
            XSSFCellStyle certNoneStyle = createCertificateNoneStyle(workbook);

            int rowIdx = 0;

            // ─── TITLE ROW ───
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.setHeightInPoints(35);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Jungle In English — Attempts Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            // ─── SUBTITLE ───
            Row subtitleRow = sheet.createRow(rowIdx++);
            Cell subCell = subtitleRow.createCell(0);
            String filterInfo = buildFilterInfo(quizId, quizTitle, studentName);
            subCell.setCellValue("Generated on " + LocalDateTime.now().format(DATE_FMT) + filterInfo);
            subCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

            rowIdx++; // Empty row

            // ─── KPI ROW ───
            Row kpiLabelRow = sheet.createRow(rowIdx++);
            Row kpiValueRow = sheet.createRow(rowIdx++);

            // Compute stats
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

            String[][] kpis = {
                    {"Total Attempts", String.valueOf(total)},
                    {"Average Score", Math.round(avgScore) + "%"},
                    {"Pass Rate", Math.round(passRate) + "%"},
                    {"Passed / Failed", passedCount + " / " + (total - passedCount)}
            };

            for (int i = 0; i < kpis.length; i++) {
                Cell labelCell = kpiLabelRow.createCell(i);
                labelCell.setCellValue(kpis[i][0]);
                labelCell.setCellStyle(kpiLabelStyle);

                Cell valCell = kpiValueRow.createCell(i);
                valCell.setCellValue(kpis[i][1]);
                valCell.setCellStyle(kpiValueStyle);
            }

            rowIdx++; // Empty row

            // ─── TABLE HEADER ───
            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(25);
            String[] headers = {"#", "Quiz", "Quiz ID", "Student", "Score", "%", "Result", "Certificate", "Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ─── DATA ROWS ───
            for (int i = 0; i < attempts.size(); i++) {
                Attempt a = attempts.get(i);
                Row row = sheet.createRow(rowIdx++);

                float score = a.getScore() != null ? a.getScore() : 0;
                int totalPts = a.getTotalPoints() != null ? a.getTotalPoints() : 0;
                int pct = totalPts > 0 ? Math.round(score * 100f / totalPts) : 0;
                boolean passed = pct >= 50;
                String quizName = a.getQuiz() != null ? a.getQuiz().getTitle() : "Unknown";
                String quizIdStr = a.getQuiz() != null ? String.valueOf(a.getQuiz().getId()) : "—";
                String student = a.getStudentName() != null ? a.getStudentName() : "Student #" + a.getStudentId();
                String dateStr = a.getSubmittedAt() != null ? a.getSubmittedAt().format(DATE_FMT) : "—";

                XSSFCellStyle rowStyle = i % 2 == 0 ? dataStyle : altDataStyle;

                // #
                Cell c0 = row.createCell(0);
                c0.setCellValue(i + 1);
                c0.setCellStyle(rowStyle);

                // Quiz
                Cell c1 = row.createCell(1);
                c1.setCellValue(quizName);
                c1.setCellStyle(rowStyle);

                // Quiz ID
                Cell c2 = row.createCell(2);
                c2.setCellValue(quizIdStr);
                c2.setCellStyle(rowStyle);

                // Student
                Cell c3 = row.createCell(3);
                c3.setCellValue(student);
                c3.setCellStyle(rowStyle);

                // Score
                Cell c4 = row.createCell(4);
                c4.setCellValue(String.format("%.0f/%d", score, totalPts));
                XSSFCellStyle sStyle = pct >= 80 ? scoreGreenStyle : (pct >= 50 ? scoreYellowStyle : scoreRedStyle);
                c4.setCellStyle(sStyle);

                // %
                Cell c5 = row.createCell(5);
                c5.setCellValue(pct + "%");
                c5.setCellStyle(sStyle);

                // Result
                Cell c6 = row.createCell(6);
                c6.setCellValue(passed ? "Passed" : "Failed");
                c6.setCellStyle(passed ? passedStyle : failedStyle);

                // Certificate
                String certGrade = certificateRepository.findByAttemptId(a.getId())
                        .map(Certificate::getGrade)
                        .orElse(null);
                Cell c7 = row.createCell(7);
                c7.setCellValue(certGrade != null ? certGrade : "None");
                c7.setCellStyle(certGrade != null ? certStyle : certNoneStyle);

                // Date
                Cell c8 = row.createCell(8);
                c8.setCellValue(dateStr);
                c8.setCellStyle(rowStyle);
            }

            // ─── AUTO-SIZE COLUMNS ───
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Add a bit of padding
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 800);
            }

            // ─── TOTAL ROW ───
            Row totalRow = sheet.createRow(rowIdx);
            Cell totalCell = totalRow.createCell(0);
            totalCell.setCellValue(attempts.size() + " attempt(s) exported");
            totalCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 8));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    private List<Attempt> fetchAttempts(Long quizId, String quizTitle, String studentName) {
        boolean hasTitle = quizTitle != null && !quizTitle.isEmpty();
        boolean hasName = studentName != null && !studentName.isEmpty();

        if (hasTitle && hasName) {
            return attemptRepository.findByQuizTitleAndStudentNameList(quizTitle, studentName);
        } else if (hasTitle && quizId != null) {
            return attemptRepository.findByQuizTitleContainingAndQuizIdList(quizTitle, quizId);
        } else if (hasTitle) {
            return attemptRepository.findByQuizTitleContainingList(quizTitle);
        } else if (hasName && quizId != null) {
            return attemptRepository.findByStudentNameContainingAndQuizIdList(studentName, quizId);
        } else if (hasName) {
            return attemptRepository.findByStudentNameContainingList(studentName);
        } else if (quizId != null) {
            return attemptRepository.findByQuizIdOrderBySubmittedAtDesc(quizId);
        }
        return attemptRepository.findAllByOrderBySubmittedAtDesc();
    }

    private String buildFilterInfo(Long quizId, String quizTitle, String studentName) {
        StringBuilder sb = new StringBuilder();
        if (quizTitle != null && !quizTitle.isEmpty()) sb.append(" | Quiz: \"").append(quizTitle).append("\"");
        if (studentName != null && !studentName.isEmpty()) sb.append(" | Student: \"").append(studentName).append("\"");
        if (quizId != null && (quizTitle == null || quizTitle.isEmpty())) sb.append(" | Quiz ID: ").append(quizId);
        return sb.toString();
    }

    // ═══════════════════ STYLES ═══════════════════

    private XSSFCellStyle createTitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(new XSSFColor(new byte[]{14, (byte) 163, 122}, null)); // #0ea37a
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle createSubtitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setColor(new XSSFColor(new byte[]{107, 114, (byte) 128}, null)); // gray
        style.setFont(font);
        return style;
    }

    private XSSFCellStyle createHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(new byte[]{14, (byte) 163, 122}, null)); // #0ea37a
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createDataStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createAltDataStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = createDataStyle(wb);
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 249, (byte) 250, (byte) 251}, null)); // #f9fafb
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private XSSFCellStyle createScoreStyle(XSSFWorkbook wb, XSSFColor color) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(color);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createResultStyle(XSSFWorkbook wb, boolean passed) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        if (passed) {
            style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 220, (byte) 252, (byte) 231}, null)); // green bg
            font.setColor(new XSSFColor(new byte[]{21, (byte) 128, 61}, null));
        } else {
            style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 254, (byte) 226, (byte) 226}, null)); // red bg
            font.setColor(new XSSFColor(new byte[]{(byte) 185, 28, 28}, null));
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createKpiLabelStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 8);
        font.setColor(new XSSFColor(new byte[]{107, 114, (byte) 128}, null));
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 249, (byte) 250, (byte) 251}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createKpiValueStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(new XSSFColor(new byte[]{14, (byte) 163, 122}, null)); // #0ea37a
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 249, (byte) 250, (byte) 251}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createCertificateStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        font.setColor(new XSSFColor(new byte[]{(byte) 146, 64, 14}, null)); // #92400e
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 254, (byte) 243, (byte) 199}, null)); // #fef3c7
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createCertificateNoneStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setColor(new XSSFColor(new byte[]{107, 114, (byte) 128}, null)); // gray
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
