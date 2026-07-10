package maker.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Module 10 — Transfert inter-entrepôts.
 * Cycle de vie : BROUILLON → EXPEDIE → RECU | ANNULE
 */
@Entity
@Table(name = "transferts")
@Data
public class Transfert {

    public enum Statut {
        BROUILLON,  // créé, pas encore validé
        EXPEDIE,    // validé et expédié depuis la source
        RECU,       // réceptionné à destination
        ANNULE      // annulé avant expédition
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Numéro de référence lisible (TRF-0001)
    @Column(unique = true, nullable = false)
    private String reference;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entrepot_source_id")
    private Entrepot entrepotSource;

    @ManyToOne
    @JoinColumn(name = "zone_source_id")
    private ZoneFr zoneSource;

    /** Module 17 — Emplacement source précis */
    @ManyToOne
    @JoinColumn(name = "emplacement_source_id")
    private Emplacement emplacementSource;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entrepot_dest_id")
    private Entrepot entrepotDestination;

    @ManyToOne
    @JoinColumn(name = "zone_dest_id")
    private ZoneFr zoneDestination;

    /** Module 17 — Emplacement destination */
    @ManyToOne
    @JoinColumn(name = "emplacement_dest_id")
    private Emplacement emplacementDestination;

    @Min(1)
    private Integer quantite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.BROUILLON;

    private String commentaire;

    // Qui a créé le transfert
    @ManyToOne
    @JoinColumn(name = "createur_id")
    private Utilisateur createur;

    private LocalDateTime dateCrea   = LocalDateTime.now();
    private LocalDateTime dateExpedi;
    private LocalDateTime dateRecu;
}
