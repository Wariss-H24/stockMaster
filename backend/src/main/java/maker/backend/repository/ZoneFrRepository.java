package maker.backend.repository;

import maker.backend.entity.ZoneFr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoneFrRepository extends JpaRepository<ZoneFr, Long> {
    // Zones actives d'un entrepôt donné
    List<ZoneFr> findByEntrepotIdAndActifTrue(Long entrepotId);
    List<ZoneFr> findByActifTrue();
}
