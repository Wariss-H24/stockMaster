package maker.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "entrepots")
@Data
public class Entrepot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nom;

    @NotBlank
    private String adresse;

    private String responsable;

    @Min(0)
    private Integer capaciteTotale;

    @Min(0)
    private Integer capaciteUtilisee;

    // Désactivable sans suppression physique
    private boolean actif = true;
}
