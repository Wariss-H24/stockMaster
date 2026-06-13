package maker.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "zones")
@Data
public class ZoneFr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nom;

    // Relation vers l'entrepôt parent
    @ManyToOne(optional = false)
    @JoinColumn(name = "entrepot_id")
    private Entrepot entrepot;

    @Min(0)
    private Integer capaciteTotale;

    @Min(0)
    private Integer capaciteUtilisee;

    // Actif = true par défaut
    private boolean actif = true;
}
