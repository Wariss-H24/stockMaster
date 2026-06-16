package maker.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FournisseurDTO {
    private Long id;

    @NotBlank(message = "Le nom du fournisseur est obligatoire")
    private String nom;

    private String adresse;

    private String telephone;

    @Email(message = "L'email doit être valide")
    private String email;

    private String contactPrincipal;

    private boolean actif;
}
