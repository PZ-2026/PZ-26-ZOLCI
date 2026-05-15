package com.trainit.pdf.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.trainit.pdf.model.ExerciseResultData;
import com.trainit.pdf.model.ReportData;
import com.trainit.pdf.model.SessionData;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generuje brandowane raporty PDF aktywności treningowej przy użyciu OpenPDF.
 *
 * <p>Schemat kolorów:
 * <ul>
 *   <li>Ciemny nagłówek {@code #1C1C1E} z żółtym akcentem {@code #FFD600}</li>
 *   <li>Nagłówki tabel {@code #2C2C2E} z białym tekstem</li>
 *   <li>Naprzemienne wiersze: biały / {@code #F5F5F5}</li>
 *   <li>Sekcja sesji: {@code #F0F0F0} tło</li>
 * </ul>
 */
public class PdfReportService {

    private static final Color C_DARK       = new Color(28,  28,  30);
    private static final Color C_DARK2      = new Color(44,  44,  46);
    private static final Color C_YELLOW     = new Color(255, 214,  0);
    private static final Color C_WHITE      = Color.WHITE;
    private static final Color C_ROW_ALT    = new Color(245, 245, 245);
    private static final Color C_ROW_HEAD   = new Color(240, 240, 240);
    private static final Color C_TEXT_MAIN  = new Color(30,  30,  30);
    private static final Color C_TEXT_SEC   = new Color(100, 100, 100);
    private static final Color C_BORDER     = new Color(210, 210, 210);
    private static final Color C_ACCENT_BG  = new Color(255, 250, 220);

    private static final Font F_BRAND       = font(FontFactory.HELVETICA_BOLD,    20, C_YELLOW);
    private static final Font F_HEADER_SUB  = font(FontFactory.HELVETICA,         11, C_WHITE);
    private static final Font F_SECTION     = font(FontFactory.HELVETICA_BOLD,    11, C_WHITE);
    private static final Font F_LABEL       = font(FontFactory.HELVETICA_BOLD,    10, C_TEXT_SEC);
    private static final Font F_VALUE       = font(FontFactory.HELVETICA,         10, C_TEXT_MAIN);
    private static final Font F_SESSION_TTL = font(FontFactory.HELVETICA_BOLD,    11, C_DARK);
    private static final Font F_SESSION_META= font(FontFactory.HELVETICA,          9, C_TEXT_SEC);
    private static final Font F_TBL_HDR     = font(FontFactory.HELVETICA_BOLD,     9, C_WHITE);
    private static final Font F_TBL_CELL    = font(FontFactory.HELVETICA,          9, C_TEXT_MAIN);
    private static final Font F_STAT_NUM    = font(FontFactory.HELVETICA_BOLD,    18, C_DARK);
    private static final Font F_STAT_LBL    = font(FontFactory.HELVETICA,          8, C_TEXT_SEC);
    private static final Font F_EMPTY       = font(FontFactory.HELVETICA_OBLIQUE, 10, C_TEXT_SEC);
    private static final Font F_FOOTER      = font(FontFactory.HELVETICA,          8, C_TEXT_SEC);
    private static final Font F_NOTES       = font(FontFactory.HELVETICA_OBLIQUE,  9, C_TEXT_SEC);

    private static final DateTimeFormatter GEN_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");


    /**
     * Generuje raport PDF na podstawie przekazanych danych.
     *
     * @param data model danych raportu
     * @return zawartość pliku PDF w bajtach
     * @throws IllegalArgumentException gdy {@code data} jest null
     * @throws IllegalStateException gdy generowanie PDF się nie powiedzie
     */
    public byte[] generateReport(ReportData data) {
        if (data == null) throw new IllegalArgumentException("ReportData must not be null");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new FooterEvent(data));
            doc.open();

            addHeader(doc, data);
            addMetaBox(doc, data);
            addStatsRow(doc, data.getSessions());
            addSessionsSection(doc, data.getSessions());

            doc.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Failed to generate PDF report", e);
        }
    }


    private void addHeader(Document doc, ReportData data) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100f);
        header.setWidths(new float[]{2.5f, 1.5f});
        header.setSpacingAfter(10f);

        PdfPCell left = new PdfPCell();
        left.setBackgroundColor(C_DARK);
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(16f);
        left.setPaddingLeft(20f);

        Paragraph brand = new Paragraph("TrainIT", F_BRAND);
        brand.setSpacingAfter(2f);
        left.addElement(brand);
        left.addElement(new Paragraph("Raport aktywności treningowej", F_HEADER_SUB));
        header.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBackgroundColor(C_YELLOW);
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(10f);
        right.setHorizontalAlignment(Element.ALIGN_CENTER);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Font typeFont = font(FontFactory.HELVETICA_BOLD, 11, C_DARK);
        Paragraph typePar = new Paragraph(nullToDash(data.getReportType()), typeFont);
        typePar.setAlignment(Element.ALIGN_CENTER);
        right.addElement(typePar);
        header.addCell(right);

        doc.add(header);
    }


    private void addMetaBox(Document doc, ReportData data) throws DocumentException {
        PdfPTable meta = new PdfPTable(4);
        meta.setWidthPercentage(100f);
        meta.setSpacingAfter(12f);

        addMetaCell(meta, "UŻYTKOWNIK",  nullToDash(data.getUserName()));
        addMetaCell(meta, "OD",          nullToDash(data.getDateFrom()));
        addMetaCell(meta, "DO",          nullToDash(data.getDateTo()));
        addMetaCell(meta, "WYGENEROWANO",
                LocalDateTime.now().format(GEN_FMT));

        doc.add(meta);
    }

    private void addMetaCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(C_BORDER);
        cell.setBorderWidth(0.5f);
        cell.setPadding(10f);
        cell.setBackgroundColor(C_ROW_ALT);

        Paragraph lbl = new Paragraph(label, F_LABEL);
        lbl.setSpacingAfter(3f);
        cell.addElement(lbl);
        cell.addElement(new Paragraph(value, F_VALUE));
        table.addCell(cell);
    }


    private void addStatsRow(Document doc, List<SessionData> sessions) throws DocumentException {
        int sessionCount = sessions != null ? sessions.size() : 0;
        int totalMin = 0;
        int exerciseCount = 0;
        if (sessions != null) {
            for (SessionData s : sessions) {
                if (s.getDurationMinutes() != null) totalMin += s.getDurationMinutes();
                if (s.getResults() != null) exerciseCount += s.getResults().size();
            }
        }

        PdfPTable stats = new PdfPTable(3);
        stats.setWidthPercentage(100f);
        stats.setSpacingAfter(16f);

        addStatCell(stats, String.valueOf(sessionCount), "sesji treningowych", false);
        addStatCell(stats, totalMin + " min",            "łączny czas",        true);
        addStatCell(stats, String.valueOf(exerciseCount),"wyników ćwiczeń",    false);

        doc.add(stats);
    }

    private void addStatCell(PdfPTable table, String number, String label, boolean accent) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(accent ? C_YELLOW : C_BORDER);
        cell.setBorderWidth(accent ? 2f : 0.5f);
        cell.setPadding(12f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(accent ? C_ACCENT_BG : C_WHITE);

        Paragraph num = new Paragraph(number, F_STAT_NUM);
        num.setAlignment(Element.ALIGN_CENTER);
        num.setSpacingAfter(3f);
        cell.addElement(num);

        Paragraph lbl = new Paragraph(label.toUpperCase(), F_STAT_LBL);
        lbl.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(lbl);

        table.addCell(cell);
    }


    private void addSectionHeader(Document doc, String text) throws DocumentException {
        PdfPTable bar = new PdfPTable(1);
        bar.setWidthPercentage(100f);
        bar.setSpacingAfter(8f);

        PdfPCell cell = new PdfPCell(new Phrase(text, F_SECTION));
        cell.setBackgroundColor(C_DARK2);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8f);
        cell.setPaddingLeft(14f);
        bar.addCell(cell);

        doc.add(bar);
    }


    private void addSessionsSection(Document doc, List<SessionData> sessions)
            throws DocumentException {

        addSectionHeader(doc, "HISTORIA TRENINGÓW");

        if (sessions == null || sessions.isEmpty()) {
            PdfPTable empty = new PdfPTable(1);
            empty.setWidthPercentage(100f);
            empty.setSpacingAfter(10f);
            PdfPCell cell = new PdfPCell(new Phrase(
                    "Brak danych treningowych w wybranym okresie.", F_EMPTY));
            cell.setPadding(16f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderColor(C_BORDER);
            empty.addCell(cell);
            doc.add(empty);
            return;
        }

        for (int i = 0; i < sessions.size(); i++) {
            addSessionBlock(doc, sessions.get(i), i + 1);
        }
    }

    private void addSessionBlock(Document doc, SessionData session, int index)
            throws DocumentException {

        PdfPTable titleBar = new PdfPTable(2);
        titleBar.setWidthPercentage(100f);
        titleBar.setWidths(new float[]{0.3f, 3.7f});
        titleBar.setSpacingBefore(8f);

        PdfPCell numCell = new PdfPCell(new Phrase(String.valueOf(index),
                font(FontFactory.HELVETICA_BOLD, 12, C_DARK)));
        numCell.setBackgroundColor(C_YELLOW);
        numCell.setBorder(Rectangle.NO_BORDER);
        numCell.setPadding(10f);
        numCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleBar.addCell(numCell);

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBackgroundColor(C_ROW_HEAD);
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setBorderWidthBottom(1.5f);
        infoCell.setBorderColorBottom(C_YELLOW);
        infoCell.setPadding(8f);
        infoCell.setPaddingLeft(12f);

        Paragraph name = new Paragraph(nullToDash(session.getWorkoutName()), F_SESSION_TTL);
        name.setSpacingAfter(2f);
        infoCell.addElement(name);

        String duration = session.getDurationMinutes() != null
                ? session.getDurationMinutes() + " min" : "—";
        infoCell.addElement(new Paragraph(
                nullToDash(session.getCompletedDate()) + "  •  " + duration,
                F_SESSION_META));
        titleBar.addCell(infoCell);

        doc.add(titleBar);
        doc.add(buildResultsTable(session.getResults()));
    }


    private PdfPTable buildResultsTable(List<ExerciseResultData> results) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100f);
        table.setSpacingAfter(4f);
        try {
            table.setWidths(new float[]{3f, 1f, 1.2f, 1.3f, 2.5f});
        } catch (DocumentException ignored) {}

        addTblHeader(table, "Ćwiczenie");
        addTblHeader(table, "Serie");
        addTblHeader(table, "Powtórzenia");
        addTblHeader(table, "Ciężar (kg)");
        addTblHeader(table, "Uwagi");

        if (results == null || results.isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase("Brak wyników ćwiczeń", F_EMPTY));
            cell.setColspan(5);
            cell.setPadding(8f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderColor(C_BORDER);
            table.addCell(cell);
            return table;
        }

        for (int i = 0; i < results.size(); i++) {
            ExerciseResultData r = results.get(i);
            Color rowBg = (i % 2 == 0) ? C_WHITE : C_ROW_ALT;

            table.addCell(dataCell(nullToDash(r.getExerciseName()), F_TBL_CELL, rowBg, Element.ALIGN_LEFT));
            table.addCell(dataCell(formatInt(r.getSetsDone()),       F_TBL_CELL, rowBg, Element.ALIGN_CENTER));
            table.addCell(dataCell(formatInt(r.getRepsDone()),       F_TBL_CELL, rowBg, Element.ALIGN_CENTER));
            table.addCell(dataCell(formatWeight(r.getWeightUsed()),  F_TBL_CELL, rowBg, Element.ALIGN_CENTER));

            String notes = (r.getNotes() != null && !r.getNotes().isBlank())
                    ? r.getNotes() : "—";
            table.addCell(dataCell(notes, F_NOTES, rowBg, Element.ALIGN_LEFT));
        }

        return table;
    }

    private void addTblHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, F_TBL_HDR));
        cell.setBackgroundColor(C_DARK2);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(7f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private PdfPCell dataCell(String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(C_BORDER);
        cell.setBorderWidth(0.3f);
        cell.setPadding(6f);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }


    /**
     * Zdarzenie strony: rysuje stopkę na dole każdej strony.
     */
    private static class FooterEvent extends PdfPageEventHelper {
        private final String userName;

        FooterEvent(ReportData data) {
            this.userName = data.getUserName() != null ? data.getUserName() : "—";
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
                Rectangle pageSize = document.getPageSize();
                float y = document.bottomMargin() - 20f;

                cb.saveState();

                cb.setColorStroke(new Color(200, 200, 200));
                cb.setLineWidth(0.5f);
                cb.moveTo(document.leftMargin(), y + 12);
                cb.lineTo(pageSize.getWidth() - document.rightMargin(), y + 12);
                cb.stroke();

                cb.beginText();
                cb.setFontAndSize(bf, 7.5f);
                cb.setColorFill(new Color(130, 130, 130));
                cb.showTextAligned(Element.ALIGN_LEFT,
                        "TrainIT — Raport aktywności  |  " + userName,
                        document.leftMargin(), y, 0);
                cb.showTextAligned(Element.ALIGN_RIGHT,
                        "Strona " + writer.getPageNumber(),
                        pageSize.getWidth() - document.rightMargin(), y, 0);
                cb.endText();

                cb.restoreState();
            } catch (DocumentException | IOException ignored) {}
        }
    }


    private static Font font(String name, float size, Color color) {
        Font f = FontFactory.getFont(name, size);
        f.setColor(color);
        return f;
    }

    private String nullToDash(String v) {
        return (v == null || v.isBlank()) ? "—" : v;
    }

    private String formatInt(Integer v) {
        return v != null ? String.valueOf(v) : "—";
    }

    private String formatWeight(Double v) {
        return v != null ? (v % 1 == 0 ? String.valueOf(v.intValue()) : String.valueOf(v)) : "—";
    }
}
