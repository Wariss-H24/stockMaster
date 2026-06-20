package maker.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import maker.backend.entity.MouvementStock;
import maker.backend.entity.Stock;
import maker.backend.repository.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Module 14 — Export PDF (OpenPDF), Excel (Apache POI) et CSV.
 */
@Service
@Transactional(readOnly = true)
public class ReportingService {

    private final StockRepository stockRepo;
    private final MouvementStockRepository mouvementRepo;
    private final FournisseurRepository fournisseurRepo;
    private final InventaireRepository inventaireRepo;

    private static final DateTimeFormatter FMT       = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color             NAVY       = new Color(30, 58, 95);
    private static final Color             NAVY_LIGHT = new Color(232, 238, 246);
    private static final Color             WHITE      = Color.WHITE;

    public ReportingService(StockRepository stockRepo,
                            MouvementStockRepository mouvementRepo,
                            FournisseurRepository fournisseurRepo,
                            InventaireRepository inventaireRepo) {
        this.stockRepo      = stockRepo;
        this.mouvementRepo  = mouvementRepo;
        this.fournisseurRepo = fournisseurRepo;
        this.inventaireRepo = inventaireRepo;
    }

    // ─────────────────────────────────────── PDF ────────────────────────────

    public byte[] genererPdfStock() throws IOException {
        return genererPdf("Rapport de Stock", new String[]{
                "Produit", "Référence", "Entrepôt", "Zone", "Disponible", "Réservé", "Transit"
        }, writer -> {
            for (Stock s : stockRepo.findAll()) {
                writer.addCell(cellule(s.getProduit().getNom()));
                writer.addCell(cellule(s.getProduit().getReference()));
                writer.addCell(cellule(s.getEntrepot().getNom()));
                writer.addCell(cellule(s.getZone() != null ? s.getZone().getNom() : "—"));
                writer.addCell(cellule(String.valueOf(s.getQuantiteDisponible())));
                writer.addCell(cellule(String.valueOf(s.getQuantiteReservee())));
                writer.addCell(cellule(String.valueOf(s.getQuantiteTransit())));
            }
        }, 7);
    }

    public byte[] genererPdfMouvements() throws IOException {
        return genererPdf("Rapport des Mouvements", new String[]{
                "Date", "Type", "Produit", "Entrepôt", "Quantité", "Source"
        }, writer -> {
            for (MouvementStock m : mouvementRepo.findAll()) {
                writer.addCell(cellule(m.getDate() != null ? m.getDate().format(FMT) : "—"));
                writer.addCell(cellule(m.getType().name()));
                writer.addCell(cellule(m.getStock().getProduit().getNom()));
                writer.addCell(cellule(m.getStock().getEntrepot().getNom()));
                writer.addCell(cellule(String.valueOf(m.getQuantite())));
                writer.addCell(cellule(m.getSource() != null ? m.getSource() : "—"));
            }
        }, 6);
    }

    // ─────────────────────────────────────── EXCEL ──────────────────────────

    public byte[] genererExcelStock() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Stock");
            CellStyle headerStyle = creerStyleEntete(wb);
            String[] headers = {"Produit","Référence","Entrepôt","Zone","Disponible","Réservé","Transit","Stock Min","Stock Max","Valeur (€)"};
            creerLigneEnteteExcel(sheet.createRow(0), headers, headerStyle);

            CellStyle numStyle = wb.createCellStyle();
            numStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
            int r = 1;
            for (Stock s : stockRepo.findAll()) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(s.getProduit().getNom());
                row.createCell(1).setCellValue(s.getProduit().getReference());
                row.createCell(2).setCellValue(s.getEntrepot().getNom());
                row.createCell(3).setCellValue(s.getZone() != null ? s.getZone().getNom() : "");
                row.createCell(4).setCellValue(s.getQuantiteDisponible());
                row.createCell(5).setCellValue(s.getQuantiteReservee());
                row.createCell(6).setCellValue(s.getQuantiteTransit());
                row.createCell(7).setCellValue(s.getStockMin());
                row.createCell(8).setCellValue(s.getStockMax());
                double val = s.getProduit().getPrixAchat() != null
                        ? s.getQuantiteDisponible() * s.getProduit().getPrixAchat() : 0;
                Cell vc = row.createCell(9); vc.setCellValue(val); vc.setCellStyle(numStyle);
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    public byte[] genererExcelMouvements() throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Mouvements");
            CellStyle headerStyle = creerStyleEntete(wb);
            String[] headers = {"Date","Type","Produit","Référence","Entrepôt","Quantité","Source","Commentaire"};
            creerLigneEnteteExcel(sheet.createRow(0), headers, headerStyle);
            int r = 1;
            for (MouvementStock m : mouvementRepo.findAll()) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(m.getDate() != null ? m.getDate().format(FMT) : "");
                row.createCell(1).setCellValue(m.getType().name());
                row.createCell(2).setCellValue(m.getStock().getProduit().getNom());
                row.createCell(3).setCellValue(m.getStock().getProduit().getReference());
                row.createCell(4).setCellValue(m.getStock().getEntrepot().getNom());
                row.createCell(5).setCellValue(m.getQuantite());
                row.createCell(6).setCellValue(m.getSource() != null ? m.getSource() : "");
                row.createCell(7).setCellValue(m.getCommentaire() != null ? m.getCommentaire() : "");
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    // ─────────────────────────────────────── CSV ────────────────────────────

