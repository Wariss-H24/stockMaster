package maker.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MouvementStockDTO {
    private Long id;
    private Long stockId;
    private String produitReference;
    private String produitNom;
    private String entrepotNom;
    private String zoneNom;
    private String type;
    private Integer quantite;
    private LocalDateTime date;
    private String source;
    private String commentaire;
}
