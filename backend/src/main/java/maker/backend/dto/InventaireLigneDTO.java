package maker.backend.dto;

import lombok.Data;

@Data
public class InventaireLigneDTO {
    private Long id;
    private Long produitId;
    private String produitNom;
    private String produitReference;
    /** Code complet de l'emplacement où se trouve le produit */
    private String emplacementCodeComplet;
    private Integer quantiteSysteme;
    private Integer quantitePhysique;
    private Integer ecart;
    private String commentaire;
}
