package maker.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlerteDTO {
    private Long stockId;
    private Long produitId;
    private String produitNom;
    private String produitReference;
    private Long entrepotId;
    private String entrepotNom;
    private String zoneNom;
    private Integer quantiteDisponible;
    private Integer stockMin;
    private String niveau; // CRITIQUE, FAIBLE, OK
    private LocalDateTime dateDetection;
}
