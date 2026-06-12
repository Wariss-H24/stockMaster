package maker.backend.repository;

import maker.backend.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Produit, Long> {
    List<Produit> findBySupprimeFalse();
}
