package maker.backend.mapper;

import maker.backend.dto.TransfertDTO;
import maker.backend.entity.Transfert;
import maker.backend.service.EmplacementService;
import org.springframework.stereotype.Component;

/**
 * Mapper Transfert ↔ TransfertDTO — inclut la hiérarchie d'emplacement (Module 17).
 */
@Component
public class TransfertMapper {

    public TransfertDTO toDTO(Transfert t) {
        TransfertDTO dto = new TransfertDTO();
        dto.setId(t.getId());
        dto.setReference(t.getReference());
        dto.setQuantite(t.getQuantite());
        dto.setStatut(t.getStatut().name());
        dto.setCommentaire(t.getCommentaire());
        dto.setDateCrea(t.getDateCrea());
        dto.setDateExpedi(t.getDateExpedi());
        dto.setDateRecu(t.getDateRecu());

        if (t.getProduit() != null) {
            dto.setProduitId(t.getProduit().getId());
            dto.setProduitNom(t.getProduit().getNom());
        }
        if (t.getEntrepotSource() != null) {
            dto.setEntrepotSourceId(t.getEntrepotSource().getId());
            dto.setEntrepotSourceNom(t.getEntrepotSource().getNom());
        }
        if (t.getZoneSource() != null) {
            dto.setZoneSourceId(t.getZoneSource().getId());
            dto.setZoneSourceNom(t.getZoneSource().getNom());
        }
        if (t.getEmplacementSource() != null) {
            dto.setEmplacementSourceId(t.getEmplacementSource().getId());
            dto.setEmplacementSourceCode(EmplacementService.buildCodeComplet(t.getEmplacementSource()));
        }
        if (t.getEntrepotDestination() != null) {
            dto.setEntrepotDestinationId(t.getEntrepotDestination().getId());
            dto.setEntrepotDestinationNom(t.getEntrepotDestination().getNom());
        }
        if (t.getZoneDestination() != null) {
            dto.setZoneDestinationId(t.getZoneDestination().getId());
            dto.setZoneDestinationNom(t.getZoneDestination().getNom());
        }
        if (t.getEmplacementDestination() != null) {
            dto.setEmplacementDestinationId(t.getEmplacementDestination().getId());
            dto.setEmplacementDestinationCode(EmplacementService.buildCodeComplet(t.getEmplacementDestination()));
        }
        if (t.getCreateur() != null) {
            dto.setCreateurNom(t.getCreateur().getNomComplet());
        }
        return dto;
    }
}
