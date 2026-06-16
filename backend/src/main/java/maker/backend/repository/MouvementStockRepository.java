package maker.backend.repository;

import maker.backend.entity.MouvementStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {
    List<MouvementStock> findByStockIdOrderByDateDesc(Long stockId);
    List<MouvementStock> findByTypeOrderByDateDesc(MouvementStock.Type type);
}
