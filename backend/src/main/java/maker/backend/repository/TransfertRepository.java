package maker.backend.repository;

import maker.backend.entity.Transfert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransfertRepository extends JpaRepository<Transfert, Long> {
    List<Transfert> findAllByOrderByDateCreaDesc();
    Optional<Transfert> findByReference(String reference);
    // Compteur pour générer la référence TRF-XXXX
    long countBy();
}
