package maker.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO Entrepot — données exposées et reçues par l'API REST.
 */
@Data
public class EntrepotDTO {
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    private String responsable;

    @Min(value = 0, message = "La capacité totale ne peut pas être négative")
    private Integer capaciteTotale;

    @Min(value = 0, message = "La capacité utilisée ne peut pas être négative")
    private Integer capaciteUtilisee;

    private boolean actif;

    // Calculé côté backend : taux d'occupation en %
    private Integer tauxOccupation;
}
