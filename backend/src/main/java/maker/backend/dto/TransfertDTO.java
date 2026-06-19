package maker.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO Transfert — utilisé pour créer et afficher un transfert.
 * Les champs *Nom sont renvoyés au frontend pour l'affichage.
 */
@Data
public class TransfertDTO {

    private Long id;
    private String reference;

    @NotNull(message = "Le produit est requis")
    private Long produitId;
    private String produitNom;

    @NotNull(message = "L'entrepôt source est requis")
    private Long entrepotSourceId;
    private String entrepotSourceNom;

    private Long zoneSourceId;
    private String zoneSourceNom;

    @NotNull(message = "L'entrepôt de destination est requis")
    private Long entrepotDestinationId;
    private String entrepotDestinationNom;

    private Long zoneDestinationId;
    private String zoneDestinationNom;

    @NotNull(message = "La quantité est requise")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;

    private String statut;
    private String commentaire;

    private String createurNom;
    private LocalDateTime dateCrea;
    private LocalDateTime dateExpedi;
    private LocalDateTime dateRecu;
}
