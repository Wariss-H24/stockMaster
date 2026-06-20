package maker.backend.repository;

import maker.backend.entity.CommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommandeFournisseurRepository extends JpaRepository<CommandeFournisseur, Long> {
    List<CommandeFournisseur> findAllByOrderByDateCreaDesc();
    long countBy();

    @Query("SELECT COUNT(c) FROM CommandeFournisseur c WHERE c.statut = :statut")
    long countByStatut(@Param("statut") CommandeFournisseur.Statut statut);
}
