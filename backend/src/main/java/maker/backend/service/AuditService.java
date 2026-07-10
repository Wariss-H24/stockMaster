package maker.backend.service;

import maker.backend.dto.AuditLogDTO;
import maker.backend.entity.AuditLog;
import maker.backend.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 15 — Service de traçabilité / audit.
 */
@Service
@Transactional
public class AuditService {

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    /** Enregistre une entrée d'audit (appelé par l'Aspect ou manuellement). */
    public AuditLog enregistrer(String entite, Long entiteId, String action,
                                 String utilisateur, String ancienneValeur,
                                 String nouvelleValeur, String ip) {
        AuditLog log = new AuditLog();
        log.setEntite(entite);
        log.setEntiteId(entiteId);
        log.setAction(action);
        log.setUtilisateur(utilisateur);
        log.setAncienneValeur(ancienneValeur);
        log.setNouvelleValeur(nouvelleValeur);
        log.setIpAdresse(ip);
        return repo.save(log);
    }

    /** Pagination : tous les logs, les plus récents en premier. */
    @Transactional(readOnly = true)
    public List<AuditLogDTO> findAll(int page, int size) {
        Page<AuditLog> p = repo.findAllByOrderByDateDesc(PageRequest.of(page, size));
        return p.getContent().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return repo.count();
    }

    @Transactional(readOnly = true)
    public List<AuditLogDTO> findByEntite(String entite, Long entiteId) {
        return repo.findByEntiteAndEntiteIdOrderByDateDesc(entite, entiteId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogDTO> rechercherAvance(String entite, String action, String utilisateur,
                                               int page, int size) {
        return repo.rechercher(
                entite != null && !entite.isBlank() ? entite : null,
                action != null && !action.isBlank() ? action : null,
                utilisateur != null && !utilisateur.isBlank() ? utilisateur : null,
                PageRequest.of(page, size)
        ).getContent().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private AuditLogDTO toDTO(AuditLog log) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(log.getId());
        dto.setEntite(log.getEntite());
        dto.setEntiteId(log.getEntiteId());
        dto.setAction(log.getAction());
        dto.setUtilisateur(log.getUtilisateur());
        dto.setAncienneValeur(log.getAncienneValeur());
        dto.setNouvelleValeur(log.getNouvelleValeur());
        dto.setDate(log.getDate());
        dto.setIpAdresse(log.getIpAdresse());
        return dto;
    }
}
