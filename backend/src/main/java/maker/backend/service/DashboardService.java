package maker.backend.service;

import maker.backend.dto.DashboardDTO;
import maker.backend.entity.CommandeFournisseur;
import maker.backend.entity.MouvementStock;
import maker.backend.entity.Stock;
import maker.backend.entity.Transfert;
import maker.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Module 13 — Tableau de bord avec KPIs et données de graphiques.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final EntrepotRepository entrepotRepo;
    private final ProduitRepository produitRepo;
    private final StockRepository stockRepo;
    private final MouvementStockRepository mouvementRepo;
    private final TransfertRepository transfertRepo;
    private final CommandeFournisseurRepository commandeRepo;
    private final AlerteService alerteService;

    public DashboardService(EntrepotRepository entrepotRepo,
                            ProduitRepository produitRepo,
                            StockRepository stockRepo,
                            MouvementStockRepository mouvementRepo,
                            TransfertRepository transfertRepo,
                            CommandeFournisseurRepository commandeRepo,
                            AlerteService alerteService) {
        this.entrepotRepo = entrepotRepo;
        this.produitRepo = produitRepo;
        this.stockRepo = stockRepo;
        this.mouvementRepo = mouvementRepo;
        this.transfertRepo = transfertRepo;
        this.commandeRepo = commandeRepo;
        this.alerteService = alerteService;
    }

    public DashboardDTO getKpis() {
        DashboardDTO dto = new DashboardDTO();

        // KPIs simples
        dto.setNbEntrepots(entrepotRepo.findByActifTrue().size());
        dto.setNbProduits(produitRepo.findBySupprimeFalse().size());

        List<Stock> stocks = stockRepo.findAll();
        long critiques = stocks.stream()
                .filter(s -> s.getStockMin() != null && s.getStockMin() > 0
                          && s.getQuantiteDisponible() <= s.getStockMin())
                .count();
        dto.setNbStocksCritiques(critiques);
        dto.setNbStocksOk(stocks.size() - critiques);

        // Valeur totale du stock
        double valeur = stocks.stream()
                .filter(s -> s.getProduit().getPrixAchat() != null)
                .mapToDouble(s -> s.getQuantiteDisponible() * s.getProduit().getPrixAchat())
                .sum();
        dto.setValeurTotaleStock(valeur);

        // Mouvements du mois courant
        LocalDateTime debutMois = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        List<MouvementStock> tousMovements = mouvementRepo.findAll();
        dto.setEntreesDuMois(tousMovements.stream()
                .filter(m -> m.getType() == MouvementStock.Type.ENTREE && m.getDate().isAfter(debutMois))
                .count());
        dto.setSortiesDuMois(tousMovements.stream()
                .filter(m -> m.getType() == MouvementStock.Type.SORTIE && m.getDate().isAfter(debutMois))
                .count());

        // Transferts en cours
        dto.setNbTransfertsEnCours(transfertRepo.findAll().stream()
                .filter(t -> t.getStatut() == Transfert.Statut.EXPEDIE)
                .count());

        // Commandes en attente
        dto.setNbCommandesEnAttente(commandeRepo.countByStatut(CommandeFournisseur.Statut.ENVOYEE));

        // Top 5 produits les plus mouvementés
        Map<Long, Long> countByProduit = tousMovements.stream()
                .collect(Collectors.groupingBy(m -> m.getStock().getProduit().getId(), Collectors.counting()));
        dto.setTopProduits(countByProduit.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    String nom = tousMovements.stream()
                            .filter(m -> m.getStock().getProduit().getId().equals(e.getKey()))
                            .findFirst().map(m -> m.getStock().getProduit().getNom()).orElse("?");
                    item.put("nom", nom);
                    item.put("mouvements", e.getValue());
                    return item;
                })
                .collect(Collectors.toList()));

        // Évolution mensuelle sur 6 mois
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);
        List<Map<String, Object>> evolution = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDateTime debut = ym.atDay(1).atStartOfDay();
            LocalDateTime fin   = ym.atEndOfMonth().atTime(23, 59, 59);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mois", ym.format(fmt));
            item.put("entrees", tousMovements.stream()
                    .filter(m -> m.getType() == MouvementStock.Type.ENTREE
                              && m.getDate().isAfter(debut) && m.getDate().isBefore(fin))
                    .count());
            item.put("sorties", tousMovements.stream()
                    .filter(m -> m.getType() == MouvementStock.Type.SORTIE
                              && m.getDate().isAfter(debut) && m.getDate().isBefore(fin))
                    .count());
            evolution.add(item);
        }
        dto.setEvolutionMensuelle(evolution);

        // Répartition par entrepôt
        dto.setRepartitionEntrepots(entrepotRepo.findByActifTrue().stream().map(e -> {
            int qte = stockRepo.findByEntrepot(e).stream()
                    .mapToInt(s -> s.getQuantiteDisponible() != null ? s.getQuantiteDisponible() : 0).sum();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("nom", e.getNom());
            item.put("quantite", qte);
            return item;
        }).collect(Collectors.toList()));

        // Alertes actives
        dto.setAlertesActives(alerteService.getAlertes());

        return dto;
    }
}
