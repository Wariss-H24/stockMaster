package maker.backend.controller;

import maker.backend.service.QrCodeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/qrcode")
@CrossOrigin(origins = "http://localhost:3000")
public class QrCodeController {

    private final QrCodeService service;

    public QrCodeController(QrCodeService service) { this.service = service; }

    @GetMapping("/produit/{id}")
    public Map<String, String> qrProduit(@PathVariable Long id) {
        return Map.of("base64", service.genererQrProduit(id));
    }

    @GetMapping("/emplacement/{id}")
    public Map<String, String> qrEmplacement(@PathVariable Long id) {
        return Map.of("base64", service.genererQrEmplacement(id));
    }
}
