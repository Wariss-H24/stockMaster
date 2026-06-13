package maker.backend.repository;

import maker.backend.entity.Entrepot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntrepotRepository extends JpaRepository<Entrepot, Long> {
    // Récupérer uniquement les entrepôts actifs
    List<Entrepot> findByActifTrue();
}
