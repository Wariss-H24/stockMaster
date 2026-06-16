package maker.backend.service;

import maker.backend.dto.BonReceptionDTO;
import maker.backend.dto.ReceptionLigneDTO;
import maker.backend.entity.*;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BonReceptionService {

    private final BonReceptionRepository repo;
    private final FournisseurRepository fournisseurRepo;
    private final EntrepotRepository entrepotRepo;
    private final ZoneFrRepository zoneRepo;
    private final ProduitRepository produitRepo;
    private final StockService stockService;
    private final MouvementStockService mouvementService;

    public BonReceptionService(BonReceptionRepository repo,
                               FournisseurRepository fournisseurRepo,
                               EntrepotRepository entrepotRepo,
                               ZoneFrRepository zoneRepo,
                               ProduitRepository produitRepo,
                               StockService stockService,
                               MouvementStockService mouvementService) {
        this.repo = repo;
        this.fournisseurRepo = fournisseurRepo;
        this.entrepotRepo = entrepotRepo;
        this.zoneRepo = zoneRepo;
        this.produitRepo = produitRepo;
        this.stockService = stockService;
        this.mouvementService = mouvementService;
    }

    public List<BonReceptionDTO> findAll() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<BonReceptionDTO> findByFournisseurId(Long fournisseurId) {
        return repo.findByFournisseurIdOrderByDateDesc(fournisseurId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public BonReceptionDTO findById(Long id) {
        return repo.findById(id).map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Bon de réception introuvable : " + id));
    }

    public BonReceptionDTO creer(BonReceptionDTO dto) {
        BonReception bon = toEntity(dto);
        bon.setStatut(BonReception.Statut.BROUILLON);
        BonReception saved = repo.save(bon);
        return toDTO(saved);
    }

    public BonReceptionDTO modifier(Long id, BonReceptionDTO dto) {
        BonReception existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bon de réception introuvable : " + id));
        if (existing.getStatut() != BonReception.Statut.BROUILLON) {
            throw new IllegalArgumentException("Seuls les bons en brouillon peuvent être modifiés.");
        }
        existing.setFournisseur(findFournisseur(dto.getFournisseurId()));
        existing.setEntrepot(findEntrepot(dto.getEntrepotId()));
        existing.setZone(findZone(dto.getZoneId()));
        existing.setCommentaire(dto.getCommentaire());
        existing.setControleQualiteOk(dto.isControleQualiteOk());
        existing.getLignes().clear();
        dto.getLignes().forEach(ligneDTO -> existing.getLignes().add(toEntity(ligneDTO, existing)));
        return toDTO(repo.save(existing));
    }

    public BonReceptionDTO valider(Long id) {
        BonReception bon = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bon de réception introuvable : " + id));
        if (bon.getStatut() != BonReception.Statut.BROUILLON) {
            throw new IllegalArgumentException("Le bon doit être en brouillon pour être validé.");
        }
        if (bon.getLignes().isEmpty()) {
            throw new IllegalArgumentException("Le bon doit contenir au moins une ligne.");
        }
        if (!bon.getControleQualiteOk()) {
            throw new IllegalArgumentException("Le contrôle qualité doit être validé avant de confirmer la réception.");
        }
        bon.setStatut(BonReception.Statut.VALIDE);
        appliquerReception(bon);
        return toDTO(repo.save(bon));
    }

    public void supprimer(Long id) {
        BonReception bon = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bon de réception introuvable : " + id));
        if (bon.getStatut() != BonReception.Statut.BROUILLON) {
            throw new IllegalArgumentException("Seuls les bons en brouillon peuvent être supprimés.");
        }
        repo.delete(bon);
    }

    private void appliquerReception(BonReception bon) {
        for (ReceptionLigne ligne : bon.getLignes()) {
            Stock stock = stockService.findOrCreateStock(
                    ligne.getProduit().getId(), bon.getEntrepot().getId(), bon.getZone() != null ? bon.getZone().getId() : null);
            stock.setQuantiteDisponible(stock.getQuantiteDisponible() + ligne.getQuantite());
            stockService.save(stock);

            MouvementStock mouvement = new MouvementStock();
            mouvement.setStock(stock);
            mouvement.setType(MouvementStock.Type.ENTREE);
            mouvement.setQuantite(ligne.getQuantite());
            mouvement.setSource(bon.getFournisseur().getNom());
            mouvement.setCommentaire("Réception validée");
            mouvementService.enregistrer(mouvement);
        }
    }

    private BonReceptionDTO toDTO(BonReception bon) {
        BonReceptionDTO dto = new BonReceptionDTO();
        dto.setId(bon.getId());
        dto.setFournisseurId(bon.getFournisseur().getId());
        dto.setFournisseurNom(bon.getFournisseur().getNom());
        dto.setEntrepotId(bon.getEntrepot().getId());
        dto.setEntrepotNom(bon.getEntrepot().getNom());
        dto.setZoneId(bon.getZone() != null ? bon.getZone().getId() : null);
        dto.setZoneNom(bon.getZone() != null ? bon.getZone().getNom() : null);
        dto.setCommentaire(bon.getCommentaire());
        dto.setStatut(bon.getStatut().name());
        dto.setControleQualiteOk(Boolean.TRUE.equals(bon.getControleQualiteOk()));
        dto.setDate(bon.getDate() != null ? bon.getDate().toString() : null);
        dto.setLignes(bon.getLignes().stream().map(this::toDTO).collect(Collectors.toList()));
        return dto;
    }

    private ReceptionLigneDTO toDTO(ReceptionLigne ligne) {
        ReceptionLigneDTO dto = new ReceptionLigneDTO();
        dto.setId(ligne.getId());
        dto.setProduitId(ligne.getProduit().getId());
        dto.setQuantite(ligne.getQuantite());
        dto.setPrixAchat(ligne.getPrixAchat());
        return dto;
    }

    private BonReception toEntity(BonReceptionDTO dto) {
        BonReception bon = new BonReception();
        bon.setFournisseur(findFournisseur(dto.getFournisseurId()));
        bon.setEntrepot(findEntrepot(dto.getEntrepotId()));
        bon.setZone(findZone(dto.getZoneId()));
        bon.setCommentaire(dto.getCommentaire());
        bon.setControleQualiteOk(dto.isControleQualiteOk());
        dto.getLignes().forEach(ligneDTO -> bon.getLignes().add(toEntity(ligneDTO, bon)));
        return bon;
    }

    private ReceptionLigne toEntity(ReceptionLigneDTO dto, BonReception bon) {
        ReceptionLigne ligne = new ReceptionLigne();
        ligne.setBonReception(bon);
        ligne.setProduit(produitRepo.findById(dto.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + dto.getProduitId())));
        ligne.setQuantite(dto.getQuantite());
        ligne.setPrixAchat(dto.getPrixAchat());
        return ligne;
    }

    private Fournisseur findFournisseur(Long id) {
        return fournisseurRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + id));
    }

    private Entrepot findEntrepot(Long id) {
        return entrepotRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private ZoneFr findZone(Long id) {
        return id == null ? null : zoneRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + id));
    }
}
