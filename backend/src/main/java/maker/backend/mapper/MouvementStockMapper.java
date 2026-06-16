package maker.backend.mapper;

import maker.backend.dto.MouvementStockDTO;
import maker.backend.entity.MouvementStock;
import org.springframework.stereotype.Component;

@Component
public class MouvementStockMapper {

    public MouvementStockDTO toDTO(MouvementStock m) {
        MouvementStockDTO dto = new MouvementStockDTO();
        dto.setId(m.getId());
        dto.setStockId(m.getStock().getId());
        dto.setProduitReference(m.getStock().getProduit().getReference());
        dto.setProduitNom(m.getStock().getProduit().getNom());
        dto.setEntrepotNom(m.getStock().getEntrepot().getNom());
        dto.setType(m.getType().name());
        dto.setQuantite(m.getQuantite());
        dto.setDate(m.getDate());
        dto.setSource(m.getSource());
        dto.setCommentaire(m.getCommentaire());
        if (m.getStock().getZone() != null) {
            dto.setZoneNom(m.getStock().getZone().getNom());
        }
        return dto;
    }
}
