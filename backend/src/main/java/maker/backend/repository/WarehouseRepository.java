package maker.backend.repository;

import maker.backend.entity.Entrepot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Entrepot, Long> {
}
