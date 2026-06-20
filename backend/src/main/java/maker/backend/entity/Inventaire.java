package maker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 11 — Inventaire physique (complet ou partiel).
 * Cycle : EN_COURS → CLOTURE | ANNULE
 */
@Entity
@Table(name = "inventaires")
@Data
public class Inventaire {

    public enum Type   { COMPLET, PARTIEL }
    public enum Statut { EN_COURS, CLOTURE, ANNULE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.EN_COURS;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entrepot_id")
    private Entrepot entrepot;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private ZoneFr zone;

    private String commentaire;

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Utilisateur responsable;

    private LocalDateTime dateDebut  = LocalDateTime.now();
    private LocalDateTime dateCloture;

    @OneToMany(mappedBy = "inventaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventaireLigne> lignes = new ArrayList<>();
}
