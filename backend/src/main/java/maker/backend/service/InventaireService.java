package maker.backend.service;

import maker.backend.dto.InventaireDTO;
import maker.backend.dto.InventaireLigneDTO;
import maker.backend.entity.*;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 11 — Gestion des inventaires physiques.
 * Cycle : creer() → EN_COURS → cloturer() → CLOTURE
 *         (ajuster les stocks selon les écarts à la clôture)
 */
@Service
@Transactional
public class InventaireService {

    private final InventaireRepository repo;
    private final EntrepotRepository entrepotRepo;
    private final ZoneFrRepository zoneRepo;
    private final ProduitRepository produitRepo;
    private final UtilisateurRepository userRepo;
    private final StockRepository stockRepo;
    private final MouvementStockService mouvementService;

    public InventaireService(InventaireRepository repo,
                             EntrepotRepository entrepotRepo,
                             ZoneFrRepository zoneRepo,
                             ProduitRepository produitRepo,
                             UtilisateurRepository userRepo,
                             StockRepository stockRepo,
                             MouvementStockService mouvementService) {
        this.repo = repo;
        this.entrepotRepo = entrepotRepo;
        this.zoneRepo = zoneRepo;
        this.produitRepo = produitRepo;
        this.userRepo = userRepo;
        this.stockRepo = stockRepo;
        this.mouvementService = mouvementService;
    }

    public List<InventaireDTO> findAll() {
        return repo.findAllByOrderByDateDebutDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public InventaireDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    /** Crée un inventaire EN_COURS et pré-remplit les lignes avec les stocks actuels. */
    public InventaireDTO creer(InventaireDTO dto, String username) {
        Entrepot entrepot = entrepotRepo.findById(dto.getEntrepotId())
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + dto.getEntrepotId()));
        ZoneFr zone = dto.getZoneId() != null
                ? zoneRepo.findById(dto.getZoneId()).orElseThrow(() -> new ResourceNotFoundException("Zone introuvable"))
                : null;
        Utilisateur responsable = userRepo.findByUsername(username).orElse(null);

        Inventaire inv = new Inventaire();
        inv.setReference(genererReference());
        inv.setType(Inventaire.Type.valueOf(dto.getType()));
        inv.setStatut(Inventaire.Statut.EN_COURS);
        inv.setEntrepot(entrepot);
        inv.setZone(zone);
        inv.setCommentaire(dto.getCommentaire());
        inv.setResponsable(responsable);

        // Pré-remplir les lignes selon les stocks existants
        List<Stock> stocks = zone != null
                ? stockRepo.findByZone(zone)
                : stockRepo.findByEntrepot(entrepot);

