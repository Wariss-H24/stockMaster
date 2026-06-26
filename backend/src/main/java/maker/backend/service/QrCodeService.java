package maker.backend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import maker.backend.entity.Emplacement;
import maker.backend.entity.Produit;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.repository.EmplacementRepository;
import maker.backend.repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

@Service
public class QrCodeService {

    private final ProduitRepository produitRepo;
    private final EmplacementRepository emplacementRepo;

    public QrCodeService(ProduitRepository produitRepo, EmplacementRepository emplacementRepo) {
        this.produitRepo = produitRepo;
        this.emplacementRepo = emplacementRepo;
    }

    public String genererQrProduit(Long id) {
        Produit p = produitRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
        String contenu = "PRODUIT|" + p.getReference() + "|" + p.getNom() + "|" + (p.getCodeBarre() != null ? p.getCodeBarre() : "");
        return generer(contenu);
    }

    public String genererQrEmplacement(Long id) {
        Emplacement e = emplacementRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Emplacement introuvable : " + id));
        String contenu = "EMPLACEMENT|" + e.getCodeComplet();
        return generer(contenu);
    }

    private String generer(String contenu) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN, 1
            );
            BitMatrix matrix = writer.encode(contenu, BarcodeFormat.QR_CODE, 300, 300, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération QR code : " + e.getMessage());
        }
    }
}
