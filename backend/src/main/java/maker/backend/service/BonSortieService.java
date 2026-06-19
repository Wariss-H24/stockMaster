package maker.backend.service;

import maker.backend.dto.BonSortieDTO;
import maker.backend.dto.SortieLigneDTO;
import maker.backend.entity.BonSortie;
import maker.backend.entity.Entrepot;
import maker.backend.entity.MouvementStock;
import maker.backend.entity.Produit;
import maker.backend.entity.Stock;
import maker.backend.entity.SortieLigne;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.repository.BonSortieRepository;
import maker.backend.repository.EntrepotRepository;
import maker.backend.repository.ProduitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class BonSortieService {

    private final BonSortieRepository repo;
    private final EntrepotRepository entrepotRepo;
    private final ProduitRepository produitRepo;
    private final StockService stockService;
    private final MouvementStockService mouvementService;

    public BonSortieService(BonSortieRepository repo,
                            EntrepotRepository entrepotRepo,
                            ProduitRepository produitRepo,
                            StockService stockService,
                            MouvementStockService mouvementService) {
        this.repo = repo;
        this.entrepotRepo = entrepotRepo;
        this.produitRepo = produitRepo;
        this.stockService = stockService;
        this.mouvementService = mouvementService;
    }

    public java.util.List<BonSortieDTO> findAll() {
        return repo.findAllByOrderByDateDesc().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public BonSortieDTO findById(Long id) {
        return repo.findById(id).map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Bon de sortie introuvable : " + id));
    }

    public BonSortieDTO creer(BonSortieDTO dto) {
        BonSortie bon = toEntity(dto);
        bon.setStatut(BonSortie.Statut.BROUILLON);
        bon.setBordereauReference(generateReference());
        return toDTO(repo.save(bon));
    }

    public BonSortieDTO modifier(Long id, BonSortieDTO dto) {
        BonSortie existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bon de sortie introuvable : " + id));
        if (existing.getStatut() != BonSortie.Statut.BROUILLON) {
            throw new IllegalArgumentException("Seuls les bons en brouillon peuvent être modifiés.");
        }
        existing.setDestination(dto.getDestination());
        existing.setCommentaire(dto.getCommentaire());
        existing.getLignes().clear();
        dto.getLignes().forEach(ligneDTO -> existing.getLignes().add(toEntity(ligneDTO, existing)));
        return toDTO(repo.save(existing));
    }

    public BonSortieDTO valider(Long id) {
        BonSortie bon = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bon de sortie introuvable : " + id));
        if (bon.getStatut() != BonSortie.Statut.BROUILLON) {
            throw new IllegalArgumentException("Le bon doit être en brouillon pour être validé.");
        }
        if (bon.getLignes().isEmpty()) {
            throw new IllegalArgumentException("Le bon doit contenir au moins une ligne.");
        }
        bon.setStatut(BonSortie.Statut.VALIDE);
        bon.setBordereauReference(bon.getBordereauReference() == null ? generateReference() : bon.getBordereauReference());
        appliquerSortie(bon);
        return toDTO(repo.save(bon));
    }

    public void supprimer(Long id) {
        BonSortie bon = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bon de sortie introuvable : " + id));
        if (bon.getStatut() != BonSortie.Statut.BROUILLON) {
            throw new IllegalArgumentException("Seuls les bons en brouillon peuvent être supprimés.");
        }
        repo.delete(bon);
    }

    private void appliquerSortie(BonSortie bon) {
        for (SortieLigne ligne : bon.getLignes()) {
            Stock stock = stockService.findOrCreateStock(ligne.getProduit().getId(), bon.getEntrepot().getId(), null);
            if (stock.getQuantiteDisponible() < ligne.getQuantite()) {
                throw new IllegalArgumentException("Stock insuffisant pour le produit '" + ligne.getProduit().getNom() +
                        "' dans l'entrepôt " + bon.getEntrepot().getNom() +
                        " (quantité disponible = " + stock.getQuantiteDisponible() + ")");
            }
            stock.setQuantiteDisponible(stock.getQuantiteDisponible() - ligne.getQuantite());
            stockService.save(stock);

            // Recalcul capacite zone si applicable
            if (stock.getZone() != null) {
                stockService.recalculerCapaciteZone(stock.getZone());
            }

            MouvementStock mouvement = new MouvementStock();
            mouvement.setStock(stock);
            mouvement.setType(MouvementStock.Type.SORTIE);
            mouvement.setQuantite(ligne.getQuantite());
            mouvement.setSource(bon.getDestination());
            mouvement.setCommentaire("Sortie validée");
            mouvementService.enregistrer(mouvement);
        }
        stockService.recalculerCapaciteEntrepot(bon.getEntrepot());
    }

    private String generateReference() {
        long count = repo.countBy() + 1;
        return String.format("BSO-%04d", count);
    }

    private BonSortieDTO toDTO(BonSortie bon) {
        BonSortieDTO dto = new BonSortieDTO();
        dto.setId(bon.getId());
        dto.setEntrepotId(bon.getEntrepot().getId());
        dto.setEntrepotNom(bon.getEntrepot().getNom());
        dto.setDestination(bon.getDestination());
        dto.setBordereauReference(bon.getBordereauReference());
        dto.setStatut(bon.getStatut().name());
        dto.setCommentaire(bon.getCommentaire());
        dto.setDate(bon.getDate() != null ? bon.getDate().toString() : null);
        dto.setLignes(bon.getLignes().stream().map(this::toDTO).collect(Collectors.toList()));
        return dto;
    }

    private SortieLigneDTO toDTO(SortieLigne ligne) {
        SortieLigneDTO dto = new SortieLigneDTO();
        dto.setId(ligne.getId());
        dto.setProduitId(ligne.getProduit().getId());
        dto.setQuantite(ligne.getQuantite());
        return dto;
    }

    private BonSortie toEntity(BonSortieDTO dto) {
        BonSortie bon = new BonSortie();
        bon.setEntrepot(findEntrepot(dto.getEntrepotId()));
        bon.setDestination(dto.getDestination());
        bon.setCommentaire(dto.getCommentaire());
        dto.getLignes().forEach(ligneDTO -> bon.getLignes().add(toEntity(ligneDTO, bon)));
        return bon;
    }

    private SortieLigne toEntity(SortieLigneDTO dto, BonSortie bon) {
        SortieLigne ligne = new SortieLigne();
        ligne.setBonSortie(bon);
        ligne.setProduit(findProduit(dto.getProduitId()));
        ligne.setQuantite(dto.getQuantite());
        return ligne;
    }

    private Entrepot findEntrepot(Long id) {
        return entrepotRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private Produit findProduit(Long id) {
        return produitRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
    }
}
