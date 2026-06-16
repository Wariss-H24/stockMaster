package maker.backend.service;

import maker.backend.dto.StockDTO;
import maker.backend.entity.Entrepot;
import maker.backend.entity.Produit;
import maker.backend.entity.Stock;
import maker.backend.entity.ZoneFr;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.mapper.StockMapper;
import maker.backend.repository.EntrepotRepository;
import maker.backend.repository.ProduitRepository;
import maker.backend.repository.StockRepository;
import maker.backend.repository.ZoneFrRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StockService {

    private final StockRepository repo;
    private final ProduitRepository produitRepo;
    private final EntrepotRepository entrepotRepo;
    private final ZoneFrRepository zoneRepo;
    private final StockMapper mapper;

    public StockService(StockRepository repo,
                        ProduitRepository produitRepo,
                        EntrepotRepository entrepotRepo,
                        ZoneFrRepository zoneRepo,
                        StockMapper mapper) {
        this.repo = repo;
        this.produitRepo = produitRepo;
        this.entrepotRepo = entrepotRepo;
        this.zoneRepo = zoneRepo;
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
        Produit produit = produitRepo.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + produitId));
        Entrepot entrepot = entrepotRepo.findById(entrepotId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + entrepotId));
        ZoneFr zone = null;
        if (zoneId != null) {
            zone = zoneRepo.findById(zoneId)
                    .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + zoneId));
        }
        final ZoneFr zoneFinal = zone;
        return repo.findByProduitAndEntrepotAndZone(produit, entrepot, zoneFinal)
                .or(() -> repo.findByProduitAndEntrepot(produit, entrepot))
                .orElseGet(() -> {
                    Stock stock = new Stock();
                    stock.setProduit(produit);
                    stock.setEntrepot(entrepot);
                    stock.setZone(zoneFinal);
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
}
