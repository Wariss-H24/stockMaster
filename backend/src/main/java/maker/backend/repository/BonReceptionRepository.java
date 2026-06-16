package maker.backend.repository;

import maker.backend.entity.BonReception;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BonReceptionRepository extends JpaRepository<BonReception, Long> {
    List<BonReception> findByFournisseurIdOrderByDateDesc(Long fournisseurId);
}
