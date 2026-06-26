package maker.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SortieLigneDTO {
    private Long id;

    @NotNull(message = "Le produit est requis")
    private Long produitId;
    private String produitNom;
    private String produitReference;

    /** Code complet de l'emplacement où se trouve ce produit */
    private String emplacementCodeComplet;

    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;
}
