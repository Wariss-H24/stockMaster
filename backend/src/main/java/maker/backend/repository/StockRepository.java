package maker.backend.repository;

import maker.backend.entity.Entrepot;
import maker.backend.entity.Produit;
import maker.backend.entity.Stock;
import maker.backend.entity.ZoneFr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findAll();
    Optional<Stock> findByProduitAndEntrepotAndZone(Produit produit, Entrepot entrepot, ZoneFr zone);
    Optional<Stock> findByProduitAndEntrepot(Produit produit, Entrepot entrepot);
    List<Stock> findByEntrepot(Entrepot entrepot);
    List<Stock> findByZone(ZoneFr zone);

    @Query("SELECT COALESCE(SUM(s.quantiteDisponible * s.produit.volume), 0) FROM Stock s WHERE s.entrepot = :entrepot AND s.produit.volume IS NOT NULL")
    Double sumEspaceUtiliseByEntrepot(@Param("entrepot") Entrepot entrepot);

    @Query("SELECT COALESCE(SUM(s.quantiteDisponible * s.produit.volume), 0) FROM Stock s WHERE s.zone = :zone AND s.produit.volume IS NOT NULL")
    Double sumEspaceUtiliseByZone(@Param("zone") ZoneFr zone);
}
