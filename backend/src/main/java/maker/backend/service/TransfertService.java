package maker.backend.service;

import maker.backend.dto.TransfertDTO;
import maker.backend.entity.*;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.mapper.TransfertMapper;
import maker.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 10 — Gestion des transferts inter-entrepôts.
 *
 * Cycle de vie :
 *   creer()    → BROUILLON  (aucun mouvement de stock)
 *   expedier() → EXPEDIE    (stock source débité + quantiteTransit++ à destination)
 *   recevoir() → RECU       (quantiteTransit-- + quantiteDisponible++ à destination)
 *   annuler()  → ANNULE     (seulement depuis BROUILLON)
 */
@Service
@Transactional
public class TransfertService {

    private final TransfertRepository transfertRepo;
    private final EntrepotRepository entrepotRepo;
    private final ZoneFrRepository zoneRepo;
    private final ProduitRepository produitRepo;
    private final UtilisateurRepository userRepo;
    private final StockService stockService;
    private final MouvementStockService mouvementService;
    private final TransfertMapper mapper;

    public TransfertService(TransfertRepository transfertRepo,
                            EntrepotRepository entrepotRepo,
                            ZoneFrRepository zoneRepo,
                            ProduitRepository produitRepo,
                            UtilisateurRepository userRepo,
                            StockService stockService,
                            MouvementStockService mouvementService,
                            TransfertMapper mapper) {
        this.transfertRepo  = transfertRepo;
        this.entrepotRepo   = entrepotRepo;
        this.zoneRepo       = zoneRepo;
        this.produitRepo    = produitRepo;
        this.userRepo       = userRepo;
        this.stockService   = stockService;
        this.mouvementService = mouvementService;
        this.mapper         = mapper;
    }

    // --- Lecture ---

