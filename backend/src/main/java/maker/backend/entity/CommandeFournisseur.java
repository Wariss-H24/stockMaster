package maker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 16 — Commande fournisseur.
 * Cycle : BROUILLON → ENVOYEE → RECUE | ANNULEE
 */
@Entity
@Table(name = "commandes_fournisseurs")
@Data
public class CommandeFournisseur {

    public enum Statut { BROUILLON, ENVOYEE, RECUE, ANNULEE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entrepot_id")
    private Entrepot entrepot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.BROUILLON;

    private String commentaire;

    @ManyToOne
    @JoinColumn(name = "createur_id")
    private Utilisateur createur;

    private LocalDateTime dateCrea    = LocalDateTime.now();
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateReception;

    /** Date de livraison souhaitée (date seule, sans heure) */
    private LocalDate dateLivraisonSouhaitee;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeLigne> lignes = new ArrayList<>();
}