    public byte[] genererCsvStock() {
        StringBuilder sb = new StringBuilder("\uFEFF"); // BOM UTF-8
        sb.append("Produit,Reference,Entrepot,Zone,Disponible,Reserve,Transit,StockMin,StockMax\n");
        for (Stock s : stockRepo.findAll()) {
            sb.append(csv(s.getProduit().getNom())).append(",")
              .append(csv(s.getProduit().getReference())).append(",")
              .append(csv(s.getEntrepot().getNom())).append(",")
              .append(csv(s.getZone() != null ? s.getZone().getNom() : "")).append(",")
              .append(s.getQuantiteDisponible()).append(",")
              .append(s.getQuantiteReservee()).append(",")
              .append(s.getQuantiteTransit()).append(",")
              .append(s.getStockMin()).append(",")
              .append(s.getStockMax()).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] genererCsvMouvements() {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("Date,Type,Produit,Reference,Entrepot,Quantite,Source,Commentaire\n");
        for (MouvementStock m : mouvementRepo.findAll()) {
            sb.append(csv(m.getDate() != null ? m.getDate().format(FMT) : "")).append(",")
              .append(m.getType()).append(",")
              .append(csv(m.getStock().getProduit().getNom())).append(",")
              .append(csv(m.getStock().getProduit().getReference())).append(",")
              .append(csv(m.getStock().getEntrepot().getNom())).append(",")
              .append(m.getQuantite()).append(",")
              .append(csv(m.getSource())).append(",")
              .append(csv(m.getCommentaire())).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // ─────────────────────────── HELPERS PDF (OpenPDF) ──────────────────────

    @FunctionalInterface
    private interface TableFiller { void fill(PdfPTable table) throws DocumentException; }

    private byte[] genererPdf(String titre, String[] headers, TableFiller filler, int cols)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        PdfWriter.getInstance(doc, bos);
        doc.open();

        // En-tête
        Font titreFont = new Font(Font.HELVETICA, 16, Font.BOLD, NAVY);
        Font subFont   = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);
        doc.add(new Paragraph(titre, titreFont));
        doc.add(new Paragraph("Généré le " + LocalDateTime.now().format(FMT), subFont));
        doc.add(Chunk.NEWLINE);

        // Tableau
        PdfPTable table = new PdfPTable(cols);
        table.setWidthPercentage(100);

        // Ligne d'en-tête
        Font hFont = new Font(Font.HELVETICA, 9, Font.BOLD, WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, hFont));
            cell.setBackgroundColor(NAVY);
            cell.setPadding(6);
            cell.setBorderColor(NAVY);
            table.addCell(cell);
        }

        try { filler.fill(table); } catch (DocumentException e) { throw new IOException(e); }

        doc.add(table);
        doc.close();
        return bos.toByteArray();
    }

    private PdfPCell cellule(String texte) {
        Font f = new Font(Font.HELVETICA, 8);
        PdfPCell cell = new PdfPCell(new Phrase(texte != null ? texte : "", f));
        cell.setPadding(4);
        cell.setBorderColor(new Color(226, 232, 240));
        return cell;
    }

    // ─────────────────────────── HELPERS EXCEL ──────────────────────────────

    private CellStyle creerStyleEntete(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void creerLigneEnteteExcel(org.apache.poi.ss.usermodel.Row row, String[] headers, CellStyle style) {
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private String csv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