    public List<TransfertDTO> findAll() {
        return transfertRepo.findAllByOrderByDateCreaDesc()
                .stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public TransfertDTO findById(Long id) {
        return transfertRepo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Transfert introuvable : " + id));
    }

    // --- Création (état BROUILLON) ---

    public TransfertDTO creer(TransfertDTO dto, String usernameCreateur) {
        Entrepot source = findEntrepot(dto.getEntrepotSourceId());
        Entrepot dest   = findEntrepot(dto.getEntrepotDestinationId());
        ZoneFr zSource  = dto.getZoneSourceId() != null ? findZone(dto.getZoneSourceId(), source) : null;
        ZoneFr zDest    = dto.getZoneDestinationId() != null ? findZone(dto.getZoneDestinationId(), dest) : null;
        Produit produit = findProduit(dto.getProduitId());

        // Empêcher un transfert vers la même zone dans le même entrepôt
        if (source.getId().equals(dest.getId()) &&
            java.util.Objects.equals(dto.getZoneSourceId(), dto.getZoneDestinationId())) {
            throw new IllegalArgumentException("Source et destination doivent être différentes");
        }

        Utilisateur createur = userRepo.findByUsername(usernameCreateur).orElse(null);

        Transfert t = new Transfert();
        t.setReference(genererReference());
        t.setProduit(produit);
        t.setEntrepotSource(source);
        t.setZoneSource(zSource);
        t.setEntrepotDestination(dest);
        t.setZoneDestination(zDest);
        t.setQuantite(dto.getQuantite());
        t.setCommentaire(dto.getCommentaire());
        t.setStatut(Transfert.Statut.BROUILLON);
        t.setCreateur(createur);

        return mapper.toDTO(transfertRepo.save(t));
    }

    // --- Expédition BROUILLON → EXPEDIE ---

    public TransfertDTO expedier(Long id) {
        Transfert t = getTransfert(id);
        if (t.getStatut() != Transfert.Statut.BROUILLON) {
            throw new IllegalArgumentException("Seul un transfert en BROUILLON peut être expédié");
        }

        // Vérifier et débiter le stock source
        Stock stockSource = stockService.findOrCreateStock(
            t.getProduit().getId(),
            t.getEntrepotSource().getId(),
            t.getZoneSource() != null ? t.getZoneSource().getId() : null
        );
        if (stockSource.getQuantiteDisponible() < t.getQuantite()) {
            throw new IllegalArgumentException(
                "Stock insuffisant : disponible=" + stockSource.getQuantiteDisponible() +
                ", demandé=" + t.getQuantite()
            );
        }

        // Débiter la source
        stockSource.setQuantiteDisponible(stockSource.getQuantiteDisponible() - t.getQuantite());
        stockService.save(stockSource);

        // Mettre en transit à la destination
        Stock stockDest = stockService.findOrCreateStock(
            t.getProduit().getId(),
            t.getEntrepotDestination().getId(),
            t.getZoneDestination() != null ? t.getZoneDestination().getId() : null
        );
        stockDest.setQuantiteTransit(stockDest.getQuantiteTransit() + t.getQuantite());
        stockService.save(stockDest);

        // Mouvement TRANSFERT côté source
        enregistrerMouvement(stockSource, MouvementStock.Type.TRANSFERT, t.getQuantite(),
            "Expédié vers " + t.getEntrepotDestination().getNom(), t.getCommentaire());

        // Recalculer capacités
        stockService.recalculerCapaciteZone(t.getZoneSource());
        stockService.recalculerCapaciteEntrepot(t.getEntrepotSource());

        t.setStatut(Transfert.Statut.EXPEDIE);
        t.setDateExpedi(LocalDateTime.now());
        return mapper.toDTO(transfertRepo.save(t));
    }

    // --- Réception EXPEDIE → RECU ---

    public TransfertDTO recevoir(Long id) {
        Transfert t = getTransfert(id);
        if (t.getStatut() != Transfert.Statut.EXPEDIE) {
            throw new IllegalArgumentException("Seul un transfert EXPÉDIÉ peut être reçu");
        }

        Stock stockDest = stockService.findOrCreateStock(
            t.getProduit().getId(),
            t.getEntrepotDestination().getId(),
            t.getZoneDestination() != null ? t.getZoneDestination().getId() : null
        );

        // Sortir du transit et créditer disponible
        stockDest.setQuantiteTransit(Math.max(0, stockDest.getQuantiteTransit() - t.getQuantite()));
        stockDest.setQuantiteDisponible(stockDest.getQuantiteDisponible() + t.getQuantite());
        stockService.save(stockDest);

        // Mouvement ENTREE côté destination
        enregistrerMouvement(stockDest, MouvementStock.Type.ENTREE, t.getQuantite(),
            "Reçu depuis " + t.getEntrepotSource().getNom(), t.getCommentaire());

        // Recalculer capacités destination
        stockService.recalculerCapaciteZone(t.getZoneDestination());
        stockService.recalculerCapaciteEntrepot(t.getEntrepotDestination());

        t.setStatut(Transfert.Statut.RECU);
        t.setDateRecu(LocalDateTime.now());
        return mapper.toDTO(transfertRepo.save(t));
    }

    // --- Annulation (seulement depuis BROUILLON) ---

    public TransfertDTO annuler(Long id) {
        Transfert t = getTransfert(id);
        if (t.getStatut() != Transfert.Statut.BROUILLON) {
            throw new IllegalArgumentException("Seul un transfert en BROUILLON peut être annulé");
        }
        t.setStatut(Transfert.Statut.ANNULE);
        return mapper.toDTO(transfertRepo.save(t));
    }

    // --- Helpers ---

    private Transfert getTransfert(Long id) {
        return transfertRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfert introuvable : " + id));
    }

    private Entrepot findEntrepot(Long id) {
        return entrepotRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private ZoneFr findZone(Long id, Entrepot entrepot) {
        ZoneFr z = zoneRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + id));
        if (!z.getEntrepot().getId().equals(entrepot.getId())) {
            throw new IllegalArgumentException("La zone ne correspond pas à l'entrepôt sélectionné");
        }
        return z;
    }

    private Produit findProduit(Long id) {
        return produitRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
    }

    private void enregistrerMouvement(Stock stock, MouvementStock.Type type,
                                       int quantite, String source, String commentaire) {
        MouvementStock m = new MouvementStock();
        m.setStock(stock);
        m.setType(type);
        m.setQuantite(quantite);
        m.setSource(source);
        m.setCommentaire(commentaire);
        mouvementService.enregistrer(m);
    }

    /** Génère une référence lisible : TRF-0001, TRF-0002... */
    private String genererReference() {
        long count = transfertRepo.countBy() + 1;
        return String.format("TRF-%04d", count);
    }
}
