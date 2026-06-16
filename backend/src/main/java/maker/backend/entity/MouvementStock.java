package maker.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_stock")
@Data
public class MouvementStock {

    public enum Type {
        ENTREE, SORTIE, TRANSFERT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Min(0)
    private Integer quantite;

    private LocalDateTime date = LocalDateTime.now();

    private String source;
    private String commentaire;
}
