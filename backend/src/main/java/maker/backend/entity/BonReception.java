package maker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bon_receptions")
@Data
public class BonReception {

    public enum Statut {
        BROUILLON, VALIDE, ANNULE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entrepot_id")
    private Entrepot entrepot;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private ZoneFr zone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.BROUILLON;

    private Boolean controleQualiteOk = false;

    private LocalDateTime date = LocalDateTime.now();

    private String commentaire;

    @OneToMany(mappedBy = "bonReception", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceptionLigne> lignes = new ArrayList<>();
}
