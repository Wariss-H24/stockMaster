package maker.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "produits")
@Data
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String reference;

    @Column(unique = true)
    private String codeBarre;

    @NotBlank
    private String nom;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    private String description;

    @Min(0)
    private Double prixAchat;

    @Min(0)
    private Double prixVente;

    private Double poids;
    private Double volume;

    private boolean supprime = false;
}
