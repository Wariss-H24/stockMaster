package maker.backend.service;

import maker.backend.dto.StockDTO;
import maker.backend.entity.Entrepot;
import maker.backend.entity.Produit;
import maker.backend.entity.Stock;
import maker.backend.entity.ZoneFr;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.mapper.StockMapper;
import maker.backend.repository.EmplacementRepository;
import maker.backend.repository.EntrepotRepository;
import maker.backend.repository.ProduitRepository;
import maker.backend.repository.StockRepository;
import maker.backend.repository.ZoneFrRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class StockService {

    private final StockRepository repo;
    private final ProduitRepository produitRepo;
    private final EntrepotRepository entrepotRepo;
    private final ZoneFrRepository zoneRepo;
    private final EmplacementRepository emplacementRepo;
    private final StockMapper mapper;

    public StockService(StockRepository repo,
                        ProduitRepository produitRepo,
                        EntrepotRepository entrepotRepo,
                        ZoneFrRepository zoneRepo,
                        EmplacementRepository emplacementRepo,
                        StockMapper mapper) {
        this.repo = repo;
        this.produitRepo = produitRepo;
        this.entrepotRepo = entrepotRepo;
        this.zoneRepo = zoneRepo;
        this.emplacementRepo = emplacementRepo;
        this.mapper = mapper;
    }

    public List<StockDTO> findAll() {
        return repo.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public StockDTO findById(Long id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Stock introuvable : " + id));
    }

    public Stock findOrCreateStock(Long produitId, Long entrepotId, Long zoneId) {
        return findOrCreateStockAvecEmplacement(produitId, entrepotId, zoneId, null);
    }

    public Stock findOrCreateStockAvecEmplacement(Long produitId, Long entrepotId, Long zoneId, Long emplacementId) {
        Produit produit = produitRepo.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + produitId));
        Entrepot entrepot = entrepotRepo.findById(entrepotId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + entrepotId));
        ZoneFr zone = zoneId != null
                ? zoneRepo.findById(zoneId).orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + zoneId))
                : null;
        final ZoneFr zoneFinal = zone;

        // Chercher l'emplacement réel depuis la DB si fourni
        maker.backend.entity.Emplacement emplacement = null;
        if (emplacementId != null) {
            // Récupération via le repository — on utilise EmplacementRepository injecté
            emplacement = emplacementRepo.findById(emplacementId)
                    .orElseThrow(() -> new ResourceNotFoundException("Emplacement introuvable : " + emplacementId));
        }
        final maker.backend.entity.Emplacement emplacementFinal = emplacement;

        return repo.findByProduitAndEntrepotAndZone(produit, entrepot, zoneFinal)
                .or(() -> repo.findByProduitAndEntrepot(produit, entrepot))
                .map(s -> {
                    if (emplacementFinal != null && s.getEmplacement() == null) {
                        s.setEmplacement(emplacementFinal);
                    }
                    return s;
                })
                .orElseGet(() -> {
                    Stock stock = new Stock();
                    stock.setProduit(produit);
                    stock.setEntrepot(entrepot);
                    stock.setZone(zoneFinal);
                    stock.setEmplacement(emplacementFinal);
                    stock.setQuantiteDisponible(0);
                    stock.setQuantiteReservee(0);
                    stock.setQuantiteTransit(0);
                    stock.setStockMin(0);
                    stock.setStockMax(0);
                    return stock;
                });
    }

    public Stock updateQuantite(Stock stock, int delta) {
        stock.setQuantiteDisponible(Math.max(0, stock.getQuantiteDisponible() + delta));
        return repo.save(stock);
    }

    public Stock save(Stock stock) {
        return repo.save(stock);
    }

    /** Met à jour les seuils stockMin / stockMax d'un stock. */
    public StockDTO majSeuils(Long id, int stockMin, int stockMax) {
        Stock stock = repo.findById(id)
                .orElseThrow(() -> new maker.backend.exception.ResourceNotFoundException("Stock introuvable : " + id));
        stock.setStockMin(Math.max(0, stockMin));
        stock.setStockMax(Math.max(0, stockMax));
        return mapper.toDTO(repo.save(stock));
    }

    public void recalculerCapaciteZone(ZoneFr zone) {
        if (zone == null) return;
        int utilise = (int) Math.round(repo.sumEspaceUtiliseByZone(zone));
        zone.setCapaciteUtilisee(utilise);
        zoneRepo.save(zone);
    }

    public void recalculerCapaciteEntrepot(Entrepot entrepot) {
        int utilise = (int) Math.round(repo.sumEspaceUtiliseByEntrepot(entrepot));
        entrepot.setCapaciteUtilisee(utilise);
        entrepotRepo.save(entrepot);
    }

    /**
     * Vérifie que l'ajout de (quantite * volume) ne dépasse pas la capacité de la zone et de l'entrepôt.
     */
    public void verifierCapaciteDisponible(Entrepot entrepot, ZoneFr zone, Produit produit, int quantite) {
        if (produit.getVolume() == null || produit.getVolume() == 0) return;
        double espaceAjouter = quantite * produit.getVolume();

        if (zone != null && zone.getCapaciteTotale() != null && zone.getCapaciteTotale() > 0) {
            double utiliseZone = repo.sumEspaceUtiliseByZone(zone);
            if (utiliseZone + espaceAjouter > zone.getCapaciteTotale()) {
                throw new IllegalArgumentException(
                    "Capacité de la zone « " + zone.getNom() + " » dépassée. Disponible : "
                    + (int)(zone.getCapaciteTotale() - utiliseZone) + " / Requis : " + (int) Math.ceil(espaceAjouter));
            }
        }

        if (entrepot.getCapaciteTotale() != null && entrepot.getCapaciteTotale() > 0) {
            double utiliseEntrepot = repo.sumEspaceUtiliseByEntrepot(entrepot);
            if (utiliseEntrepot + espaceAjouter > entrepot.getCapaciteTotale()) {
                throw new IllegalArgumentException(
                    "Capacité de l'entrepôt « " + entrepot.getNom() + " » dépassée. Disponible : "
                    + (int)(entrepot.getCapaciteTotale() - utiliseEntrepot) + " / Requis : " + (int) Math.ceil(espaceAjouter));
            }
        }
    }
}
