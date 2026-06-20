package maker.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommandeFournisseurDTO {

    private Long id;
    private String reference;

    @NotNull(message = "Le fournisseur est requis")
    private Long fournisseurId;
    private String fournisseurNom;

    @NotNull(message = "L'entrepôt est requis")
    private Long entrepotId;
    private String entrepotNom;

    private String statut;
    private String commentaire;

    private String createurNom;
    private LocalDateTime dateCrea;
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateReception;
    private LocalDateTime dateLivraisonSouhaitee;

    private List<CommandeLigneDTO> lignes = new ArrayList<>();

    // Montant total calculé
    private Double montantTotal;
}
