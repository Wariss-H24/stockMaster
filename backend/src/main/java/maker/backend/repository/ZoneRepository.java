package maker.backend.repository;

import maker.backend.entity.ZoneFr;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<ZoneFr, Long> {
}
