package maker.backend.repository;

import maker.backend.entity.Emplacement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmplacementRepository extends JpaRepository<Emplacement, Long> {
    List<Emplacement> findByZoneId(Long zoneId);
    List<Emplacement> findByZoneEntrepotId(Long entrepotId);
    List<Emplacement> findByOccupeFalse();
    boolean existsByZoneIdAndRayonAndEtagereAndCode(Long zoneId, String rayon, String etagere, String code);
}
