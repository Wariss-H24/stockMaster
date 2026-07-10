package maker.backend.repository;

import maker.backend.entity.Etagere;
import maker.backend.entity.Rayon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtagereRepository extends JpaRepository<Etagere, Long> {
    List<Etagere> findByRayonOrderByNomAsc(Rayon rayon);
    List<Etagere> findByRayonIdOrderByNomAsc(Long rayonId);
    boolean existsByCode(String code);
}
