package maker.backend.repository;

import maker.backend.entity.Emplacement;
import maker.backend.entity.Etagere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmplacementRepository extends JpaRepository<Emplacement, Long> {
    List<Emplacement> findByEtagereOrderByNomAsc(Etagere etagere);
    List<Emplacement> findByEtagereIdOrderByNomAsc(Long etagereId);
    Optional<Emplacement> findByCode(String code);
    boolean existsByCode(String code);

    /** Tous les emplacements d'un entrepôt (navigation hiérarchique) */
    @Query("SELECT e FROM Emplacement e WHERE " +
           "e.etagere.rayon.zone.entrepot.id = :entrepotId AND e.actif = true " +
           "ORDER BY e.etagere.rayon.zone.nom, e.etagere.rayon.nom, e.etagere.nom, e.nom")
    List<Emplacement> findByEntrepotId(@Param("entrepotId") Long entrepotId);

    /** Tous les emplacements d'une zone */
    @Query("SELECT e FROM Emplacement e WHERE " +
           "e.etagere.rayon.zone.id = :zoneId AND e.actif = true " +
           "ORDER BY e.etagere.rayon.nom, e.etagere.nom, e.nom")
    List<Emplacement> findByZoneId(@Param("zoneId") Long zoneId);
}
