package maker.backend.service;

import maker.backend.dto.AlerteDTO;
import maker.backend.entity.Stock;
import maker.backend.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 12 — Alertes automatiques de stock.
 *
 * JavaMailSender est optionnel (@Autowired required=false).
 * Si spring.mail n'est pas configuré → pas de bean → emails silencieusement ignorés.
 * Les alertes restent consultables via l'API dans tous les cas.
 */
@Service
public class AlerteService {

    private final StockRepository stockRepo;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${stockmaster.alert.email.to:admin@stockmaster.com}")
    private String emailDestinataire;

    public AlerteService(StockRepository stockRepo) {
        this.stockRepo = stockRepo;
    }

    // ── Lecture ───────────────────────────────────────────────────────────────

    /** Toutes les alertes actives (stocks ≤ stockMin). */
    public List<AlerteDTO> getAlertes() {
        return stockRepo.findAll().stream()
                .filter(s -> s.getStockMin() != null && s.getStockMin() > 0
                          && s.getQuantiteDisponible() <= s.getStockMin())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Uniquement critiques (qté = 0 ou ≤ 50% du min). */
    public List<AlerteDTO> getAlertesCritiques() {
        return getAlertes().stream()
                .filter(a -> "CRITIQUE".equals(a.getNiveau()))
                .collect(Collectors.toList());
    }

    // ── Scheduler ─────────────────────────────────────────────────────────────

    /** Vérification automatique toutes les heures. */
    @Scheduled(cron = "0 0 * * * *")
    public void verifierStocksAutomatiquement() {
        List<AlerteDTO> critiques = getAlertesCritiques();
        if (!critiques.isEmpty()) {
            System.out.println("[StockMaster] ⚠ " + critiques.size() + " stock(s) critique(s) détecté(s).");
            envoyerEmailAlertes(critiques);
        }
    }

    // ── Email ─────────────────────────────────────────────────────────────────

    /**
     * Envoie un email récapitulatif.
     * Si mailSender est null (spring.mail non configuré) → log console uniquement.
     */
    public void envoyerEmailAlertes(List<AlerteDTO> alertes) {
        if (alertes.isEmpty()) return;

        // Log console toujours actif
        System.out.println("[StockMaster] Email alertes : " + alertes.size() + " alerte(s)");

        if (mailSender == null) {
            System.out.println("[StockMaster] Email non envoyé — spring.mail non configuré.");
            System.out.println("[StockMaster] Pour activer : configurez spring.mail dans application.properties");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailDestinataire);
            message.setSubject("[StockMaster] ⚠ " + alertes.size() + " alerte(s) de stock critique");
            message.setText(construireCorps(alertes));
            mailSender.send(message);
            System.out.println("[StockMaster] Email envoyé à " + emailDestinataire);
        } catch (Exception e) {
            System.err.println("[StockMaster] Échec envoi email : " + e.getMessage());
        }
    }

    private String construireCorps(List<AlerteDTO> alertes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour,\n\n");
        sb.append("Les produits suivants ont un stock critique :\n\n");
        for (AlerteDTO a : alertes) {
            sb.append("  • ").append(a.getProduitNom())
              .append(" [").append(a.getProduitReference()).append("]")
              .append("\n    Entrepôt : ").append(a.getEntrepotNom());
            if (a.getZoneNom() != null) sb.append(" / ").append(a.getZoneNom());
            sb.append("\n    Stock disponible : ").append(a.getQuantiteDisponible())
              .append(" | Stock minimum : ").append(a.getStockMin())
              .append(" | Niveau : ").append(a.getNiveau())
              .append("\n\n");
        }
        sb.append("Veuillez procéder à un réapprovisionnement.\n\n— StockMaster\n");
        return sb.toString();
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private AlerteDTO toDTO(Stock s) {
        AlerteDTO dto = new AlerteDTO();
        dto.setStockId(s.getId());
        dto.setProduitId(s.getProduit().getId());
        dto.setProduitNom(s.getProduit().getNom());
        dto.setProduitReference(s.getProduit().getReference());
        dto.setEntrepotId(s.getEntrepot().getId());
        dto.setEntrepotNom(s.getEntrepot().getNom());
        dto.setZoneNom(s.getZone() != null ? s.getZone().getNom() : null);
        dto.setQuantiteDisponible(s.getQuantiteDisponible());
        dto.setStockMin(s.getStockMin());
        dto.setDateDetection(LocalDateTime.now());

        int min   = s.getStockMin()          != null ? s.getStockMin()          : 0;
        int dispo = s.getQuantiteDisponible() != null ? s.getQuantiteDisponible() : 0;
        dto.setNiveau(dispo <= 0 || (min > 0 && dispo <= min / 2) ? "CRITIQUE" : "FAIBLE");
        return dto;
    }
}
