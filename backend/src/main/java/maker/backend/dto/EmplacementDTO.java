package maker.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmplacementDTO {
    private Long id;

    @NotNull(message = "La zone est requise")
    private Long zoneId;
    private String zoneNom;
    private String entrepotNom;

    @NotBlank(message = "Le rayon est requis")
    private String rayon;

    @NotBlank(message = "L'étagère est requise")
    private String etagere;

    @NotBlank(message = "Le code est requis")
    private String code;

    private boolean occupe;
    private String codeComplet;
}
