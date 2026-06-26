package maker.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Module 17 — Rayon : subdivision d'une Zone.
 * Hiérarchie : Entrepôt → Zone → Rayon → Étagère → Emplacement
 */
@Entity
@Table(name = "rayons")
@Data
public class Rayon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nom;

    /** Code court lisible ex: RAYON-03 */
    @Column(unique = true)
    private String code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "zone_id")
    private ZoneFr zone;

    private String description;
    private boolean actif = true;
}
