package maker.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategorieDTO {
    private Long id;

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    private String nom;

    private String description;

    private boolean actif;
}
