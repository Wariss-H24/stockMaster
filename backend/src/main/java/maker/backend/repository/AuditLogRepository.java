package maker.backend.repository;

import maker.backend.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByDateDesc(Pageable pageable);

    List<AuditLog> findByEntiteAndEntiteIdOrderByDateDesc(String entite, Long entiteId);

    List<AuditLog> findByUtilisateurOrderByDateDesc(String utilisateur);

    /**
     * Recherche avec filtres optionnels.
     * Les paramètres null sont ignorés grâce aux conditions OR IS NULL.
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:entite IS NULL OR a.entite = :entite) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:utilisateur IS NULL OR a.utilisateur = :utilisateur) " +
           "ORDER BY a.date DESC")
    Page<AuditLog> rechercher(
            @Param("entite")      String entite,
            @Param("action")      String action,
            @Param("utilisateur") String utilisateur,
            Pageable pageable);
}
