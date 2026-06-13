package maker.backend.mapper;

import maker.backend.dto.UtilisateurDTO;
import maker.backend.entity.Role;
import maker.backend.entity.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper manuel Utilisateur ↔ UtilisateurDTO.
 * Évite d'exposer l'entité JPA directement dans l'API.
 */
@Component
public class UtilisateurMapper {

    // Convertit l'entité vers le DTO (le motDePasse n'est jamais inclus dans la réponse)
    public UtilisateurDTO toDTO(Utilisateur u) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setNomComplet(u.getNomComplet());
        dto.setActif(u.isActif());
        dto.setRoles(u.getRoles().stream().map(Role::getNom).collect(Collectors.toSet()));
        return dto;
    }

    // Convertit le DTO vers l'entité (sans gérer les rôles ici, fait dans le service)
    public Utilisateur toEntity(UtilisateurDTO dto) {
        Utilisateur u = new Utilisateur();
        u.setUsername(dto.getUsername());
        u.setNomComplet(dto.getNomComplet());
        u.setMotDePasse(dto.getMotDePasse());
        u.setActif(dto.isActif());
        return u;
    }
}
