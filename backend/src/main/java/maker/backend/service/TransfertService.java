package maker.backend.service;

import maker.backend.dto.TransfertDTO;
import maker.backend.entity.Entrepot;
import maker.backend.entity.MouvementStock;
import maker.backend.entity.Produit;
import maker.backend.entity.Stock;
import maker.backend.entity.ZoneFr;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.repository.EntrepotRepository;
import maker.backend.repository.ProduitRepository;
import maker.backend.repository.ZoneFrRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class TransfertService {

    private final EntrepotRepository entrepotRepo;
    private final ZoneFrRepository zoneRepo;
    private final ProduitRepository produitRepo;
    private final StockService stockService;
    private final MouvementStockService mouvementService;

    public TransfertService(EntrepotRepository entrepotRepo,
                            ZoneFrRepository zoneRepo,
                            ProduitRepository produitRepo,
                            StockService stockService,
                            MouvementStockService mouvementService) {
        this.entrepotRepo = entrepotRepo;
        this.zoneRepo = zoneRepo;
        this.produitRepo = produitRepo;
        this.stockService = stockService;
        this.mouvementService = mouvementService;
    }

    public void transferer(TransfertDTO dto) {
        if (dto.getSourceEntrepotId().equals(dto.getDestinationEntrepotId()) &&
                Objects.equals(dto.getSourceZoneId(), dto.getDestinationZoneId())) {
            throw new IllegalArgumentException("La source et la destination du transfert doivent être différentes.");
        }

        Entrepot sourceEntrepot = findEntrepot(dto.getSourceEntrepotId());
        ZoneFr sourceZone = findZone(dto.getSourceZoneId(), sourceEntrepot);
        Entrepot destinationEntrepot = findEntrepot(dto.getDestinationEntrepotId());
        ZoneFr destinationZone = findZone(dto.getDestinationZoneId(), destinationEntrepot);
        Produit produit = findProduit(dto.getProduitId());

        Stock sourceStock = stockService.findOrCreateStock(produit.getId(), sourceEntrepot.getId(), sourceZone != null ? sourceZone.getId() : null);
        if (sourceStock.getQuantiteDisponible() < dto.getQuantite()) {
            throw new IllegalArgumentException("Stock insuffisant pour le produit '" + produit.getNom() +
                    "' dans l'entrepôt " + sourceEntrepot.getNom() +
                    " (quantité disponible = " + sourceStock.getQuantiteDisponible() + ")");
        }

        stockService.verifierCapaciteDisponible(destinationEntrepot, destinationZone, produit, dto.getQuantite());
        Stock destinationStock = stockService.findOrCreateStock(produit.getId(), destinationEntrepot.getId(), destinationZone != null ? destinationZone.getId() : null);

        sourceStock.setQuantiteDisponible(sourceStock.getQuantiteDisponible() - dto.getQuantite());
        destinationStock.setQuantiteDisponible(destinationStock.getQuantiteDisponible() + dto.getQuantite());

        stockService.save(sourceStock);
        stockService.save(destinationStock);

        String commentaire = dto.getCommentaire() != null && !dto.getCommentaire().trim().isEmpty()
                ? dto.getCommentaire().trim() : "Transfert validé";

        MouvementStock sourceMouvement = new MouvementStock();
        sourceMouvement.setStock(sourceStock);
        sourceMouvement.setType(MouvementStock.Type.TRANSFERT);
        sourceMouvement.setQuantite(dto.getQuantite());
        sourceMouvement.setSource("Vers " + destinationEntrepot.getNom() + (destinationZone != null ? " / " + destinationZone.getNom() : ""));
        sourceMouvement.setCommentaire(commentaire);
        mouvementService.enregistrer(sourceMouvement);

        MouvementStock destinationMouvement = new MouvementStock();
        destinationMouvement.setStock(destinationStock);
        destinationMouvement.setType(MouvementStock.Type.TRANSFERT);
        destinationMouvement.setQuantite(dto.getQuantite());
        destinationMouvement.setSource("Depuis " + sourceEntrepot.getNom() + (sourceZone != null ? " / " + sourceZone.getNom() : ""));
        destinationMouvement.setCommentaire(commentaire);
        mouvementService.enregistrer(destinationMouvement);

        stockService.recalculerCapaciteZone(sourceZone);
        stockService.recalculerCapaciteEntrepot(sourceEntrepot);
        stockService.recalculerCapaciteZone(destinationZone);
        stockService.recalculerCapaciteEntrepot(destinationEntrepot);
    }

    private Entrepot findEntrepot(Long id) {
        return entrepotRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private ZoneFr findZone(Long id, Entrepot entrepot) {
        if (id == null) {
            return null;
        }
        ZoneFr zone = zoneRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + id));
        if (!zone.getEntrepot().getId().equals(entrepot.getId())) {
            throw new IllegalArgumentException("La zone ne dépend pas de l'entrepôt sélectionné.");
        }
        return zone;
    }

    private Produit findProduit(Long id) {
        return produitRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
    }
}
