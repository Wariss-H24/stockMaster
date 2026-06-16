package maker.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SortieLigneDTO {
    private Long id;

    @NotNull(message = "Le produit est requis")
    private Long produitId;

    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;
}
