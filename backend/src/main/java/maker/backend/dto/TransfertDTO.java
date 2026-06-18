package maker.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransfertDTO {

    @NotNull(message = "Le produit est requis")
    private Long produitId;

    @NotNull(message = "L'entrepôt source est requis")
    private Long sourceEntrepotId;

    private Long sourceZoneId;

    @NotNull(message = "L'entrepôt de destination est requis")
    private Long destinationEntrepotId;

    private Long destinationZoneId;

    @NotNull(message = "La quantité est requise")
    @Min(value = 1, message = "La quantité de transfert doit être au moins de 1")
    private Integer quantite;

    private String commentaire;
}
