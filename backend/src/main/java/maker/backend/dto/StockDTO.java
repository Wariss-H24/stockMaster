package maker.backend.dto;

import lombok.Data;

@Data
public class StockDTO {
    private Long id;
    private Long produitId;
    private String produitReference;
    private String produitNom;
    private Long entrepotId;
    private String entrepotNom;
    private Long zoneId;
    private String zoneNom;
    private Long emplacementId;
    private String emplacementCode;
    private String emplacementCodeComplet;
    private Integer quantiteDisponible;
    private Integer quantiteReservee;
    private Integer quantiteTransit;
    private Integer stockMin;
    private Integer stockMax;
}
