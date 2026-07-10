package maker.backend.controller;

import maker.backend.dto.QrScanResultDTO;
import maker.backend.service.QrCodeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
@CrossOrigin(origins = "*")
public class QrCodeController {

    private final QrCodeService service;

    public QrCodeController(QrCodeService service) {
        this.service = service;
    }

    // ── Génération PNG ────────────────────────────────────────────────────────

    /** Génère un QR Code PNG pour un produit */
    @GetMapping(value = "/produit/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrProduit(@PathVariable Long id) throws Exception {
        return service.genererQrProduit(id);
    }

    /** Génère un QR Code PNG pour un emplacement */
    @GetMapping(value = "/emplacement/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrEmplacement(@PathVariable Long id) throws Exception {
        return service.genererQrEmplacement(id);
    }

    /** Génère un QR Code PNG pour un stock (produit + emplacement combiné) */
    @GetMapping(value = "/stock/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrStock(@PathVariable Long id) throws Exception {
        return service.genererQrStock(id);
    }

    // ── Résolution après scan ─────────────────────────────────────────────────

    /**
     * Appelé par le frontend après décodage du QR Code.
     * type = PRODUCT → données produit + stocks
     * type = LOCATION → données emplacement + produits présents
     */
    @GetMapping("/resolve")
    public QrScanResultDTO resoudre(@RequestParam String type,
                                     @RequestParam Long id) {
        return service.resoudre(type, id);
    }
}
