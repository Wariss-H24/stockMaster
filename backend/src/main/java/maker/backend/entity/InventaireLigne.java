package maker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Ligne d'inventaire : une ligne = un produit avec stock théorique (système)
 * et stock physique (compté sur place).
 */
@Entity
@Table(name = "inventaire_lignes")
@Data
public class InventaireLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inventaire_id")
    private Inventaire inventaire;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    /** Quantité enregistrée dans le système avant l'inventaire */
    private Integer quantiteSysteme = 0;

    /** Quantité réellement comptée sur le terrain */
    private Integer quantitePhysique = 0;

    /** Écart = physique - système (positif = surplus, négatif = manque) */
    private Integer ecart = 0;

    private String commentaire;
}
