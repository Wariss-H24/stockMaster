package maker.backend.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import maker.backend.dto.QrScanResultDTO;
import maker.backend.entity.*;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Module 18 — Génération QR Code et résolution après scan.
 *
 * QR encode une URL : http://<host>/scan?type=PRODUCT&id=42
 *                     http://<host>/scan?type=LOCATION&id=7
 *
 * Le frontend intercepte /scan → appelle /api/qr/resolve?type=…&id=…
 * → reçoit les données structurées.
 */
@Service
@Transactional(readOnly = true)
public class QrCodeService {

    private final ProduitRepository produitRepo;
    private final EmplacementRepository emplacementRepo;
    private final StockRepository stockRepo;

    @Value("${stockmaster.app.url:http://localhost:3000}")
    private String appUrl;

    public QrCodeService(ProduitRepository produitRepo,
                         EmplacementRepository emplacementRepo,
                         StockRepository stockRepo) {
        this.produitRepo     = produitRepo;
        this.emplacementRepo = emplacementRepo;
        this.stockRepo       = stockRepo;
    }

    // ── Génération PNG ────────────────────────────────────────────────────────

    /** QR Code pour un produit — encode l'URL de résolution */
    public byte[] genererQrProduit(Long produitId) throws Exception {
        Produit p = produitRepo.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + produitId));
        String url = appUrl + "/scan?type=PRODUCT&id=" + produitId
                + "&ref=" + encode(p.getReference())
                + "&nom=" + encode(p.getNom());
        return genererPng(url, 300);
    }

    /** QR Code pour un emplacement — encode l'URL de résolution */
    public byte[] genererQrEmplacement(Long emplacementId) throws Exception {
        Emplacement emp = emplacementRepo.findById(emplacementId)
                .orElseThrow(() -> new ResourceNotFoundException("Emplacement introuvable : " + emplacementId));
        String codeComplet = EmplacementService.buildCodeComplet(emp);
        String url = appUrl + "/scan?type=LOCATION&id=" + emplacementId
                + "&code=" + encode(emp.getCode())
                + "&complet=" + encode(codeComplet);
        return genererPng(url, 300);
    }

    /** QR Code pour un stock (produit + emplacement) */
    public byte[] genererQrStock(Long stockId) throws Exception {
        Stock s = stockRepo.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock introuvable : " + stockId));
        String prodRef = s.getProduit().getReference();
        String empCode = s.getEmplacement() != null ? EmplacementService.buildCodeComplet(s.getEmplacement()) : "N/A";
        String url = appUrl + "/scan?type=PRODUCT&id=" + s.getProduit().getId()
                + "&ref=" + encode(prodRef) + "&emp=" + encode(empCode);
        return genererPng(url, 300);
    }

    // ── Résolution après scan ─────────────────────────────────────────────────

    /** Appelé par le frontend après décodage du QR — retourne les données complètes */
    public QrScanResultDTO resoudre(String type, Long id) {
        if ("PRODUCT".equalsIgnoreCase(type)) {
            return resoudreProduit(id);
        } else if ("LOCATION".equalsIgnoreCase(type)) {
            return resoudreEmplacement(id);
        }
        throw new IllegalArgumentException("Type QR inconnu : " + type);
    }

    private QrScanResultDTO resoudreProduit(Long produitId) {
        Produit p = produitRepo.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + produitId));

        QrScanResultDTO result = new QrScanResultDTO();
        result.setType("PRODUCT");
        result.setProduitId(p.getId());
        result.setProduitReference(p.getReference());
        result.setProduitNom(p.getNom());
        result.setProduitCategorie(p.getCategorie() != null ? p.getCategorie().getNom() : null);
        result.setProduitDescription(p.getDescription());
        result.setProduitPrixAchat(p.getPrixAchat());
        result.setProduitPrixVente(p.getPrixVente());
        result.setProduitPoids(p.getPoids());

        // Tous les stocks de ce produit
        List<Stock> stocks = stockRepo.findByProduit(p);
        int total = stocks.stream()
                .mapToInt(s -> s.getQuantiteDisponible() != null ? s.getQuantiteDisponible() : 0)
                .sum();
        result.setStockTotal(total);

        List<QrScanResultDTO.StockDetailDTO> details = stocks.stream().map(s -> {
            QrScanResultDTO.StockDetailDTO d = new QrScanResultDTO.StockDetailDTO();
            d.setStockId(s.getId());
            d.setEntrepotNom(s.getEntrepot().getNom());
            d.setZoneNom(s.getZone() != null ? s.getZone().getNom() : null);
            if (s.getEmplacement() != null) {
                d.setEmplacementCode(s.getEmplacement().getCode());
                d.setEmplacementCodeComplet(EmplacementService.buildCodeComplet(s.getEmplacement()));
            }
            d.setQuantiteDisponible(s.getQuantiteDisponible());
            d.setQuantiteReservee(s.getQuantiteReservee());
            d.setQuantiteTransit(s.getQuantiteTransit());
            d.setStockMin(s.getStockMin());
            return d;
        }).collect(Collectors.toList());
        result.setStocks(details);

        return result;
    }

    private QrScanResultDTO resoudreEmplacement(Long emplacementId) {
        Emplacement emp = emplacementRepo.findById(emplacementId)
                .orElseThrow(() -> new ResourceNotFoundException("Emplacement introuvable : " + emplacementId));

        QrScanResultDTO result = new QrScanResultDTO();
        result.setType("LOCATION");
        result.setEmplacementId(emp.getId());
        result.setEmplacementCode(emp.getCode());
        result.setEmplacementCodeComplet(EmplacementService.buildCodeComplet(emp));
        result.setEtagereNom(emp.getEtagere().getNom());
        result.setRayonNom(emp.getEtagere().getRayon().getNom());
        result.setZoneNom(emp.getEtagere().getRayon().getZone().getNom());
        result.setEntrepotNom(emp.getEtagere().getRayon().getZone().getEntrepot().getNom());
        result.setCapaciteMax(emp.getCapaciteMax());
        result.setCapaciteUtilisee(emp.getCapaciteUtilisee());

        // Produits présents à cet emplacement
        List<Stock> stocks = stockRepo.findAll().stream()
                .filter(s -> s.getEmplacement() != null && s.getEmplacement().getId().equals(emplacementId))
                .collect(Collectors.toList());

        List<QrScanResultDTO.ProduitStockDTO> produits = stocks.stream().map(s -> {
            QrScanResultDTO.ProduitStockDTO pd = new QrScanResultDTO.ProduitStockDTO();
            pd.setProduitId(s.getProduit().getId());
            pd.setProduitReference(s.getProduit().getReference());
            pd.setProduitNom(s.getProduit().getNom());
            pd.setCategorieNom(s.getProduit().getCategorie() != null ? s.getProduit().getCategorie().getNom() : null);
            pd.setQuantiteDisponible(s.getQuantiteDisponible());
            pd.setStockMin(s.getStockMin());
            int dispo = s.getQuantiteDisponible() != null ? s.getQuantiteDisponible() : 0;
            int min   = s.getStockMin()          != null ? s.getStockMin()           : 0;
            if (dispo <= 0 || (min > 0 && dispo <= min / 2))      pd.setStatut("CRITIQUE");
            else if (min > 0 && dispo <= min)                      pd.setStatut("FAIBLE");
            else                                                    pd.setStatut("OK");
            return pd;
        }).collect(Collectors.toList());
        result.setProduitsPresents(produits);

        return result;
    }

    // ── Génération PNG ZXing ──────────────────────────────────────────────────

    private byte[] genererPng(String content, int taille) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, taille, taille, hints);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", bos);
        return bos.toByteArray();
    }

    private String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}
