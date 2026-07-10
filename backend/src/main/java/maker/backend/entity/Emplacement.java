package maker.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Module 17 — Emplacement : position exacte dans une Étagère.
 * Code complet ex: ENT-001/ZONE-A/RAYON-03/ETAGERE-02/EMP-12
 */
@Entity
@Table(name = "emplacements")
@Data
public class Emplacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nom;

    /** Code unique lisible ex: EMP-012 */
    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "etagere_id")
    private Etagere etagere;

    /** Capacité maximale en unités */
    private Integer capaciteMax;

    /** Capacité actuellement utilisée (mise à jour automatiquement) */
    private Integer capaciteUtilisee = 0;

    private boolean actif = true;
}
