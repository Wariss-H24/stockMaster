package maker.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO ZoneFr — inclut les infos de l'entrepôt parent pour l'affichage.
 */
@Data
public class ZoneFrDTO {
    private Long id;

    @NotBlank(message = "Le nom de la zone est obligatoire")
    private String nom;

    // ID de l'entrepôt parent (reçu du frontend)
    @NotNull(message = "L'entrepôt est obligatoire")
    private Long entrepotId;

    // Nom de l'entrepôt (renvoyé au frontend pour l'affichage)
    private String entrepotNom;

    @Min(0)
    private Integer capaciteTotale;

    @Min(0)
    private Integer capaciteUtilisee;

    private boolean actif;

    // Taux d'occupation calculé en %
    private Integer tauxOccupation;
}
