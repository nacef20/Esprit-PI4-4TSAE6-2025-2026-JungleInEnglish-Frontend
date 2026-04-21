package tn.esprit.reclamationreponse.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import tn.esprit.reclamationreponse.dto.ComplaintAdminReportStatsDto;
import tn.esprit.reclamationreponse.dto.ComplaintPriorityLevel;
import tn.esprit.reclamationreponse.entities.ComplaintStatus;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ComplaintAdminReportExcelService {

    public byte[] generateExcel(ComplaintAdminReportStatsDto stats) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Admin Report");

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle sectionStyle = createSectionStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle valueStyle = createValueStyle(workbook);

            int rowIndex = 0;
            rowIndex = addHeader(sheet, rowIndex, titleStyle);
            rowIndex = addSummarySection(sheet, rowIndex, stats, sectionStyle, headerStyle, valueStyle);
            rowIndex = addStatusSection(sheet, rowIndex, stats, sectionStyle, headerStyle, valueStyle);
            addPrioritySection(sheet, rowIndex, stats, sectionStyle, headerStyle, valueStyle);

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.setColumnWidth(0, Math.max(sheet.getColumnWidth(0), 8000));
            sheet.setColumnWidth(1, Math.max(sheet.getColumnWidth(1), 4500));

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate admin report Excel", ex);
        }
    }

    private int addHeader(Sheet sheet, int rowIndex, CellStyle titleStyle) {
        Row appRow = sheet.createRow(rowIndex++);
        createCell(appRow, 0, "JungleInEnglish", titleStyle);

        Row titleRow = sheet.createRow(rowIndex++);
        createCell(titleRow, 0, "Admin Complaints Performance Report", titleStyle);

        Row generatedRow = sheet.createRow(rowIndex++);
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        createCell(generatedRow, 0, "Generated on " + generatedAt, null);

        return rowIndex + 1;
    }

    private int addSummarySection(
            Sheet sheet,
            int rowIndex,
            ComplaintAdminReportStatsDto stats,
            CellStyle sectionStyle,
            CellStyle headerStyle,
            CellStyle valueStyle
    ) {
        Row sectionRow = sheet.createRow(rowIndex++);
        createCell(sectionRow, 0, "Executive Summary", sectionStyle);

        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        long unresolvedCount = Math.round((stats.unresolvedRate() / 100.0) * stats.totalComplaints());

        rowIndex = addKeyValueRow(sheet, rowIndex, "Total complaints", String.valueOf(stats.totalComplaints()), headerStyle, valueStyle);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Average processing time", decimalFormat.format(stats.averageProcessingHours()) + " h", headerStyle, valueStyle);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Unresolved rate", decimalFormat.format(stats.unresolvedRate()) + " %", headerStyle, valueStyle);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Unresolved complaints", String.valueOf(unresolvedCount), headerStyle, valueStyle);

        return rowIndex + 1;
    }

    private int addStatusSection(
            Sheet sheet,
            int rowIndex,
            ComplaintAdminReportStatsDto stats,
            CellStyle sectionStyle,
            CellStyle headerStyle,
            CellStyle valueStyle
    ) {
        Row sectionRow = sheet.createRow(rowIndex++);
        createCell(sectionRow, 0, "Distribution by Status", sectionStyle);

        rowIndex = addKeyValueRow(sheet, rowIndex, "Pending", String.valueOf(stats.complaintsByStatus().getOrDefault(ComplaintStatus.EN_ATTENTE, 0L)), headerStyle, valueStyle);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Resolved", String.valueOf(stats.complaintsByStatus().getOrDefault(ComplaintStatus.TRAITEE, 0L)), headerStyle, valueStyle);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Rejected", String.valueOf(stats.complaintsByStatus().getOrDefault(ComplaintStatus.REJETEE, 0L)), headerStyle, valueStyle);

        return rowIndex + 1;
    }

    private int addPrioritySection(
            Sheet sheet,
            int rowIndex,
            ComplaintAdminReportStatsDto stats,
            CellStyle sectionStyle,
            CellStyle headerStyle,
            CellStyle valueStyle
    ) {
        Row sectionRow = sheet.createRow(rowIndex++);
        createCell(sectionRow, 0, "Distribution by Priority", sectionStyle);

        rowIndex = addKeyValueRow(sheet, rowIndex, "High", String.valueOf(stats.complaintsByPriority().getOrDefault(ComplaintPriorityLevel.HIGH, 0L)), headerStyle, valueStyle);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Medium", String.valueOf(stats.complaintsByPriority().getOrDefault(ComplaintPriorityLevel.MEDIUM, 0L)), headerStyle, valueStyle);
        rowIndex = addKeyValueRow(sheet, rowIndex, "Low", String.valueOf(stats.complaintsByPriority().getOrDefault(ComplaintPriorityLevel.LOW, 0L)), headerStyle, valueStyle);

        return rowIndex;
    }

    private int addKeyValueRow(
            Sheet sheet,
            int rowIndex,
            String key,
            String value,
            CellStyle keyStyle,
            CellStyle valueStyle
    ) {
        Row row = sheet.createRow(rowIndex++);
        createCell(row, 0, key, keyStyle);
        createCell(row, 1, value, valueStyle);
        return rowIndex;
    }

    private void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private CellStyle createTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 13);
        style.setFont(font);
        return style;
    }

    private CellStyle createSectionStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createValueStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
}
