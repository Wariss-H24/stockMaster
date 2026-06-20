package maker.backend.controller;

import maker.backend.service.ReportingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reporting")
@CrossOrigin(origins = "http://localhost:3000")
public class ReportingController {

    private final ReportingService service;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    public ReportingController(ReportingService service) {
        this.service = service;
    }

    // ── PDF ──────────────────────────────────────────────────────────────────

    @GetMapping("/pdf/stock")
    public ResponseEntity<byte[]> pdfStock() throws IOException {
        return pdf(service.genererPdfStock(), "rapport_stock_" + timestamp() + ".pdf");
    }

    @GetMapping("/pdf/mouvements")
    public ResponseEntity<byte[]> pdfMouvements() throws IOException {
        return pdf(service.genererPdfMouvements(), "rapport_mouvements_" + timestamp() + ".pdf");
    }

    // ── Excel ─────────────────────────────────────────────────────────────────

    @GetMapping("/excel/stock")
    public ResponseEntity<byte[]> excelStock() throws IOException {
        return excel(service.genererExcelStock(), "stock_" + timestamp() + ".xlsx");
    }

    @GetMapping("/excel/mouvements")
    public ResponseEntity<byte[]> excelMouvements() throws IOException {
        return excel(service.genererExcelMouvements(), "mouvements_" + timestamp() + ".xlsx");
    }

    // ── CSV ───────────────────────────────────────────────────────────────────

    @GetMapping("/csv/stock")
    public ResponseEntity<byte[]> csvStock() {
        return csv(service.genererCsvStock(), "stock_" + timestamp() + ".csv");
    }

    @GetMapping("/csv/mouvements")
    public ResponseEntity<byte[]> csvMouvements() {
        return csv(service.genererCsvMouvements(), "mouvements_" + timestamp() + ".csv");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<byte[]> pdf(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    private ResponseEntity<byte[]> excel(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    private ResponseEntity<byte[]> csv(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(data);
    }

    private String timestamp() {
        return LocalDateTime.now().format(FMT);
    }
}
