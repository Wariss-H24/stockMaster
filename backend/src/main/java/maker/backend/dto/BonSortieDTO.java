package maker.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BonSortieDTO {
    private Long id;

    @NotNull(message = "L'entrepôt est requis")
    private Long entrepotId;
    private String entrepotNom;

    // Emplacement de sortie (indique au magasinier où aller chercher)
    private Long zoneId;
    private String zoneNom;
    private String emplacementCodeComplet; // ex: ENT-001/ZONE-A/RAYON-03/ETAGERE-02/EMP-12

    @NotBlank(message = "La destination est obligatoire")
    private String destination;

    private String bordereauReference;
    private String statut;
    private String commentaire;
    private String date;

    @Valid
    @Size(min = 1, message = "Au moins une ligne de sortie est requise")
    private List<SortieLigneDTO> lignes = new ArrayList<>();
}
