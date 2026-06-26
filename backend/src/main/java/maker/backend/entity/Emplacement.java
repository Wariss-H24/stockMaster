package maker.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "emplacements")
@Data
public class Emplacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "zone_id")
    private ZoneFr zone;

    @NotBlank
    private String rayon;

    @NotBlank
    private String etagere;

    @NotBlank
    private String code;

    private boolean occupe = false;

    /** Retourne le code complet hiérarchique ex: ENT-001/ZONE-A/RAYON-03/ETAGERE-02/EMP-12 */
    @Transient
    public String getCodeComplet() {
        return zone.getEntrepot().getNom() + "/" + zone.getNom() + "/" + rayon + "/" + etagere + "/" + code;
    }
}
