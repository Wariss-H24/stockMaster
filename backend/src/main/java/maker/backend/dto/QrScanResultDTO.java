package maker.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * Module 18 — Résultat renvoyé après décodage du QR Code.
 * type = "PRODUCT" → infos produit + stock
 * type = "LOCATION" → infos emplacement + liste produits
 */
@Data
public class QrScanResultDTO {

    /** PRODUCT ou LOCATION */
    private String type;

    // ── PRODUCT ──────────────────────────────────────────────────────────────
    private Long   produitId;
    private String produitReference;
    private String produitNom;
    private String produitCategorie;
    private String produitDescription;
    private Double produitPrixAchat;
    private Double produitPrixVente;
    private Double produitPoids;

    /** Stock total disponible (tous entrepôts) */
    private Integer stockTotal;

    /** Détail des stocks par emplacement */
    private List<StockDetailDTO> stocks;

    // ── LOCATION ─────────────────────────────────────────────────────────────
    private Long   emplacementId;
    private String emplacementCode;
    private String emplacementCodeComplet;
    private String zoneNom;
    private String rayonNom;
    private String etagereNom;
    private String entrepotNom;
    private Integer capaciteMax;
    private Integer capaciteUtilisee;

    /** Produits présents à cet emplacement */
    private List<ProduitStockDTO> produitsPresents;

    // ── Sous-DTOs ─────────────────────────────────────────────────────────────

    @Data
    public static class StockDetailDTO {
        private Long    stockId;
        private String  entrepotNom;
        private String  zoneNom;
        private String  emplacementCode;
        private String  emplacementCodeComplet;
        private Integer quantiteDisponible;
        private Integer quantiteReservee;
        private Integer quantiteTransit;
        private Integer stockMin;
    }

    @Data
    public static class ProduitStockDTO {
        private Long    produitId;
        private String  produitReference;
        private String  produitNom;
        private String  categorieNom;
        private Integer quantiteDisponible;
        private Integer stockMin;
        private String  statut; // OK, FAIBLE, CRITIQUE
    }
}
