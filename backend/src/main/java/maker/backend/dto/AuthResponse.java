package maker.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * DTO renvoyé après une connexion réussie.
 * Contient le token JWT + les infos de l'utilisateur connecté.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String nomComplet;
    private Set<String> roles;
}
