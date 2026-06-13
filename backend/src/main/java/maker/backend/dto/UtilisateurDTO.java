package maker.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

/**
 * DTO Utilisateur — ce qui transite entre frontend et backend.
 * Le mot de passe n'est jamais renvoyé dans les réponses (sécurité).
 */
@Data
public class UtilisateurDTO {
    private Long id;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    private String username;

    // Utilisé uniquement à la création/modification, jamais renvoyé au client
    private String motDePasse;

    @NotBlank(message = "Le nom complet est obligatoire")
    private String nomComplet;

    private boolean actif;

    // Noms des rôles (ex: "ADMIN", "GESTIONNAIRE")
    private Set<String> roles;
}
