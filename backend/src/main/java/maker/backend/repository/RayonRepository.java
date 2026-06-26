package maker.backend.repository;

import maker.backend.entity.Rayon;
import maker.backend.entity.ZoneFr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RayonRepository extends JpaRepository<Rayon, Long> {
    List<Rayon> findByZoneOrderByNomAsc(ZoneFr zone);
    List<Rayon> findByZoneIdOrderByNomAsc(Long zoneId);
    List<Rayon> findByActifTrueOrderByNomAsc();
    boolean existsByCode(String code);
}
