package maker.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class InventaireDTO {

    private Long id;
    private String reference;

    @NotNull(message = "Le type est requis")
    private String type;      // COMPLET | PARTIEL

    private String statut;

    @NotNull(message = "L'entrepôt est requis")
    private Long entrepotId;
    private String entrepotNom;

    private Long zoneId;
    private String zoneNom;

    private String commentaire;

    private Long responsableId;
    private String responsableNom;

    private LocalDateTime dateDebut;
    private LocalDateTime dateCloture;

    private List<InventaireLigneDTO> lignes = new ArrayList<>();

    // Résumé écarts (calculé à la clôture)
    private int nbLignes;
    private int nbEcartsPositifs;
    private int nbEcartsNegatifs;
    private int nbSansEcart;
}
