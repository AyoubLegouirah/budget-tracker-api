package com.ayoub.budgettracker.service;

import com.ayoub.budgettracker.entity.Transaction;
import com.ayoub.budgettracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final TransactionRepository transactionRepository;

    private static final float MARGIN = 50f;
    private static final float W  = PDRectangle.A4.getWidth();
    private static final float H  = PDRectangle.A4.getHeight();
    private static final float CW = W - 2 * MARGIN;

    private static final Color C_HEADER  = new Color(30, 58, 95);
    private static final Color C_INCOME  = new Color(22, 163, 74);
    private static final Color C_EXPENSE = new Color(220, 38, 38);
    private static final Color C_TEXT    = new Color(20, 20, 20);
    private static final Color C_GRAY    = new Color(110, 110, 110);
    private static final Color C_SEP     = new Color(200, 210, 220);

    public byte[] generateMonthlyReport(UUID userId, YearMonth month) throws IOException {
        LocalDate from = month.atDay(1);
        LocalDate to   = month.atEndOfMonth();

        List<Transaction> txs = transactionRepository.findByUserIdAndDateBetween(userId, from, to);

        // ── aggregate ────────────────────────────────────────────────────
        BigDecimal totalIncome  = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<UUID, CatRow> catMap = new LinkedHashMap<>();

        for (Transaction t : txs) {
            if ("INCOME".equals(t.getType())) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpense = totalExpense.add(t.getAmount());
                catMap.merge(
                    t.getCategory().getId(),
                    new CatRow(t.getCategory().getIcon(), t.getCategory().getName(), t.getAmount()),
                    (a, b) -> new CatRow(a.icon, a.name, a.amount.add(b.amount))
                );
            }
        }
        List<CatRow> categories = catMap.values().stream()
            .sorted(Comparator.comparing((CatRow r) -> r.amount).reversed())
            .toList();

        List<Transaction> last50 = txs.stream()
            .sorted(Comparator.comparing(Transaction::getDate).reversed())
            .limit(50)
            .toList();

        BigDecimal net = totalIncome.subtract(totalExpense);

        // ── build PDF ────────────────────────────────────────────────────
        try (PDDocument doc = new PDDocument()) {
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font bold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            try {
                float y = H - MARGIN;

                // ── header block ─────────────────────────────────────────
                float hdrH = 62f;
                fillRect(cs, 0, H - hdrH, W, hdrH, C_HEADER);

                String monthFr = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH));
                monthFr = Character.toUpperCase(monthFr.charAt(0)) + monthFr.substring(1);
                drawText(cs, bold,    16, "BudgetTracker - Bilan de " + monthFr, MARGIN, H - 30, Color.WHITE);
                drawText(cs, regular,  9,
                    "Genere le " + LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)),
                    MARGIN, H - 48, new Color(160, 190, 225));
                y = H - hdrH - 24;

                // ── stats ─────────────────────────────────────────────────
                sectionTitle(cs, bold, "RESUME DU MOIS", y);
                y -= 7; hline(cs, y); y -= 16;

                float bw = (CW - 20) / 3f;
                float bh = 54f;
                statBox(cs, regular, bold, MARGIN,              y - bh, bw, bh, "Total revenus",  eur(totalIncome),  C_INCOME);
                statBox(cs, regular, bold, MARGIN + bw + 10,    y - bh, bw, bh, "Total depenses", eur(totalExpense), C_EXPENSE);
                statBox(cs, regular, bold, MARGIN + 2*(bw+10),  y - bh, bw, bh, "Epargne nette",  eur(net),          net.signum() >= 0 ? C_INCOME : C_EXPENSE);
                y -= bh + 24;

                // ── category section ──────────────────────────────────────
                if (!categories.isEmpty()) {
                    sectionTitle(cs, bold, "DEPENSES PAR CATEGORIE", y);
                    y -= 7; hline(cs, y); y -= 16;

                    for (CatRow r : categories) {
                        if (y < MARGIN + 22) break;
                        BigDecimal pct = totalExpense.signum() == 0 ? BigDecimal.ZERO
                            : r.amount.multiply(BigDecimal.valueOf(100))
                                      .divide(totalExpense, 1, RoundingMode.HALF_UP);
                        String right = eur(r.amount) + "  (" + pct + "%)";
                        drawDotLine(cs, regular, 9, r.name, right, y, C_TEXT, C_EXPENSE);
                        y -= 17;
                    }
                    y -= 10;
                }

                // ── transactions section ──────────────────────────────────
                if (!last50.isEmpty()) {
                    if (y < 160) { cs = newPage(doc, cs); y = H - MARGIN; }

                    sectionTitle(cs, bold, "DERNIERES TRANSACTIONS", y);
                    y -= 7; hline(cs, y); y -= 16;

                    DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    for (Transaction t : last50) {
                        if (y < MARGIN + 30) { cs = newPage(doc, cs); y = H - MARGIN; }
                        boolean income = "INCOME".equals(t.getType());
                        Color ac   = income ? C_INCOME : C_EXPENSE;
                        String sign = income ? "+ " : "- ";
                        String line1 = t.getDate().format(df)
                            + "  -  " + clip(t.getDescription(), 35)
                            + "  (" + clip(t.getCategory().getName(), 18) + ")";
                        drawText(cs, regular, 9, line1, MARGIN, y, C_TEXT);
                        y -= 13;
                        drawTextRight(cs, regular, 9, sign + eur(t.getAmount()), W - MARGIN, y, ac);
                        y -= 18;
                    }
                }
            } finally {
                cs.close();
            }

            // ── footer on every page ──────────────────────────────────────
            int n = doc.getNumberOfPages();
            for (int i = 0; i < n; i++) {
                try (PDPageContentStream f = new PDPageContentStream(
                        doc, doc.getPage(i), AppendMode.APPEND, true)) {
                    hline(f, MARGIN - 8);
                    drawText(f, regular, 8,
                        "BudgetTracker  -  Page " + (i + 1) + " / " + n,
                        MARGIN, MARGIN - 22, C_GRAY);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    // ── page management ───────────────────────────────────────────────────

    private PDPageContentStream newPage(PDDocument doc, PDPageContentStream current) throws IOException {
        current.close();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        return new PDPageContentStream(doc, page);
    }

    // ── section helpers ───────────────────────────────────────────────────

    private void sectionTitle(PDPageContentStream cs, PDType1Font bold,
                               String title, float y) throws IOException {
        drawText(cs, bold, 10, title, MARGIN, y, C_GRAY);
    }

    private void drawDotLine(PDPageContentStream cs, PDType1Font font, float size,
                              String left, String right, float y,
                              Color leftColor, Color rightColor) throws IOException {
        float leftW  = font.getStringWidth(left)  / 1000f * size;
        float rightW = font.getStringWidth(right) / 1000f * size;
        float dotW   = font.getStringWidth(".")   / 1000f * size;

        drawText(cs, font, size, left,  MARGIN,                  y, leftColor);
        drawText(cs, font, size, right, W - MARGIN - rightW,     y, rightColor);

        float dotsStart = MARGIN + leftW + 4f;
        float dotsEnd   = W - MARGIN - rightW - 4f;
        int numDots = (dotsEnd > dotsStart && dotW > 0)
            ? (int) ((dotsEnd - dotsStart) / dotW) : 0;
        if (numDots > 0) {
            drawText(cs, font, size, ".".repeat(numDots), dotsStart, y, C_SEP);
        }
    }

    private void statBox(PDPageContentStream cs, PDType1Font regular, PDType1Font bold,
                          float x, float y, float w, float h,
                          String label, String value, Color valueColor) throws IOException {
        fillRect(cs, x, y, w, h, new Color(247, 248, 250));
        cs.setStrokingColor(C_SEP);
        cs.setLineWidth(0.5f);
        cs.addRect(x, y, w, h);
        cs.stroke();
        drawText(cs, regular,  8, label, x + 10, y + h - 16, C_GRAY);
        drawText(cs, bold,    14, value, x + 10, y + 12,      valueColor);
    }

    // ── drawing primitives ────────────────────────────────────────────────

    private void drawText(PDPageContentStream cs, PDType1Font font, float size,
                           String text, float x, float y, Color color) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private void drawTextRight(PDPageContentStream cs, PDType1Font font, float size,
                                String text, float rightX, float y, Color color) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * size;
        drawText(cs, font, size, text, rightX - textWidth, y, color);
    }

    private void fillRect(PDPageContentStream cs, float x, float y,
                           float w, float h, Color color) throws IOException {
        cs.setNonStrokingColor(color);
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    private void hline(PDPageContentStream cs, float y) throws IOException {
        cs.setStrokingColor(C_SEP);
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y);
        cs.lineTo(W - MARGIN, y);
        cs.stroke();
    }

    // ── text utils ────────────────────────────────────────────────────────

    private String eur(BigDecimal amount) {
        return String.format(Locale.US, "%,.2f EUR", amount).replace(",", " ");
    }

    private String clip(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "." : s;
    }

    // ── inner types ───────────────────────────────────────────────────────

    private record CatRow(String icon, String name, BigDecimal amount) {}
}
