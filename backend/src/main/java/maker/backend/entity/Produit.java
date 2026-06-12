package maker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;
    private String codeBarre;
    private String nom;
    private String categorie;
    private String description;
    private Double prixAchat;
    private Double prixVente;
    private Double poids;
    private Double volume;
    private boolean supprime = false;
}
