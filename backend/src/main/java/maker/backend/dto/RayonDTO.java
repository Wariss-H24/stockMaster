package maker.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RayonDTO {
    private Long id;

    @NotBlank(message = "Le nom est requis")
    private String nom;

    private String code;

    @NotNull(message = "La zone est requise")
    private Long zoneId;
    private String zoneNom;

    // Pour afficher le chemin complet
    private Long entrepotId;
    private String entrepotNom;

    private String description;
    private boolean actif;
    private int nbEtageres;
}
