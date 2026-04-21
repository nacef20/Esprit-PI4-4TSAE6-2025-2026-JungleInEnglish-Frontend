package tn.esprit.gestion_planning.Services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestion_planning.Entites.CreneauHoraire;
import tn.esprit.gestion_planning.Entites.PlanningSession;
import tn.esprit.gestion_planning.Repositories.CreneauRepository;
import tn.esprit.gestion_planning.Repositories.PlanningSessionRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class PlanningPdfService {

    private final PlanningSessionRepository planningSessionRepository;
    private final CreneauRepository creneauRepository;

    // ── Couleurs ─────────────────────────────────────────────────────────
    private static final Color GREEN_JUNGLE  = new Color(46, 125, 50);
    private static final Color DARK_GREEN    = new Color(27, 94, 32);
    private static final Color GRAY_HEADER   = new Color(192, 192, 192);
    private static final Color WHITE         = Color.WHITE;
    private static final Color BLACK         = Color.BLACK;
    private static final Color BORDER        = new Color(158, 158, 158);
    private static final Color DARK_GRAY     = new Color(66, 66, 66);

    // Couleurs alternées pour les cellules sessions (selon matiereId % 4)
    private static final Color[] CELL_COLORS = {
            new Color(232, 245, 233), // vert clair
            new Color(227, 242, 253), // bleu clair
            new Color(255, 249, 196), // jaune clair
            new Color(252, 228, 236), // rose clair
    };

    // ── Jours de la semaine ───────────────────────────────────────────────
    private static final String[] JOURS_NOMS = {
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"
    };

    // ─────────────────────────────────────────────────────────────────────
    // MÉTHODE PRINCIPALE
    // ─────────────────────────────────────────────────────────────────────
    public byte[] generateEmploiDuTempsPdf(LocalDate anyDayInWeek) throws Exception {

        // ── 1. Calculer lundi et samedi de la semaine ─────────────────────
        LocalDate lundi  = anyDayInWeek.with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDate samedi = lundi.plusDays(5);
        DateTimeFormatter fmt      = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fmtCourt = DateTimeFormatter.ofPattern("dd/MM");

        // ── 2. Charger les créneaux dynamiquement depuis la DB ────────────
        List<CreneauHoraire> tousCreneaux = creneauRepository.findAll();
        tousCreneaux.sort(Comparator.comparing(CreneauHoraire::getHeureDebut));

        if (tousCreneaux.isEmpty()) {
            throw new RuntimeException("Aucun créneau trouvé en base de données.");
        }

        // ── 3. Charger les sessions de la semaine ─────────────────────────
        List<PlanningSession> sessions =
                planningSessionRepository.findByDatePlanningBetween(lundi, samedi);

        // ── 4. Setup document PDF paysage A4 ─────────────────────────────
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 18, 18, 18, 18);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        doc.open();

        PdfContentByte cb = writer.getDirectContent();
        float W = doc.getPageSize().getWidth();
        float H = doc.getPageSize().getHeight();

        // Charger les fonts
        BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, false);
        BaseFont bfNorm = BaseFont.createFont(BaseFont.HELVETICA,      BaseFont.WINANSI, false);

        // ── 5. HEADER ─────────────────────────────────────────────────────
        drawHeader(cb, bfBold, bfNorm, W, H, lundi, samedi, fmt);

        // ── 6. TABLE ──────────────────────────────────────────────────────
        drawTable(cb, bfBold, bfNorm, W, H,
                lundi, fmtCourt,
                tousCreneaux, sessions);

        // ── 7. FOOTER ─────────────────────────────────────────────────────
        drawFooter(cb, bfNorm, W, fmt);

        doc.close();
        return baos.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────────────────────────────
    private void drawHeader(PdfContentByte cb, BaseFont bfBold, BaseFont bfNorm,
                            float W, float H,
                            LocalDate lundi, LocalDate samedi,
                            DateTimeFormatter fmt) throws Exception {

        // Fond vert
        cb.setColorFill(GREEN_JUNGLE);
        cb.rectangle(0, H - 55, W, 55);
        cb.fill();

        // Nom école (gauche)
        cb.setColorFill(WHITE);
        cb.setFontAndSize(bfBold, 20);
        cb.beginText();
        cb.setTextMatrix(25, H - 33);
        cb.showText("JungleInEnglish");
        cb.endText();

        cb.setFontAndSize(bfNorm, 8);
        cb.beginText();
        cb.setTextMatrix(25, H - 49);
        cb.showText("www.jungleinenglish.com");
        cb.endText();

        // Titre centre
        cb.setFontAndSize(bfBold, 15);
        cb.beginText();
        cb.setTextMatrix(W / 2 - 115, H - 29);
        cb.showText("Emploi du Temps Hebdomadaire");
        cb.endText();

        cb.setFontAndSize(bfNorm, 9);
        cb.beginText();
        cb.setTextMatrix(W / 2 - 95, H - 46);
        cb.showText("Semaine du " + lundi.format(fmt) + "  au  " + samedi.format(fmt));
        cb.endText();

        // Infos droite
        cb.setFontAndSize(bfNorm, 8);
        cb.beginText();
        cb.setTextMatrix(W - 215, H - 29);
        cb.showText("Annee universitaire : 2025/2026");
        cb.endText();

        cb.beginText();
        cb.setTextMatrix(W - 155, H - 46);
        cb.showText("Semestre 1");
        cb.endText();
    }

    // ─────────────────────────────────────────────────────────────────────
    // TABLE PRINCIPALE — créneaux 100% dynamiques depuis DB
    // ─────────────────────────────────────────────────────────────────────
    private void drawTable(PdfContentByte cb, BaseFont bfBold, BaseFont bfNorm,
                           float W, float H,
                           LocalDate lundi, DateTimeFormatter fmtCourt,
                           List<CreneauHoraire> tousCreneaux,
                           List<PlanningSession> sessions) throws Exception {

        int nbCreneaux = tousCreneaux.size();

        float marginLeft  = 18f;
        float tableTop    = H - 68f;
        float dayColW     = 68f;
        float tableW      = W - marginLeft - 18f;
        float creneauW    = (tableW - dayColW) / nbCreneaux;
        float headerRowH  = 32f;
        float rowH        = 60f;

        // ── En-tête : coin vert ──────────────────────────────────────────
        cb.setColorFill(GREEN_JUNGLE);
        cb.setColorStroke(BORDER);
        cb.rectangle(marginLeft, tableTop - headerRowH, dayColW, headerRowH);
        cb.fillStroke();

        // ── En-tête : une colonne par créneau (dynamique) ────────────────
        for (int t = 0; t < nbCreneaux; t++) {
            CreneauHoraire cr = tousCreneaux.get(t);
            float x = marginLeft + dayColW + t * creneauW;

            cb.setColorFill(GRAY_HEADER);
            cb.setColorStroke(BORDER);
            cb.rectangle(x, tableTop - headerRowH, creneauW, headerRowH);
            cb.fillStroke();

            // Label "HH:MM - HH:MM"
            String label = cr.getHeureDebut().toString().substring(0, 5)
                    + " - "
                    + cr.getHeureFin().toString().substring(0, 5);

            cb.setColorFill(BLACK);
            cb.setFontAndSize(bfBold, 8);
            cb.beginText();
            cb.setTextMatrix(x + creneauW / 2 - 28, tableTop - headerRowH + 12);
            cb.showText(label);
            cb.endText();

            // Label jour de la semaine du créneau si disponible
            if (cr.getJourSemaine() != null) {
                cb.setFontAndSize(bfNorm, 6);
                cb.setColorFill(DARK_GRAY);
                cb.beginText();
                cb.setTextMatrix(x + creneauW / 2 - 18, tableTop - headerRowH + 3);
                cb.showText(cr.getJourSemaine().toString());
                cb.endText();
            }
        }

        // ── Lignes : un jour par ligne ───────────────────────────────────
        for (int d = 0; d < JOURS_NOMS.length; d++) {
            LocalDate jourDate = lundi.plusDays(d);
            float y = tableTop - headerRowH - (d + 1) * rowH;

            // Cellule jour
            cb.setColorFill(GRAY_HEADER);
            cb.setColorStroke(BORDER);
            cb.rectangle(marginLeft, y, dayColW, rowH);
            cb.fillStroke();

            cb.setColorFill(DARK_GREEN);
            cb.setFontAndSize(bfBold, 9);
            cb.beginText();
            cb.setTextMatrix(marginLeft + dayColW / 2 - 18, y + rowH / 2 + 5);
            cb.showText(JOURS_NOMS[d]);
            cb.endText();

            cb.setColorFill(BLACK);
            cb.setFontAndSize(bfNorm, 7);
            cb.beginText();
            cb.setTextMatrix(marginLeft + dayColW / 2 - 20, y + rowH / 2 - 8);
            cb.showText(jourDate.format(fmtCourt) + "/2025");
            cb.endText();

            // ── Cellules sessions ────────────────────────────────────────
            for (int t = 0; t < nbCreneaux; t++) {
                CreneauHoraire cr = tousCreneaux.get(t);
                float x = marginLeft + dayColW + t * creneauW;

                // Chercher la session : même jour + même créneau (par ID)
                final LocalDate fd   = jourDate;
                final Long creneauId = cr.getIdCreneauHoraire();

                PlanningSession session = sessions.stream()
                        .filter(s -> s.getDatePlanning() != null
                                && s.getDatePlanning().equals(fd))
                        .filter(s -> s.getCreneau() != null
                                && s.getCreneau().getIdCreneauHoraire() != null
                                && s.getCreneau().getIdCreneauHoraire().equals(creneauId))
                        .findFirst()
                        .orElse(null);

                if (session != null) {
                    drawSessionCell(cb, bfBold, bfNorm, x, y, creneauW, rowH, session);
                } else {
                    // Cellule vide
                    cb.setColorFill(WHITE);
                    cb.setColorStroke(BORDER);
                    cb.rectangle(x, y, creneauW, rowH);
                    cb.fillStroke();
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // CELLULE SESSION (avec contenu)
    // ─────────────────────────────────────────────────────────────────────
    private void drawSessionCell(PdfContentByte cb, BaseFont bfBold, BaseFont bfNorm,
                                 float x, float y, float creneauW, float rowH,
                                 PlanningSession session) throws Exception {

        // Couleur fond selon matiereId
        int colorIdx = (int)(session.getMatiereId() % CELL_COLORS.length);
        cb.setColorFill(CELL_COLORS[colorIdx]);
        cb.setColorStroke(BORDER);
        cb.rectangle(x, y, creneauW, rowH);
        cb.fillStroke();

        // Barre verte gauche
        cb.setColorFill(GREEN_JUNGLE);
        cb.rectangle(x + 1, y + 2, 4, rowH - 4);
        cb.fill();

        float cx = x + creneauW / 2;

        // ── Matière ──
        cb.setColorFill(BLACK);
        cb.setFontAndSize(bfBold, 8);
        cb.beginText();
        String matiereText = "Matiere #" + session.getMatiereId();
        cb.setTextMatrix(cx - (matiereText.length() * 2.4f), y + rowH - 13);
        cb.showText(matiereText);
        cb.endText();

        // ── Enseignant ──
        cb.setColorFill(DARK_GRAY);
        cb.setFontAndSize(bfNorm, 7);
        cb.beginText();
        String ensText = "Ens. #" + session.getEnseignantId();
        cb.setTextMatrix(cx - (ensText.length() * 2.0f), y + rowH - 25);
        cb.showText(ensText);
        cb.endText();

        // ── Groupe ──
        cb.setFontAndSize(bfNorm, 7);
        cb.beginText();
        String grpText = "Groupe #" + session.getGroupeId();
        cb.setTextMatrix(cx - (grpText.length() * 2.0f), y + rowH - 36);
        cb.showText(grpText);
        cb.endText();

        // ── Badge salle ──
        String salleText = (session.getSalle() != null && session.getSalle().getNom_salle() != null)
                ? session.getSalle().getNom_salle()
                : "N/A";
        if (salleText.length() > 9) salleText = salleText.substring(0, 9);

        float badgeW  = Math.max(40f, salleText.length() * 5.5f);
        float badgeX  = cx - badgeW / 2;

        cb.setColorFill(GREEN_JUNGLE);
        cb.roundRectangle(badgeX, y + 7, badgeW, 14, 4);
        cb.fill();

        cb.setColorFill(WHITE);
        cb.setFontAndSize(bfBold, 6);
        cb.beginText();
        cb.setTextMatrix(badgeX + badgeW / 2 - (salleText.length() * 1.8f), y + 11);
        cb.showText(salleText);
        cb.endText();
    }

    // ─────────────────────────────────────────────────────────────────────
    // FOOTER
    // ─────────────────────────────────────────────────────────────────────
    private void drawFooter(PdfContentByte cb, BaseFont bfNorm,
                            float W, DateTimeFormatter fmt) throws Exception {
        cb.setColorFill(GREEN_JUNGLE);
        cb.rectangle(0, 0, W, 18);
        cb.fill();

        cb.setColorFill(WHITE);
        cb.setFontAndSize(bfNorm, 7);
        cb.beginText();
        cb.setTextMatrix(20, 5);
        cb.showText("JungleInEnglish — Genere le " + LocalDate.now().format(fmt));
        cb.endText();

        cb.beginText();
        cb.setTextMatrix(W - 55, 5);
        cb.showText("Page 1 / 1");
        cb.endText();
    }
}