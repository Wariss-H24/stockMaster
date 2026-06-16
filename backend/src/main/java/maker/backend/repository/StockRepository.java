package maker.backend.repository;

import maker.backend.entity.Entrepot;
import maker.backend.entity.Produit;
import maker.backend.entity.Stock;
import maker.backend.entity.ZoneFr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findAll();
    Optional<Stock> findByProduitAndEntrepotAndZone(Produit produit, Entrepot entrepot, ZoneFr zone);
    Optional<Stock> findByProduitAndEntrepot(Produit produit, Entrepot entrepot);
}
