package maker.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmplacementDTO {
    private Long id;

    @NotBlank(message = "Le nom est requis")
    private String nom;

    @NotBlank(message = "Le code est requis")
    private String code;

    @NotNull(message = "L'étagère est requise")
    private Long etagereId;
    private String etagereNom;

    // Chemin complet pour l'affichage
    private Long rayonId;
    private String rayonNom;
    private Long zoneId;
    private String zoneNom;
    private Long entrepotId;
    private String entrepotNom;

    /** Code complet : ENT-001/ZONE-A/RAYON-03/ETAGERE-02/EMP-12 */
    private String codeComplet;

    private Integer capaciteMax;
    private Integer capaciteUtilisee;
    private boolean actif;

    /** Taux d'occupation en % */
    private Integer tauxOccupation;
}
