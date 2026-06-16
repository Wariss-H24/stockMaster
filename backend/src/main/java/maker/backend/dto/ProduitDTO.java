package maker.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProduitDTO {
    private Long id;

    @NotBlank(message = "La référence est obligatoire")
    private String reference;

    private String codeBarre;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private Long categorieId;
    private String categorieNom;

    private String description;

    @Min(value = 0, message = "Le prix d'achat ne peut pas être négatif")
    private Double prixAchat;

    @Min(value = 0, message = "Le prix de vente ne peut pas être négatif")
    private Double prixVente;

    private Double poids;
    private Double volume;

    private boolean supprime;
}