        for (Stock s : stocks) {
            InventaireLigne ligne = new InventaireLigne();
            ligne.setInventaire(inv);
            ligne.setProduit(s.getProduit());
            ligne.setQuantiteSysteme(s.getQuantiteDisponible());
            ligne.setQuantitePhysique(s.getQuantiteDisponible()); // valeur par défaut = système
            ligne.setEcart(0);
            inv.getLignes().add(ligne);
        }
        return toDTO(repo.save(inv));
    }

    /** Met à jour les quantités physiques saisies (uniquement EN_COURS). */
    public InventaireDTO mettreAJourLignes(Long id, List<InventaireLigneDTO> lignesDTO) {
        Inventaire inv = getOrThrow(id);
        if (inv.getStatut() != Inventaire.Statut.EN_COURS) {
            throw new IllegalArgumentException("L'inventaire n'est plus en cours de saisie.");
        }
        for (InventaireLigne ligne : inv.getLignes()) {
            lignesDTO.stream()
                    .filter(dto -> dto.getId() != null && dto.getId().equals(ligne.getId()))
                    .findFirst().ifPresent(dto -> {
                        ligne.setQuantitePhysique(dto.getQuantitePhysique() != null ? dto.getQuantitePhysique() : ligne.getQuantitePhysique());
                        ligne.setEcart(ligne.getQuantitePhysique() - ligne.getQuantiteSysteme());
                        ligne.setCommentaire(dto.getCommentaire());
                    });
        }
        return toDTO(repo.save(inv));
    }

    /**
     * Clôture l'inventaire : applique les ajustements de stock pour les lignes avec écart.
     * Un écart positif → ENTREE de régularisation ; négatif → SORTIE de régularisation.
     */
    public InventaireDTO cloturer(Long id) {
        Inventaire inv = getOrThrow(id);
        if (inv.getStatut() != Inventaire.Statut.EN_COURS) {
            throw new IllegalArgumentException("Seul un inventaire EN_COURS peut être clôturé.");
        }

        for (InventaireLigne ligne : inv.getLignes()) {
            int ecart = ligne.getQuantitePhysique() - ligne.getQuantiteSysteme();
            ligne.setEcart(ecart);
            if (ecart == 0) continue;

            // Trouver ou créer le stock
            Stock stock = stockRepo.findByProduitAndEntrepot(ligne.getProduit(), inv.getEntrepot())
                    .orElseGet(() -> {
                        Stock s = new Stock();
                        s.setProduit(ligne.getProduit());
                        s.setEntrepot(inv.getEntrepot());
                        s.setZone(inv.getZone());
                        s.setQuantiteDisponible(0);
                        s.setQuantiteReservee(0);
                        s.setQuantiteTransit(0);
                        return s;
                    });

            // Appliquer l'écart
            stock.setQuantiteDisponible(Math.max(0, stock.getQuantiteDisponible() + ecart));
            stockRepo.save(stock);

            // Enregistrer le mouvement de régularisation
            MouvementStock m = new MouvementStock();
            m.setStock(stock);
            m.setType(ecart > 0 ? MouvementStock.Type.ENTREE : MouvementStock.Type.SORTIE);
            m.setQuantite(Math.abs(ecart));
            m.setSource("Inventaire " + inv.getReference());
            m.setCommentaire("Régularisation écart inventaire (écart=" + ecart + ")");
            mouvementService.enregistrer(m);
        }

        inv.setStatut(Inventaire.Statut.CLOTURE);
        inv.setDateCloture(LocalDateTime.now());
        return toDTO(repo.save(inv));
    }

    public InventaireDTO annuler(Long id) {
        Inventaire inv = getOrThrow(id);
        if (inv.getStatut() != Inventaire.Statut.EN_COURS) {
            throw new IllegalArgumentException("Seul un inventaire EN_COURS peut être annulé.");
        }
        inv.setStatut(Inventaire.Statut.ANNULE);
        return toDTO(repo.save(inv));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Inventaire getOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventaire introuvable : " + id));
    }

    private String genererReference() {
        long count = repo.countBy() + 1;
        return String.format("INV-%04d", count);
    }

    private InventaireDTO toDTO(Inventaire inv) {
        InventaireDTO dto = new InventaireDTO();
        dto.setId(inv.getId());
        dto.setReference(inv.getReference());
        dto.setType(inv.getType().name());
        dto.setStatut(inv.getStatut().name());
        dto.setEntrepotId(inv.getEntrepot().getId());
        dto.setEntrepotNom(inv.getEntrepot().getNom());
        if (inv.getZone() != null) {
            dto.setZoneId(inv.getZone().getId());
            dto.setZoneNom(inv.getZone().getNom());
        }
        dto.setCommentaire(inv.getCommentaire());
        if (inv.getResponsable() != null) {
            dto.setResponsableId(inv.getResponsable().getId());
            dto.setResponsableNom(inv.getResponsable().getNomComplet());
        }
        dto.setDateDebut(inv.getDateDebut());
        dto.setDateCloture(inv.getDateCloture());
        dto.setLignes(inv.getLignes().stream().map(this::toLigneDTO).collect(Collectors.toList()));
        // Stats
        dto.setNbLignes(inv.getLignes().size());
        dto.setNbEcartsPositifs((int) inv.getLignes().stream().filter(l -> l.getEcart() != null && l.getEcart() > 0).count());
        dto.setNbEcartsNegatifs((int) inv.getLignes().stream().filter(l -> l.getEcart() != null && l.getEcart() < 0).count());
        dto.setNbSansEcart((int) inv.getLignes().stream().filter(l -> l.getEcart() != null && l.getEcart() == 0).count());
        return dto;
    }

    private InventaireLigneDTO toLigneDTO(InventaireLigne l) {
        InventaireLigneDTO dto = new InventaireLigneDTO();
        dto.setId(l.getId());
        dto.setProduitId(l.getProduit().getId());
        dto.setProduitNom(l.getProduit().getNom());
        dto.setProduitReference(l.getProduit().getReference());
        dto.setQuantiteSysteme(l.getQuantiteSysteme());
        dto.setQuantitePhysique(l.getQuantitePhysique());
        dto.setEcart(l.getEcart());
        dto.setCommentaire(l.getCommentaire());
        return dto;
    }
}
