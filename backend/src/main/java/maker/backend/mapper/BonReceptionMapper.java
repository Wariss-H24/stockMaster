package maker.backend.mapper;

import maker.backend.dto.BonReceptionDTO;
import maker.backend.dto.ReceptionLigneDTO;
import maker.backend.entity.BonReception;
import maker.backend.entity.ReceptionLigne;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BonReceptionMapper {

    public BonReceptionDTO toDTO(BonReception bon) {
        BonReceptionDTO dto = new BonReceptionDTO();
        dto.setId(bon.getId());
        dto.setFournisseurId(bon.getFournisseur().getId());
        dto.setEntrepotId(bon.getEntrepot().getId());
        dto.setZoneId(bon.getZone() != null ? bon.getZone().getId() : null);
        dto.setCommentaire(bon.getCommentaire());
        dto.setStatut(bon.getStatut().name());
        dto.setControleQualiteOk(Boolean.TRUE.equals(bon.getControleQualiteOk()));
        dto.setLignes(bon.getLignes().stream().map(this::toDTO).collect(Collectors.toList()));
        return dto;
    }

    public ReceptionLigneDTO toDTO(ReceptionLigne ligne) {
        ReceptionLigneDTO dto = new ReceptionLigneDTO();
        dto.setId(ligne.getId());
        dto.setProduitId(ligne.getProduit().getId());
        dto.setQuantite(ligne.getQuantite());
        dto.setPrixAchat(ligne.getPrixAchat());
        return dto;
    }
}
