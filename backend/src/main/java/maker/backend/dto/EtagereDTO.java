package maker.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EtagereDTO {
    private Long id;

    @NotBlank(message = "Le nom est requis")
    private String nom;

    private String code;

    @NotNull(message = "Le rayon est requis")
    private Long rayonId;
    private String rayonNom;

    // Chemin complet pour l'affichage
    private Long zoneId;
    private String zoneNom;
    private Long entrepotId;
    private String entrepotNom;

    private Integer niveaux;
    private boolean actif;
    private int nbEmplacements;
}
