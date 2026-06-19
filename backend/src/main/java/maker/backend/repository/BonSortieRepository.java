package maker.backend.repository;

import maker.backend.entity.BonSortie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BonSortieRepository extends JpaRepository<BonSortie, Long> {

    List<BonSortie> findAllByOrderByDateDesc();

    /** Compteur pour générer la référence BSO-XXXX */
    long countBy();
}
