package maker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ZoneFr {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @ManyToOne
    private Entrepot entrepot;

    private Integer capaciteTotale;
    private Integer capaciteUtilisee;
}
