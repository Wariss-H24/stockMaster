package maker.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Entity
@Table(name = "stocks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"produit_id", "entrepot_id", "zone_id"})
})
@Data
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entrepot_id")
    private Entrepot entrepot;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private ZoneFr zone;

    @Min(0)
    private Integer quantiteDisponible = 0;

    @Min(0)
    private Integer quantiteReservee = 0;

    @Min(0)
    private Integer quantiteTransit = 0;

    @Min(0)
    private Integer stockMin = 0;

    @Min(0)
    private Integer stockMax = 0;
}
