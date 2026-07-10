package maker.backend.service;

import maker.backend.dto.CommandeFournisseurDTO;
import maker.backend.dto.CommandeLigneDTO;
import maker.backend.entity.*;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 16 — Gestion des commandes fournisseurs.
 * Cycle : BROUILLON → ENVOYEE → RECUE | ANNULEE
 */
@Service
@Transactional
public class CommandeFournisseurService {

    private final CommandeFournisseurRepository repo;
    private final FournisseurRepository fournisseurRepo;
    private final EntrepotRepository entrepotRepo;
    private final ProduitRepository produitRepo;
    private final UtilisateurRepository userRepo;
    private final StockService stockService;
    private final MouvementStockService mouvementService;

    public CommandeFournisseurService(CommandeFournisseurRepository repo,
                                      FournisseurRepository fournisseurRepo,
                                      EntrepotRepository entrepotRepo,
                                      ProduitRepository produitRepo,
                                      UtilisateurRepository userRepo,
                                      StockService stockService,
                                      MouvementStockService mouvementService) {
        this.repo = repo;
        this.fournisseurRepo = fournisseurRepo;
        this.entrepotRepo = entrepotRepo;
        this.produitRepo = produitRepo;
        this.userRepo = userRepo;
        this.stockService = stockService;
        this.mouvementService = mouvementService;
    }

    public List<CommandeFournisseurDTO> findAll() {
        return repo.findAllByOrderByDateCreaDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CommandeFournisseurDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    /** Crée une commande en BROUILLON */
    public CommandeFournisseurDTO creer(CommandeFournisseurDTO dto, String username) {
        CommandeFournisseur cmd = buildFromDTO(dto);
        cmd.setReference(genererReference());
        cmd.setStatut(CommandeFournisseur.Statut.BROUILLON);
        cmd.setCreateur(userRepo.findByUsername(username).orElse(null));
        return toDTO(repo.save(cmd));
    }

    /** Modifie uniquement si BROUILLON */
    public CommandeFournisseurDTO modifier(Long id, CommandeFournisseurDTO dto) {
        CommandeFournisseur cmd = getOrThrow(id);
        if (cmd.getStatut() != CommandeFournisseur.Statut.BROUILLON) {
            throw new IllegalArgumentException("Seules les commandes en brouillon peuvent être modifiées.");
        }
        cmd.setFournisseur(findFournisseur(dto.getFournisseurId()));
        cmd.setEntrepot(findEntrepot(dto.getEntrepotId()));
        cmd.setCommentaire(dto.getCommentaire());
        cmd.setDateLivraisonSouhaitee(dto.getDateLivraisonSouhaitee());
        cmd.getLignes().clear();
        dto.getLignes().forEach(l -> cmd.getLignes().add(toLigne(l, cmd)));
        return toDTO(repo.save(cmd));
    }

    /** BROUILLON → ENVOYEE */
    public CommandeFournisseurDTO envoyer(Long id) {
        CommandeFournisseur cmd = getOrThrow(id);
        if (cmd.getStatut() != CommandeFournisseur.Statut.BROUILLON) {
            throw new IllegalArgumentException("Seules les commandes en brouillon peuvent être envoyées.");
        }
        if (cmd.getLignes().isEmpty()) {
            throw new IllegalArgumentException("La commande doit contenir au moins une ligne.");
        }
        cmd.setStatut(CommandeFournisseur.Statut.ENVOYEE);
        cmd.setDateEnvoi(LocalDateTime.now());
        return toDTO(repo.save(cmd));
    }

    /** ENVOYEE → RECUE (met à jour les stocks) */
    public CommandeFournisseurDTO receptionner(Long id) {
        CommandeFournisseur cmd = getOrThrow(id);
        if (cmd.getStatut() != CommandeFournisseur.Statut.ENVOYEE) {
            throw new IllegalArgumentException("Seules les commandes envoyées peuvent être réceptionnées.");
        }

        for (CommandeLigne ligne : cmd.getLignes()) {
            Stock stock = stockService.findOrCreateStock(
                    ligne.getProduit().getId(), cmd.getEntrepot().getId(), null);
            stock.setQuantiteDisponible(stock.getQuantiteDisponible() + ligne.getQuantite());
            stockService.save(stock);

            MouvementStock m = new MouvementStock();
            m.setStock(stock);
            m.setType(MouvementStock.Type.ENTREE);
            m.setQuantite(ligne.getQuantite());
            m.setSource(cmd.getFournisseur().getNom());
            m.setCommentaire("Réception commande " + cmd.getReference());
            mouvementService.enregistrer(m);
        }

        stockService.recalculerCapaciteEntrepot(cmd.getEntrepot());
        cmd.setStatut(CommandeFournisseur.Statut.RECUE);
        cmd.setDateReception(LocalDateTime.now());
        return toDTO(repo.save(cmd));
    }

    /** Annulation (depuis BROUILLON ou ENVOYEE) */
    public CommandeFournisseurDTO annuler(Long id) {
        CommandeFournisseur cmd = getOrThrow(id);
        if (cmd.getStatut() == CommandeFournisseur.Statut.RECUE) {
            throw new IllegalArgumentException("Une commande déjà reçue ne peut pas être annulée.");
        }
        cmd.setStatut(CommandeFournisseur.Statut.ANNULEE);
        return toDTO(repo.save(cmd));
    }

    public void supprimer(Long id) {
        CommandeFournisseur cmd = getOrThrow(id);
        if (cmd.getStatut() != CommandeFournisseur.Statut.BROUILLON) {
            throw new IllegalArgumentException("Seules les commandes en brouillon peuvent être supprimées.");
        }
        repo.delete(cmd);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private CommandeFournisseur getOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable : " + id));
    }

    private CommandeFournisseur buildFromDTO(CommandeFournisseurDTO dto) {
        CommandeFournisseur cmd = new CommandeFournisseur();
        cmd.setFournisseur(findFournisseur(dto.getFournisseurId()));
        cmd.setEntrepot(findEntrepot(dto.getEntrepotId()));
        cmd.setCommentaire(dto.getCommentaire());
        cmd.setDateLivraisonSouhaitee(dto.getDateLivraisonSouhaitee());
        dto.getLignes().forEach(l -> cmd.getLignes().add(toLigne(l, cmd)));
        return cmd;
    }

    private CommandeLigne toLigne(CommandeLigneDTO dto, CommandeFournisseur cmd) {
        if (dto.getProduitId() == null) {
            throw new IllegalArgumentException("Chaque ligne de commande doit avoir un produit sélectionné.");
        }
        CommandeLigne l = new CommandeLigne();
        l.setCommande(cmd);
        l.setProduit(produitRepo.findById(dto.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + dto.getProduitId())));
        l.setQuantite(dto.getQuantite() != null ? dto.getQuantite() : 1);
        l.setPrixUnitaire(dto.getPrixUnitaire() != null ? dto.getPrixUnitaire() : 0.0);
        return l;
    }

