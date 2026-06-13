package maker.backend.repository;

import maker.backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    // Recherche par username pour l'authentification
    Optional<Utilisateur> findByUsername(String username);
}
