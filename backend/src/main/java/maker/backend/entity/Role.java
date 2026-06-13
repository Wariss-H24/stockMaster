package maker.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom du rôle : ADMIN, GESTIONNAIRE, MAGASINIER, AUDITEUR
    @NotBlank
    @Column(unique = true, nullable = false)
    private String nom;
}
