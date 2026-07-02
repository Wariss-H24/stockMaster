package maker.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Module 17 — Étagère : subdivision d'un Rayon.
 */
@Entity
@Table(name = "etageres")
@Data
public class Etagere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nom;

    /** Code court lisible ex: ETAGERE-02 */
    @Column(unique = true)
    private String code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rayon_id")
    private Rayon rayon;

    private Integer niveaux; // nombre de niveaux/tablettes
    private boolean actif = true;
}
