package maker.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BonReceptionDTO {
    private Long id;

    @NotNull(message = "Le fournisseur est requis")
    private Long fournisseurId;
    private String fournisseurNom;

    @NotNull(message = "L'entrepôt est requis")
    private Long entrepotId;
    private String entrepotNom;

    private Long zoneId;
    private String zoneNom;
    private String commentaire;
    private String statut;
    private boolean controleQualiteOk;
    private String date;

    @Valid
    @Size(min = 1, message = "Au moins une ligne de réception est requise")
    private List<ReceptionLigneDTO> lignes = new ArrayList<>();
}
