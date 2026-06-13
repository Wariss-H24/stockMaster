package maker.backend.repository;

import maker.backend.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    // Tous les produits non supprimés (suppression logique)
    List<Produit> findBySupprimeFalse();

    // Recherche par référence unique
    Optional<Produit> findByReferenceAndSupprimeFalse(String reference);
}
