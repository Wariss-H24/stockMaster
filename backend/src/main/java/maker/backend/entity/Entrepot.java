package maker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Entrepot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String adresse;
    private String responsable;
    private Integer capaciteTotale;
    private Integer capaciteUtilisee;
    private boolean actif = true;
}
