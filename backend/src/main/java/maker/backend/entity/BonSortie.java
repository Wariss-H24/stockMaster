package maker.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bon_sorties")
@Data
public class BonSortie {

    public enum Statut {
        BROUILLON, VALIDE, ANNULE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entrepot_id")
    private Entrepot entrepot;

    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.BROUILLON;

    private String bordereauReference;

    private LocalDateTime date = LocalDateTime.now();

    private String commentaire;

    @OneToMany(mappedBy = "bonSortie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SortieLigne> lignes = new ArrayList<>();
}
