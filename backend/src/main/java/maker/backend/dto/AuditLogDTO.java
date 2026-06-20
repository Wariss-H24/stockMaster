package maker.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogDTO {
    private Long id;
    private String entite;
    private Long entiteId;
    private String action;
    private String utilisateur;
    private String ancienneValeur;
    private String nouvelleValeur;
    private LocalDateTime date;
    private String ipAdresse;
}