    private Fournisseur findFournisseur(Long id) {
        return fournisseurRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable : " + id));
    }

    private Entrepot findEntrepot(Long id) {
        return entrepotRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    private String genererReference() {
        long count = repo.countBy() + 1;
        return String.format("CMD-%04d", count);
    }

    private CommandeFournisseurDTO toDTO(CommandeFournisseur cmd) {
        CommandeFournisseurDTO dto = new CommandeFournisseurDTO();
        dto.setId(cmd.getId());
        dto.setReference(cmd.getReference());
        dto.setFournisseurId(cmd.getFournisseur().getId());
        dto.setFournisseurNom(cmd.getFournisseur().getNom());
        dto.setEntrepotId(cmd.getEntrepot().getId());
        dto.setEntrepotNom(cmd.getEntrepot().getNom());
        dto.setStatut(cmd.getStatut().name());
        dto.setCommentaire(cmd.getCommentaire());
        if (cmd.getCreateur() != null) dto.setCreateurNom(cmd.getCreateur().getNomComplet());
        dto.setDateCrea(cmd.getDateCrea());
        dto.setDateEnvoi(cmd.getDateEnvoi());
        dto.setDateReception(cmd.getDateReception());
        dto.setDateLivraisonSouhaitee(cmd.getDateLivraisonSouhaitee());
        dto.setLignes(cmd.getLignes().stream().map(this::toLigneDTO).collect(Collectors.toList()));
        double montant = cmd.getLignes().stream()
                .filter(l -> l.getPrixUnitaire() != null)
                .mapToDouble(l -> l.getQuantite() * l.getPrixUnitaire())
                .sum();
        dto.setMontantTotal(montant);
        return dto;
    }

    private CommandeLigneDTO toLigneDTO(CommandeLigne l) {
        CommandeLigneDTO dto = new CommandeLigneDTO();
        dto.setId(l.getId());
        dto.setProduitId(l.getProduit().getId());
        dto.setProduitNom(l.getProduit().getNom());
        dto.setQuantite(l.getQuantite());
        dto.setPrixUnitaire(l.getPrixUnitaire());
        dto.setSousTotal(l.getPrixUnitaire() != null ? l.getQuantite() * l.getPrixUnitaire() : null);
        return dto;
    }
}
