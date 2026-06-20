package maker.backend.dto;

import lombok.Data;

@Data
public class CommandeLigneDTO {
    private Long id;
    private Long produitId;
    private String produitNom;
    private Integer quantite;
    private Double prixUnitaire;
    private Double sousTotal;
}
